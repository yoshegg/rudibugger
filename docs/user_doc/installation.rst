Installation instructions
=========================

Prerequisites
-------------

VOnDA
~~~~~
rudibugger can be seen as an extension of **VOnDA**. Therefore you need to install it first, as it contains most of the dependencies needed to build rudibugger. Installation instructions can be found `here <https://github.com/bkiefer/vonda#installation>`_.

Maven
~~~~~

rudibugger is a maven-based project. On Ubuntu-based machines, you can install it with ``sudo apt install maven``.

JavaFX
~~~~~~~
rudibugger is mainly based on JavaFX. 

If you're using **Oracle's JDK**, you should be good to go. 

If you're using **OpenJDK**, you need ``openjfx`` which might not be installed on your machine. If you are using an Ubuntu-based linux, you can install it using ``sudo apt install openjfx``.



Installation
------------

If your computer meets the prerequisites, you can install rudibugger:

.. code-block:: shell

  git clone https://github.com/yoshegg/rudibugger
  cd rudibugger
  mvn install

