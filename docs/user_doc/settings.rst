Settings
========

Some settings can be configured in the settings window: :menuselection:`Tools --> Options`.


Default editor
--------------

One can select the default editor to open ``.rudi`` files. 

If you want to use a custom editor, you need to specify to commands that will be called upon requesting a file (or rule). The command must include ``%file`` and / or ``%rule`` that will then be replaced by the respective file name and line number. 


.. _connection-to-vonda:

Connection to VOnDA
-------------------

Show index after timestamp in logging table   
  If multiple logging events occur at the same time, they might not be distinguishable. Therefore, one can show an index alongside of the timestamp.

Automatically connect to VOnDA when opening a project
  Basically what it says.
