# YAMCS SLE Provider Simulator

Open a shell for the Provider Simulator and another shell for the YAMCS-SLE client. Launch the Proider Simulator first and then the YAMCS-SLE client. 

1. mvn exec:java

This should launch the provider simulation from the Provider Simulator directory . Working on making the Povider Simulator a YAMCS plugin.

2. To launch YAMCS using sitl_commander_workspace, you must first make the workspace following the instructions from airliner/README.md

3. ./bin/yamcs-start /opt/yamcs .

This should launch YAMCS from the sitl_commander_workspace directory. 

4. When YAMCS is lauched, sle-client will be in READY state. To go to ACTIVE state navigate to "more actions" and select Start SLE. 

