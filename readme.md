Raspberry Pi

- setup raspberry pi, enable wifi or wired networking
- give it a unique hostname on the network
- create user on raspberry pi (<me>)
- login as user <me>
- copy ssh config so I can login quietly
- copy bashrc from ./config to /home/<me>/.bashrc
- echo > .hushlogin
- update sudoers so <me> can sudo without passwords
- "install" a recent jdk8 - I put this in my /home/<me>/bin folder so as not to interfere with any other configurations
- "install" maven - also in the /home/<me>/bin folder
- create m2 folder - /home/<me>/m2
- copy settings.xml from ./config to /home/<me>/m2
- git clone repository
