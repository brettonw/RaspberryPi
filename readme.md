# Raspberry Pi

This is my exploration in using the Raspberry Pi 3. I'm using 4 Pis configured in a small cluster, and
a host of accessories to do things like run motors and experiment with external controls. I am new to 
the world of external electronics, and make no representation of correctness or accuracy in any of this,
but it's what is working for me.

![Picture of my cluster](https://brettonw.github.io/RaspberryPi/img/IMG_6875.jpg =200)

## Cluster
I watched a lot of YouTube videos by people who made Raspberry Pi clusters, including some with 100s
of nodes. Ultimately, the Pi is not a high performance computer, so the reason to do this is to have
cheap distribution of low complexity tasks. It's a good way to experiment with the tools used to
manage a "real" cluster, like Hadoop, Spark, Chef, Puppet, etc.

I'm flexible on my programming languages, but I wanted to be able to build tools that integrate with 
some of my other work, so Java is my preferred platform in this project.

### Parts List
- [CanaKit Raspberry Pi 3 Ultimate Starter Kit](http://a.co/5KxmgZ5)
- [TRENDnet Gigabit Switch](http://a.co/fVxfpCf)
- [60W 12A 6-Port USB Charger](http://a.co/4HqDjPS)
- [Raspberry Pi 3 Model B](http://a.co/f4Y2Um9) (3x)
- [SanDisk Ultra 16GB Micro SDHC (Class 10)](http://a.co/8HrXGRo) (3x) 
- [Heatsink Set for Raspberry Pi 3](http://a.co/1tCzLk9)
- [USB 2.0 A Type Male to 5.5 x 2.5mm DC 5V Power Plug](http://a.co/h3zVyUc) (powers the switch from the USB Charger so I don't need a separate power plug)
- [10-Pack 1ft Micro USB](http://a.co/0Btpvuz)
- [5-Pack 1ft Cat6 Ethernet](http://a.co/cUOpwLt)
- [M2.5 Brass Spacer Standoff Kit](http://a.co/bAOlq5F) (2x)
- [Cooling Fan](http://a.co/e57TnVs) (2x - but I haven't added these to the build yet)

## Electronics
One of my reasons for doing this project was to learn how to control external components from a 
micro-motorController. I've always been interested in robotics, and quadcopters, so maybe this will be my 
first foray into those worlds... To do that, I had to add a few extra parts...

### Software
I'm using the [Pi4J library](http://pi4j.com) as a starting point. So far, the only caveat has been 
that I need to use a snapshot version due to some compatibility issues, but maybe that will turn 
into a release soon.

I'm also using [Olivier LeDiouris's raspberry pi](https://github.com/OlivierLD/raspberry-pi4j-samples) 
library for reference. It includes a lot of adaptations of the Adafruit Python code into Java which 
is useful when working with the Adafruit breakout boards (like the motor hat or servo driver).

My goal is to re-write the low level compoenents into my own design idioms as a means to understand
what the parts are doing.

#### Reference
- [pinout diagram](http://pi4j.com/images/gpio-control-example-large.png)

### Parts List
- [Adafruit DC & Stepper Motor HAT for Raspberry Pi](http://a.co/3hptr0h)
- [5-Pack 2x20 Extra Tall Stacking Header](http://a.co/cuu8YUX)
- [4 DC motors (Brushed)](http://a.co/3F5tUUR)
- [20pcs Propellers](http://a.co/1wKPzRs)
- [5-Pack NEMA-17 1.8 degree Stepper Motors](http://a.co/0CFBGUn)
- [Adafruit 16-Channel 12-bit PWM/Servo Driver](http://a.co/1lVjtwz)
- [10-Pack 9g SG90 Mini Servos](http://a.co/eOHpByI)
- [2.5-Amp 12-Volt Power Supply](http://a.co/29hax0E) (for motors)
- [6-Pack Buck Converters](http://a.co/5OdkDqP) (for stepping 12V down to 3.5V, 3.7V, or 5V)
- [6-Pack Mini Digital DC Voltmeter](http://a.co/3Yc5Pyb)
- etc...

### Tools List
- [PanaVise Miniature Vise](http://a.co/7KfzNKG)
- [Hakko FX888D-23BY Digital Soldering Station](http://a.co/4iqPetU)
- [Hakko T18BL Conical Soldering Tip](http://a.co/9tItB7x)

## Setup
### OPTION 1
- from an already configured raspberry pi, use the sd card copier app from the desktop ([https://www.raspberrypi.org/forums/viewtopic.php?t=206661](won't work from command line) ) with the unique id's option checked.
- put the duplicated card into the new raspberry pi and boot.
- from raspi-config (or preferences->configuration on the desktop), give the new raspberry pi a unique hostname on the network.

### OPTION 2
- from NOOBS, setup raspberry pi as a new installation and reboot.
- from raspi-config, give it a unique hostname on the network.
- from raspi-config, enable ssh.
- from the brettonw/RaspberryPi/config directory, run `setup-raspberry-pi.sh <hostname>`.

#### it will...
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

## Notes
- H264 playback seems to be supported, even in full screen, in the chromium browser. It is heavily affected by the network interface, and the amount of memory dedicated to the GPU.
- The Wifi only connects to the lower pulseFrequency transmitters.
- I use sshpass to do ssh logins from the command line in the setup script. This is not a popular tool - brew doesn't install it on the mac. I don't exactly appreciate having other people make my decisions for me, so I built it and installed it myself - the source was pulled from sourceforge.
- to do a "factory reset", you use NOOBS (New Out Of Box Software) - but you have to wait until the raspberry shows on the screen to press the shift key to launch NOOBS.

### Steps to use the WebGL capability
- update the chromium launch command to remove the "disable GPU compositing" flag
- update the raspi-config->advanced Options to use the Open GL driver (full or fake KMS), and set the memory split to 256
  NOTE: doing this causes video playback to fail on the device. The software is still very much in development.
  
#### CONCLUSION
it... "works", but is not currently viable for GPU-intensive tasks.
