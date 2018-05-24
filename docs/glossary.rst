Glossary
========

.. glossary::

   FileTreeView
     Represents the folder (and its subfolders) containing the ``.rudi`` files.


   RuleModel
     Contains all the currently used modules and rules of the project. After compiling the ``.rudi`` code, this structure is saved in a file called ``RuleLoc.yml``.


   RuleTreeView
     A ``TreeView`` representing the hierarchical structure of the *RuleModel*.


   RuleLoggingState
     The *ruleLoggingState* describes under what circumstances a rule is being logged.


   RuleModelState
      The hierarchical structure reflecting the structure of the modules and their rules is shown in the *ruleTreeview*. The current look of this ``TreeView`` (its expansion state and the *ruleLoggingStates* of the contained rules is called *RuleModel state*. This state can be saved and reloaded.

