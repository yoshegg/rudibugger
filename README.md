# rudibugger

**rudibugger** is the GUI of [**VOnDA**](https://github.com/bkiefer/vonda) (**V**ersatile **On**tology-based **D**ialogue **A**rchitecture), a framework to implement the dialogue management functionality in dialogue systems.

Its purpose is to help navigating, compiling and editing source code that is used by a **VOnDA** based dialogue system. Furthermore it offers debugging functionality by allowing the user to track what is happening in a running system. 

A video demonstrating rudibugger can be found [here](https://youtu.be/nSotEVZUEyw).



## Installation

**rudibugger** is a maven-based project. It is mainly based on JavaFX, which might not be installed on your system if you're using OpenJDK.

If you are using an Ubuntu-based linux, you can install it using

```sudo apt install openjfx```

*missing: detailed installation instructions*


### Prerequisites

It is mandatory to install [**VOnDA**](https://github.com/bkiefer/vonda) first, as it contains most of the dependencies needed to build **rudibugger**.

*missing: further requisites*


### Installation process

```
git clone https://github.com/yoshegg/rudibugger
cd rudibugger
mvn install
```


## How to run rudibugger

### Start rudibugger

In the root folder of **rudibugger**, two scripts are included:

  - `runRudibugger.sh` runs rudibugger and prints its own logging statements on the running shell. (e.g. `rudibugger has been started.`)
  - `run.sh` does not print these statements. 


### Connect to VOnDA

Given a **VOnDA**-based project on the same machine, one must only open the project in **rudibugger** by selecting the project's configuration file. If the system is running, one can connect to VOnDA by clicking the connect button in the upper left. 


## Origin
**rudibugger** was originally written in the context of a Bachelor's Thesis by Christophe Biwer [(cbiwer@coli.uni-saarland.de)](mailto:cbiwer@coli.uni-saarland.de) under the supervision of Dipl.-Inf. Bernd Kiefer and Prof. Dr. Josef van Genabith. 

The Thesis with the name *rudibugger - Graphisches Debugging der Dialogmanagementtechnologie VOnDA* can be found [here](http://doi.org/10.13140/RG.2.2.36556.31368).
