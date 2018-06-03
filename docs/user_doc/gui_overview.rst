.. include:: .special.rst

General GUI overview
====================

After opening a project, some GUI elements become accessible or show project specific content. 


MenuBar's file menu
-------------------

New .rudi file
~~~~~~~~~~~~~~

You can create a new module (aka ``.rudi`` file) for the currently open project by clicking the according menu item or the shortcut :kbd:`Ctrl` + :kbd:`N`. The newly created file will be opened in your default editor (cf. section :ref:`Editor <Editor>` for more information). Before you can use it in your project, it needs to be saved first and also imported from the main module or one of its children. 

Load / Save rule logging states
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

It is possible to save the current selection of how rules are logged. (cf. :term:`RuleModelState`). Besides, it is also possible to reload a selection. 

.. tip::
   The saved file will contain information about all the rules that were ever used until this point. This is especially useful if you need to disable some rules temporary, but want to continue using them later. 


Save files / modules
~~~~~~~~~~~~~~~~~~~~

You can save a modified file by clicking the according menu or with the shortcut :kbd:`Ctrl` + :kbd:`S`.

You can also save a file / module as a new file via :menuselection:`File --> Save As...` (or the shortcut :kbd:`Ctrl` + :kbd:`Shift` + :kbd:`S`). Keep in mind that a new module must be imported first to be used in the runtime system. 

Close Project
~~~~~~~~~~~~~

Closing a project resets all GUI elements (except for tabs) and closes a potential connection to **VOnDA**. 

.. danger:: 
    Closing **does not ask for confirmation**. Besides, it does not close open files or saves them. You must save and / or close them yourself.


Toolbar
-------

Compile and run buttons
~~~~~~~~~~~~~~~~~~~~~~~

Upon loading a project, the *Compile* and the *Run* Button might become active if an according script has been found in the root directory of the chosen project. 

.. attention:: 
    The compile script must be called ``compile`` and the run file ``run.sh``. They must be executable. 


.. tip::
  A not so obvious feature is the possibility to **define additional compile commands**. These need to be defined in $CONFIG_YAML as shown in the following example:
  
  
  .. code-block:: yaml

     customCompileCommands:
       foo: java -jar ../vonda/target/vonda-compiler-1.1-SNAPSHOT.jar -c dipalCompile.yml src/main/rudi/PalAgent.rudi
       bar: ./devcompile
    

  The command *foo* demonstrates that a complete shell command can be used.
  The second command *bar* shows that a normal shell script file can also be used.

  It is also possible to use command names with white spaces.
  
  Additional compile commands are accessible via a drop-down menu of the compile button. The menu only appears if additional commands have been speicifed, though. 


VOnDA connection button
~~~~~~~~~~~~~~~~~~~~~~~

The button right of the *run* button shows the connection state to **VOnDA**'s live system. Clicking it starts a connection attempt or, if an attempt has already been started or a connection has already been established, closes the connection.

.. hint:: Take a look at the section :ref:`connection-to-vonda` if you want to automatically connect after opening a project. 


fileTreeView
------------

This ``TreeView`` represents the ``.rudi`` folder of a project and its subdirectories. 
Double clicking an entry opens the represented file in your default editor (cf. section :ref:`Editor <Editor>` for more information).

File icons 
~~~~~~~~~~

Different icons indicate the usage state of the shown file:

===========================  ============================================================
Icon                         Usage    
===========================  ============================================================  
Transparent (*light grey*)   Not used in currently compiled project  
File icon with green circle  A normal module being used in the currently compiled project  
Blue file icon               Wrapper class of the project
Orange file icon             Main module of the project
===========================  ============================================================

.. note:: For more information about the wrapper class or the main module, cf. **VOnDA**'s `github page <https://github.com/bkiefer/vonda/>`_.



Further graphical file indications
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If a file has been modified after the last compilation attempt, its background colour will change. For more information, cf. section :ref:`Sync indicator <Sync indicator>`.


ruleTreeView
------------

This ``TreeView`` shows all the rules that are included in the currently used modules. 


RuleLoggingState Icons
~~~~~~~~~~~~~~~~~~~~~~
 
The modules are represented with file icons and the rules with custom CheckBoxes. According to the rule logging state, the boxes have different colour:

==============  ===============================
Colour          Rule is...    
==============  ===============================
:grey:`Grey`    not logged at all  
:green:`Green`  logged if it evaluated to true
:red:`Red`      logged if it evaluated to false
:blue:`Blue`    always logged  
==============  ===============================


Module icons will change their colour according to the states of their children. 

.. note:: Modules that do not contain any rules are also shown, but don't contain any children in the ``TreeView``. Therefore, their icons will always be grey. 



Further graphical compilation indications
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Compilation problems will be indicated in the following way:

============  ==============  =================
Problem       Compilation...  Background colour
============  ==============  =================
Syntax error  **stops**       Dark red
Error         continues       Red
Warning       continues       Yellow
============  ==============  =================



Set a rule logging state
~~~~~~~~~~~~~~~~~~~~~~~~

There are different ways to set the logging state of a rule:

  * You can **cycle through** the different states **by clicking** on the `CheckBox`. 
  * You can set the state of a **specific rule** via context menu.
  * You can set the state of **all the children of a module** in a similar way. 
  * You can set the state of **rules that are children of a parent rule** in a similar way. 



Open a rule or a module
~~~~~~~~~~~~~~~~~~~~~~~

By *double clicking* on a rule or module you can open the selected module at the specific rule in your default editor. You can also do this via the context menu.


Inspect errors and warnings
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Modules that produced warnings or errors will be shown with a coloured ground. You can open the specific lines where the issue occurred via the context menu.



Status bar (and its indicators)
-------------------------------

The status bar contains two circles, the so-called *indicators*. 


Sync indicator
~~~~~~~~~~~~~~

The first indicates that the current ``.rudi`` code is in sync with the compiled code. This status will immediately change if a module has been changed, even if this change occurred outside of rudibugger or if rudibugger was closed during that change. 

==============  ===========
Colour          Status
==============  ===========
:green:`Green`  In-sync
:red:`Red`      Out-of-sync
:grey:`Grey`    Undefined
==============  ===========

.. note:: Undefined means that the project has never been compiled or that no project has been opened.

.. note::
  When loading a project, rudibugger checks if the timestamp of the ``.rudi`` files is superior to the timestamp of the rule structure file (``RuleLoc.yml``).



Compilation indicator
~~~~~~~~~~~~~~~~~~~~~

The outcome of a compilation attempt is represented by the colour of the second indicator. 

================  ============================================
Colour            Status
================  ============================================
:green:`Green`    Compiled without problems
:red:`Red`        **Compilation aborted** due to syntax errors
:orange:`Orange`  Compiled with errors
:yellow:`Yellow`  Compiled with warnings
:grey:`Grey`      Undefined
================  ============================================



.. note:: Undefined means that the project has never been compiled or that no project has been opened.

.. tip:: You can go to the specific lines of the problems via context menu.



