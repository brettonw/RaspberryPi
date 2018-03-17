# Raspberry Pi

## Steps to set up a raspberry pi 3 for use with this project
### OPTION 1
- from an already configured raspberry pi, use the sd card copier app from the desktop ([https://www.raspberrypi.org/forums/viewtopic.php?t=206661](won't work from command line) ) with the unique id's option checked.
- put the duplicated card into the new raspberry pi and boot
- from raspi-config (or preferences->configuration on the desktop), give the new raspberry pi a unique hostname on the network

### OPTION 2
- from NOOBS, setup raspberry pi as a new installation and reboot
- from raspi-config, give it a unique hostname on the network
- from raspi-config, enable ssh
- from the config directory, run `setup-raspberry-pi.sh <hostname>`

### it will...
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
- update the raspi-config->advanced Options to use the Open GL driver (full or fake KMS), and set the memory split to 256
  NOTE: doing this causes video playback to fail on the device. The software is still very much in development.

# Notes
- H264 playback seems to be supported, even in full screen, in the chromium browser. It is heavily affected by the network interface, and the amount of memory dedicated to the GPU.
- The Wifi only connects to the lower frequency transmitters.
- I use sshpass to do ssh logins from the command line in the setup script. This is not a popular tool - brew doesn't install it on the mac. I don't exactly appreciate having other people make my decisions for me, so I built it and installed it myself - the source was pulled from sourceforge.
- to do a "factory reset", you use NOOBS (New Out Of Box Software) - but you have to wait until the raspberry shows on the screen to press the shift key to launch NOOBS.

# Pi4J
- pinout diagram: http://pi4j.com/images/gpio-control-example-large.png
