# Raspberry Pi

## Steps to set up a raspberry pi 3 for use with this project

- setup raspberry pi, enable wifi or wired networking
- give it a unique hostname on the network
- create user on raspberry pi (<me>)
- update sudoers so <me> can sudo without passwords
- login as user <me>
- copy ssh config so I can login quietly
- copy bashrc from ./config to /home/<me>/.bashrc
- echo > .hushlogin
- force update on all software packages (sudo apt-get update && sudo apt-get upgrade)
- "install" a recent jdk8 - I put this in my /home/<me>/bin folder so as not to interfere with any other configurations
- "install" maven - also in the /home/<me>/bin folder
- create m2 folder - /home/<me>/m2
- copy settings.xml from ./config to /home/<me>/m2
- git clone repository

# Steps to use the WebGL capability
- update the chromium launch command to remove the "disable GPU compositing" flag
- update the raspi-config->advanced Options to use the Open GL driver (full KMS), and set the memory split to 256
