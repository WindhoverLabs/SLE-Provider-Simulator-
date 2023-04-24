# YAMCS SLE Provider Simulator

Open a shell for the Provider Simulator and another shell for the YAMCS-SLE client. Launch the Provider Simulator first and then the YAMCS-SLE client. 

To launch YAMCS using sitl_commander_workspace, you must first make the workspace following the instructions from airliner/README.md

1. Provider simulator shell

 ./bin/yamcs-start /opt/yamcs .
 
 2. YAMCS-SLE client
 
 ./bin/yamcs-start /opt/yamcs .

This should launch YAMCS from the sitl_commander_workspace directory. 

When YAMCS is lauched, sle-client will be in READY state. To go to ACTIVE state navigate to "more actions" and select "Start SLE". 

This project has Spotbug maven plugin to be used as a source analysis tool. To launch Spotbug, type this command: mvn spotbugs:gui
