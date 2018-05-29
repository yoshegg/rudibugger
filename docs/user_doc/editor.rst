Editor
======

**rudibugger** has an integrated editor based on *RichTextFX*. (For more information about this dependency, take a look at its `github page <https://github.com/FXMisc/RichTextFX/>`_.)

It is also possible to use an external editor. Right now, a well working *Emacs* integration is implemented. It can be activated under :menuselection:`Tools --> Options --> Default editor`. At the same place, you can also set up your own editor. Cf. section :ref:`Settings<Settings>` for more information.


Tabs and CodeArea
-----------------

.. note:: The following only applies if you're using rudibugger as editor.

When opening a file or a rule, it will be opened in a new tab in the editor part of **rudibugger**. If the tab is already open, it will be switched to. 

Syntax highlighting is supported via *RichTextFX*. It might be buggy, though. 

.. danger:: Right now, there is no *ask-for-confirmation* dialog to save the content of a modified file that should be closed.



Search in project
-----------------

**rudibugger** includes a full-text search, that searches all of the project's ``.rudi`` files. It can be found under :menuselection:`Edit --> Find in project...`. Alternatively you can use the shortcut :kbd:`Ctrl` + :kbd:`Shift` + :kbd:`F`.

