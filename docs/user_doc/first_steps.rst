First steps
============

As rudibugger is a debugger for **VOnDA** projects, one must open a project first. 


Open a project
--------------

To open a project, go to :menuselection:`File --> Open project...`. *(Alternatively, you can use the shortcut* :kbd:`Ctrl+Shift+O` *.)*

In the appearing file dialogue, you must choose a ``.yml`` file meeting the minimum requirements of rudibugger. This configuration file will be called **$CONFIG_YAML** from here on. 

.. warning:: 
    A `.yml` file must (at least) have the following entries:
      * ``outputDirectory``
      * ``wrapperClass``
      * ``ontologyFile``
      * ``rootPackage``

.. note:: For more information on how a **VOnDA** project must be structured, cf. **VOnDA**'s `github page <https://github.com/bkiefer/vonda/>`_.



Recent projects
---------------

If you want to open a project that you already opened with **rudibugger** once, you can use the according menu *Open recent project*. The according list is sorted by the time of last opening.

.. hint:: The list of recent projects is saved under ``~/.config/rudibugger/recentProjects.yml``.


.. tip:: If you close **rudibugger** while having a project open, the project will be reloaded when you start **rudibugger** the next time. 


 
