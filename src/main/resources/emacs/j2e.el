;;; j2e.el -- Java to Emacs and back

;;; Original Copyright (C) 1997 Cygnus Solutions

(require 'compile)

;;; Known problems and things to do:

;;; Constants.

;; non-nil if using XEmacs.
(defconst j2e-is-xemacs (string-match "XEmacs" emacs-version))

;;; Variable definitions.

;; History list for tags finding.
(defvar j2e-history-list nil)

;; This holds the connection to Java.  It is local to each buffer; this
;; lets us have multiple Java servers share an Emacs.
(defvar j2e-process nil)
(make-variable-buffer-local 'j2e-process)

;; Name of the current process.  This is only set when running a
;; function from a process filter.  It is only defvar'd because I
;; don't like to use variables that aren't declared.
(defvar j2e-current-process nil)

;; Name of the Java application that uses this emacs.
(defvar j2e-application "j2e")

;; The base directory of the current project. All files will be relative to
;; this directory
(defvar j2e-project-directory nil)

;; List of files belonging to the current project. j2e-find-file-hook will
;; check if a file is contained in the list and put the buffer into
;; j2e-minor-mode, eventually
(defvar j2e-project-file-list nil)

(defvar j2e-minor-mode nil "t if j2e mode is active")
(make-variable-buffer-local 'j2e-minor-mode)
(or (assoc 'j2e-minor-mode minor-mode-alist)
    (setq minor-mode-alist (cons '(j2e-minor-mode " J2E") minor-mode-alist)))
(setplist 'j2e-minor-mode (plist-put (symbol-plist 'j2e-minor-mode)
				    'permanent-local t))

(defun j2e-minor-mode (arg)
  "Minor mode for working with Java.
Adds some commands for looking up stuff in J2E:
\\{j2e-keymap}
This mode is automatically activated when files are opened by J2E and cannot
be activated for other buffers.  You can toggle it for J2E-related buffers
though.  This lets you access the command bindings that this mode overrides."
  (interactive "P")
  (unless j2e-process
    (error "This buffer has no j2emacs connection"))
  (setq j2e-minor-mode (if (null arg) (not j2e-minor-mode)
                         (> (prefix-numeric-value arg) 0))))

(defconst j2e-comp-error-regexp-alist
  '(("^\\(?:INF\\(O\\)\\|WAR\\(N\\)\\): \\(?:[^:]*\\): \\([^(\n]*\\)" 3 nil nil (1 . 2))
    ("^ERROR: \\(.+?\\)\\(:[ \t]*\\)\\([0-9]+\\)\\2\\([0-9]+\\):" 1 3 4)
    ))

(defvar j2e-comp-mode-map nil
  "Keymap for j2e compilation buffers.")

(defun j2e-create-menu ()
  (setq compilation-menu-map nil)
  (let ((map (make-sparse-keymap)))
    (set-keymap-parent map compilation-button-map)
    (define-key map "\C-c\C-f" 'next-error-follow-minor-mode)
    (define-key map "n" 'next-error-no-select)
    (define-key map "p" 'previous-error-no-select)
    (define-key map "e" 'compile-goto-error)
    (define-key map "{" 'compilation-previous-file)
    (define-key map "}" 'compilation-next-file)
    (define-key map "\t" 'compilation-next-error)
    (define-key map [backtab] 'compilation-previous-error)

    ;; Set up the menu-bar
    (define-key map [menu-bar grep]
      (cons j2e-application (make-sparse-keymap j2e-application)))

    (let ((opt-map (make-sparse-keymap "Skip")))
      (define-key map [menu-bar grep compilation-skip]
        (cons "Skip Less Important Messages" opt-map))

      (define-key map [menu-bar grep compilation-mode-separator1]
        '("----" . nil))

      (define-key opt-map [compilation-skip-none]
        '(menu-item "Don't Skip Any Messages"
                    (lambda ()
                      (interactive)
                      (customize-set-variable 'compilation-skip-threshold 0))
                    :help "Do not skip any type of messages"
                    :button (:radio . (eq compilation-skip-threshold 0))))
      (define-key opt-map [compilation-skip-info]
        '(menu-item "Skip Info"
                    (lambda ()
                      (interactive)
                      (customize-set-variable 'compilation-skip-threshold 1))
                    :help "Skip anything less than warning"
                    :button (:radio . (eq compilation-skip-threshold 1))))
      (define-key opt-map [compilation-skip-warning-and-info]
        '(menu-item "Skip Warnings and Info"
                    (lambda ()
                      (interactive)
                      (customize-set-variable 'compilation-skip-threshold 2))
                    :help "Skip over Warnings and Info, stop for errors"
                    :button (:radio . (eq compilation-skip-threshold 2)))))

    (define-key map [menu-bar grep compilation-first-error]
      '(menu-item "First Error" compilation-first-error
                  :help "Restart at the first error, visit corresponding source code"))
    (define-key map [menu-bar grep compilation-previous-error]
      '(menu-item "Previous Error" compilation-previous-error
		  :help "Visit previous `previous-error' message and corresponding source code"
                  :keys "p"))
    (define-key map [menu-bar grep compilation-next-error]
      '(menu-item "Next Error" compilation-next-error
                  :help "Visit next `next-error' message and corresponding source code"
                  :keys "n"))
    (define-key map [menu-bar grep compilation-separator2]
      '("----" . nil))
    (define-key map [menu-bar grep compilation-grep]
      '(menu-item "Search Files (grep)..." grep
                  :help "Run grep, with user-specified args, and collect output in a buffer"))
    (define-key map [menu-bar grep compilation-recompile]
      '(menu-item "Reload" j2e-reload
                  :help "Re-load the project files"))
    (setq j2e-comp-mode-map map)))


;; When we tell J2E about a file, we must always send it exactly the
;; same name as it sent us.  So we stash the original filename here.
(defvar j2e-file-name nil)
(make-variable-buffer-local 'j2e-file-name)

(defvar j2e-keymap nil
  "Keymap for j2emacs minor mode.")
(defun j2e-keymap ()
  (unless j2e-keymap
    (setq j2e-keymap (make-sparse-keymap))
    (define-key j2e-keymap "\C-c\C-c" 'j2e-reload)

    (define-key j2e-keymap [menu-bar grep]
      (cons j2e-application (make-sparse-keymap j2e-application)))
    (define-key j2e-keymap [menu-bar grep reload]
      '(menu-item "Reload" j2e-reload
                  :help "Re-load the project files"))
    (or (assoc 'j2e-minor-mode minor-mode-map-alist)
        (setq minor-mode-map-alist (cons (cons 'j2e-minor-mode j2e-keymap)
                                         minor-mode-map-alist)))))

;;;
;;; Commands that the user can run to interact with J2E.
;;;

;; Hide the current project.
(defun j2e-hide-project ()
  "Hide the Source Navigator project associated with this buffer."
  (interactive)
  (j2e-send "tkbHideShow withdraw"))

;; Like find-tag, but use SN to look up the tag.
(defun j2e-find-tag (tagname)
  "Like find-tag, but use Source Navigator to look up name."
  (interactive
   (progn
     (require 'etags)
     (list (read-string "Find tag: "
			(find-tag-default)
			'j2e-history-list))))
  (j2e-send (concat "sn_emacs_display_object "
		   (j2e-quote tagname)))
  ;; We know a response is coming.  This makes things look a little
  ;; more synchronous.
  (accept-process-output))

;; Like find-tag, but use SN to look up the tag.
(defun j2e-find-implementation-tag (tagname)
  "Like find-tag, but use Source Navigator to look up name."
  (interactive
   (progn
     (require 'etags)
     (list (read-string "Find tag: "
			(find-tag-default)
			'j2e-history-list))))
  (j2e-send (concat "sn_emacs_display_implementation "
		   (j2e-quote tagname)))
  ;; We know a response is coming.  This makes things look a little
  ;; more synchronous.
  (accept-process-output))

(defun j2e-classbrowser (class)
  "Browse the contents of a class in the Source Navigator."
  (interactive
   (progn
     (require 'etags)
     (list (read-string "Browse class: "
			(find-tag-default)
			'j2e-history-list))))
  (j2e-send (concat "sn_classbrowser " (j2e-quote class))))

(defun j2e-classtree (class)
  "Browse a class in the Source Navigator hierarchy browser."
  (interactive
   (progn
     (require 'etags)
     (list (read-string "Browse class: "
			(find-tag-default)
			'j2e-history-list))))
  (j2e-send (concat "sn_classtree " (j2e-quote class))))

(defun j2e-retrieve (pattern)
  "Tell Source Navigator to retrieve all symbols matching pattern.
If there is only one match SN will take Emacs there.  If there are
several they are listed in a pop-up where you can select one to edit."
  (interactive
   (progn
     (require 'etags)
     (list (read-string "Retrieve pattern: "
                        (find-tag-default)
                        'j2e-history-list))))
  (j2e-send (concat "sn_retrieve_symbol " (j2e-quote pattern) " all")))

(defun j2e-xref (symbol)
  "Look up a symbol in the Source Navigator cross-referencer."
  (interactive
   (progn
     (require 'etags)
     (list (read-string "Xref symbol: "
			(find-tag-default)
			'j2e-history-list))))
  (j2e-send (concat "sn_xref both " (j2e-quote symbol))))

(defun j2e-tag-unimplemented ()
  "Bound to tags-finding keys that Source Navigator can't (yet) handle."
  (interactive)
  (error "this keybinding is unimplemented in Source Navigator"))

;; functions talking to the main application

(defun j2e-reload ()
  "Look up a symbol in the Source Navigator cross-referencer."
  (interactive)
  (save-some-buffers)
  (j2e-send "reload"))

;; functions to be called by the main application

(defun j2e-project-files(directory filenames)
  (setq j2e-project-directory directory)
  (setq j2e-project-file-list filenames))

(defun j2e-kill-buffer(name)
  (let ((buffer (get-buffer name)))
    (if buffer (kill-buffer buffer))))

(defun j2e-append-to-buffer(name what)
  (save-excursion
    (with-current-buffer (get-buffer-create name)
      (toggle-read-only 0)
      (goto-char (point-max))
      (insert what))))

(defun j2e-clear-buffer(name)
  (save-excursion
    (let ((buffer (get-buffer name )))
      (if buffer
          (with-current-buffer buffer
            (delete-region (point-min) (point-max)))))))

(defun j2e-compilation-buffer(name)
  (save-excursion
    (let ((buffer (get-buffer name)))
      (unless buffer
        (setq buffer (get-buffer-create name))
        (with-current-buffer buffer
          (j2e-comp-mode)
          (setq j2e-process j2e-current-process)
          (setq j2e-minor-mode t)))
      (if (not (get-buffer-window buffer))
          (pop-to-buffer buffer)))))


;; find-tag-other-frame and find-tag-other-window versions are harder
;; to do; there is a synchronization problem here.
;; (defun j2e-find-tag-other-frame)
;;(defun j2e-find-tag-other-window)
;; (defun j2e-find-tag-regexp) ; FIXME do it?
;; FIXME what about tags-query-replace, tags-loop-continue,
;; tags-search, tags-table-files, find-tag-hook, find-tag-noselect?

;; Turn off menus for now.  Why bother when there is only one item?
;    (progn
;      (define-key j2e-keymap [menu-bar SN] (cons "SN" (make-sparse-keymap)))
;      (define-key j2e-keymap [menu-bar SN hide] '("Hide project"
;						 . j2e-hide-project)))
;    )

;;;
;;; Internal functions that can talk to SN.
;;;

;; Connect to a java application.  Arguments are:
;; * APPLICATIONNAME - The name of the application, to be used in buffer names
;;              and menus
;; * HOSTNAME - name of host to connect to
;; * DIRECTORY - directory where temp file might be (if not absolute)
;; * PORT - port to connect to
(defun j2e-startup (appname hostname port)
  (setq j2e-application appname)
  (message "Starting %s process (%s:%s)" j2e-application hostname port)
  (j2e-create-menu)
  (j2e-keymap)
  (save-excursion
    (let ((buffer (generate-new-buffer " j2e")))
      (set-buffer buffer)
      (setq j2e-process (open-network-stream "j2e" buffer hostname port))
      (set-process-query-on-exit-flag j2e-process nil)
      (set-process-filter j2e-process 'j2e-filter)
      (set-process-sentinel j2e-process 'j2e-sentinel))
    (add-hook 'find-file-hook 'j2e-check-project-file)))

;; This quoting is sufficient to protect eg a filename from any sort
;; of expansion or splitting.  Tcl quoting sure sucks.
(defun j2e-quote (string)
  (mapconcat (function (lambda (char)
			 (if (memq char '(?[ ?] ?{ ?} ?\\ ?\" ?$ ?  ?\;))
			     (concat "\\" (char-to-string char))
			   (char-to-string char))))
	     string ""))

;; Send a command to J2E.
(defun j2e-send (string)
  (process-send-string j2e-process (concat string "\n")))

;; Check if a buffer is in the project file list and connect him to the
;; application eventually
(defun j2e-check-project-file()
  (let ((buffer-file (buffer-file-name (current-buffer)))
        (project-files j2e-project-file-list))
    (while project-files
      (let ((path ;;(expand-file-name (car project-files) j2e-project-directory)
             (car project-files)))
        (if (string= path buffer-file)
            (progn
              (j2e-make-client-buffer (car project-files))
              (setq project-files nil))
          (setq project-files (cdr project-files)))))))



;; This is run on a hook after a file is saved.  If we have to, we
;; notify the appropriate J2E.
(defun j2e-after-save ()
  (if j2e-minor-mode
      (j2e-send (concat "file_changed " (j2e-quote j2e-file-name)))))

;; This is the process filter for reading from J2E.  It just tries to
;; read the process buffer as a lisp object; when the read succeeds,
;; the result is evalled.
(defun j2e-filter (proc string)
  ;; Only do the work if the process buffer is alive.
  (if (buffer-name (process-buffer proc))
      (let ((inhibit-quit t)
	    (j2e-current-process proc)
	    form form-list)
	(save-match-data
	  (save-excursion
	    (set-buffer (process-buffer proc))
	    ;; If process marker not already set, we must set it.
	    ;; This seems to contradict the docs; go figure.
	    (or (marker-position (process-mark proc))
		(set-marker (process-mark proc) (point-min)))
	    (goto-char (process-mark proc))
	    (insert string)
	    (set-marker (process-mark proc) (point))
	    (goto-char (point-min))
	    ;; Note that we only catch end-of-file.  invalid-read-syntax
	    ;; we let through; that indicates an J2E bug that we really
	    ;; want to see.
	    (while (progn
		     (setq form (condition-case nil
				    (read (current-buffer))
				  (end-of-file nil)))
		     form)
	      ;; Remove the stuff we've read.
	      (delete-region (point-min) (point))
	      (setq form-list (cons form form-list)))))
	;; Now go through each form on our list and eval it.  We do
	;; this outside the save-excursion because we want the
	;; expression to be able to move point around.  We also turn
	;; C-g back on.
	(setq form-list (nreverse form-list))
	(setq inhibit-quit nil)
	(while form-list
          (eval (car form-list))
	  (setq form-list (cdr form-list))))))

;; This is run when the J2E connection dies.  We go through each buffer
;; and do some cleaning up.  We also remove our own process buffer.
(defun j2e-sentinel (process event)
  (save-excursion
    (let ((b-list (buffer-list)))
      (while b-list
	(set-buffer (car b-list))
	(if (eq j2e-process process)
	    (progn
	      ;; This buffer belongs to the current invocation.  Close
	      ;; down.
	      (setq j2e-process nil)
	      (setq j2e-minor-mode nil)))
	(setq b-list (cdr b-list)))))
  (kill-buffer (process-buffer process)))

;;;
;;; Functions that are run by J2E.  These functions can assume that
;;; j2e-current-process is set, if they like.
;;;

(defun j2e-make-client-buffer(partial-file)
  (setq j2e-minor-mode t)
  (setq j2e-process (with-current-buffer (get-buffer " j2e")
                      j2e-process))
  (setq j2e-file-name partial-file)
  (add-hook 'after-save-hook 'j2e-after-save nil t))

;; helps on Linux, what about MacOS, Windows?
(defadvice raise-frame (after make-it-work (&optional frame) activate)
  "Work around some bug? in raise-frame/Emacs/GTK/Metacity/something.
   Katsumi Yamaoka <yamaoka@jpl.org> posted this in
   http://article.gmane.org/gmane.emacs.devel:39702"
  (call-process
   "wmctrl" nil nil nil "-i" "-R"
   (frame-parameter (or frame (selected-frame)) 'outer-window-id)))

;; Sent by J2E when we should visit a file.
;; Arguments are:
;; * DIRECTORY    - base directory of project
;; * PARTIAL-FILE - possibly-relative filename
;; * LINE, COLUMN - where cursor should end up
;; * STATE        - either "normal" or "disabled"; the latter means read-only
(defun j2e-visit (directory partial-file line column state)
  (let* ((file (expand-file-name partial-file directory))
	 (obuf (get-file-buffer file)))
    (cond (obuf (switch-to-buffer obuf)
		(push-mark))
	  (t (setq obuf (if (string= state "disabled")
                            (find-file-read-only file)
                          (find-file file)))))
    ;; (message "visit %s %d %d (%s)" file line column obuf)
    (setq j2e-minor-mode t)
    (setq j2e-process j2e-current-process)
    (setq j2e-file-name partial-file)
    (add-hook 'after-save-hook 'j2e-after-save nil t)
    (goto-line line)
    (forward-char column)
    ;; too bad this requires the preceding hack on GTK
    (let* ((window (get-buffer-window obuf t))
           (frame (if window
                      (window-frame window)
                    (progn (message "No window") nil))))
      (cond (frame (raise-frame frame))
            (t (message "No Frame"))))
    ))

;; This command is sent by J2E when a buffer we have should be put into
;; J2E mode.  It actually sends a list of (possibly relative) filenames
;; and the project's root directory.
(defun j2e-mark-for-project (directory file-list)
  (save-excursion
    (let (buffer
	  file)
      (while file-list
	(setq file (expand-file-name (car file-list) directory))
	(setq buffer (get-file-buffer file))
	(if buffer
	    (progn
	      (set-buffer buffer)
	      (if (not j2e-minor-mode)
		  (progn
		    (setq j2e-minor-mode t)
		    (setq j2e-process j2e-current-process)))))
	(setq file-list (cdr file-list))))))

;;;###autoload
(define-compilation-mode j2e-comp-mode "j2e-comp"
  "Does nothing."
  ;;(set (make-local-variable 'tool-bar-map) j2e-mode-tool-bar-map)
  (set (make-local-variable 'compilation-error-regexp-alist)
       j2e-comp-error-regexp-alist)
  (set (make-local-variable 'compilation-process-setup-function) 'nil)
  (set (make-local-variable 'compilation-disable-input) t)
  (set (make-local-variable 'compilation-mode-map) j2e-comp-mode-map)
  (setq compilation-skip-threshold 2))


(provide 'j2e)
