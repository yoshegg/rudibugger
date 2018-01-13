;;; cplan.el --- Major Mode for interaction with content planner

;; Copyright (C) 2010  Free Software Foundation, Inc.

;; Author: Bernd Kiefer <kiefer@dfki.de>
;; Keywords:

;; This file is free software; you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation; either version 2, or (at your option)
;; any later version.

;; This file is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.

;; You should have received a copy of the GNU General Public License
;; along with GNU Emacs; see the file COPYING.  If not, write to
;; the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
;; Boston, MA 02111-1307, USA.

;;; Commentary:

;;

;;; Code:

(defvar cplan-mode-syntax-table nil
  "Syntax for the content planner rule files")

(or cplan-mode-syntax-table
    (let ((st (make-syntax-table)))
      ;; define comment for these styles: `/* ... */' and `// ...'
      (modify-syntax-entry ?\/ ". 124b" st)
      (modify-syntax-entry ?* ". 23" st)
      (modify-syntax-entry ?\n "> b" st)

      (modify-syntax-entry ?^ "." st)
      (modify-syntax-entry ?| "." st)
      (modify-syntax-entry ?! "." st)
      (modify-syntax-entry ?, "." st)
      (modify-syntax-entry ?. "." st)
      (modify-syntax-entry ?= "." st)

      (modify-syntax-entry ?# "_" st)

      (modify-syntax-entry ?< "(>  " st)
      (modify-syntax-entry ?> ")<  " st)

      (modify-syntax-entry ?\( "()  " st)
      (modify-syntax-entry ?- "\\" st)

      (setq cplan-mode-syntax-table st)))

(setq auto-mode-alist
      (append
       auto-mode-alist
       (list '("\\.trf" . cplan-mode) '("\\.cpj" . conf-mode))))

(defun cplan-comment-dwim (arg)
  "Comment or uncomment current line or region in a smart way.
For detail, see `comment-dwim'."
  (interactive "*P")
  (require 'newcomment)
  (let ((deactivate-mark nil) (comment-start "//") (comment-end ""))
    (comment-dwim arg)))

(setq cplan-font-lock-specs
      '(("\\(->\\|\\.\\|!\\)" . font-lock-warning-face)
        ;;("//.*\n" . font-lock-comment-face)
        ;;("[|^!=]" . font-lock-keyword-face)
        ("<[a-zA-Z_-]+>" . font-lock-variable-name-face)
        ("\\([#_a-zA-Z0-9-]+:\\)\\([#_a-zA-Z0-9-]*\\)"
         . ((1 font-lock-function-name-face) (2 font-lock-type-face)))
        (":\\([#_a-zA-Z0-9-]+\\)". ((1 font-lock-type-face)))
        ))

(defun cplan-mode ()
  "Major mode for interaction with Java, esp. the CCG content planner
Adds some commands for looking up stuff:
\\{cplan-mode-map}
This mode is automatically activated when files are opened by J2E and cannot
be activated for other buffers.  You can toggle it for J2E-related buffers
though.  This lets you access the command bindings that this mode overrides."
  (interactive)
  (kill-all-local-variables)
  (setq major-mode 'cplan-mode)
  (setq mode-name "Cplan")
  ;;(use-local-map cplan-mode-map)
  (make-local-variable 'comment-start-skip)
  (setq comment-start-skip "/\\*+ *\\|//+ *")
  (make-local-variable 'comment-start)
  (setq comment-start "//")
  (make-local-variable 'comment-end)
  (setq comment-end "")

  (setq font-lock-defaults '(cplan-font-lock-specs))

  (set-syntax-table cplan-mode-syntax-table))

;; move commands : rule forward/backward

;; indentation

;; reload project file

(provide 'cplan)
;;; cplan.el ends here
