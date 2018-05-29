# rudibugger

**rudibugger** is the GUI of [**VOnDA**](https://github.com/bkiefer/vonda) (**V**ersatile **On**tology-based **D**ialogue **A**rchitecture), a framework to implement the dialogue management functionality in dialogue systems.

Its purpose is to help navigating, compiling and editing source code that is used by a **VOnDA**-based dialogue system. Furthermore, it offers debugging functionality by allowing the user to track what is happening in a running system. 

A video demonstrating rudibugger can be found [here](https://youtu.be/nSotEVZUEyw).



## Documentation

Installation instructions, how to run rudibugger and information about its features can be found in the (not yet complete) [documentation](http://rudibugger.readthedocs.io/en/latest/index.html).

A short quick guide on how to start rudibugger is written below.


## How to run rudibugger (quick guide)

### Start rudibugger

In the root folder of **rudibugger**, two scripts are included:

  - `runRudibugger.sh` runs rudibugger and prints its own logging statements on the running shell. (e.g. `rudibugger has been started.`).
  - `run.sh` does not print these statements. 

### Connect to VOnDA

Given a **VOnDA**-based project on the same machine, one must only open the project in **rudibugger** by selecting the project's configuration file. If the system is running, one can connect to **VOnDA** by clicking the connect button in the upper left. 



## Origin
**rudibugger** was originally written in the context of a Bachelor's Thesis by Christophe Biwer [(cbiwer@coli.uni-saarland.de)](mailto:cbiwer@coli.uni-saarland.de) under the supervision of Dipl.-Inf. Bernd Kiefer and Prof. Dr. Josef van Genabith. 

The Thesis with the title *rudibugger - Graphisches Debugging der Dialogmanagementtechnologie VOnDA* can be found [here](http://doi.org/10.13140/RG.2.2.36556.31368).
