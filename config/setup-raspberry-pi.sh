#!/usr/bin/env bash

# setup raspberry pi, enable wifi or wired networking
# give it a unique hostname on the network

# get the name of the machine we are logging into
RASPBERRY_PI=$1;
if [[ -z  $RASPBERRY_PI  ]]; then
    echo "Usage: $0 hostname <pi-password>";
    exit;
fi

# try to see if the machine is reachable
PING_RASPBERRY_PI="$(ping -o $RASPBERRY_PI 2>&1)";
echo $PING_RASPBERRY_PI | grep "56 data bytes" &> /dev/null;
if [ $? != 0 ]; then
    echo "Cannot connect to $RASPBERRY_PI";
    exit;
fi

# function to check that login works
checkLogin() {
    local LOGIN_USER=$1;
    local LOGIN_PASSWORD=$2;

    echo "Logging into $LOGIN_USER@$RASPBERRY_PI...";
    local LOGIN_RESPONSE="$(sshpass -p $LOGIN_PASSWORD ssh $LOGIN_USER@$RASPBERRY_PI printenv 2>&1)";
    echo $LOGIN_RESPONSE | grep "USER=$LOGIN_USER" &> /dev/null;
    if [ $? == 0 ]; then
        return 0;
    else
        echo "...Failed";
    fi
    return 1;
}

# set up a few defaults
RASPBERRY_PI_USER="pi";
RASPBERRY_PI_USER_PASSWORD=$2;
# if the user didn't supply a password, we want to try to login with the defaults
if [[ -z  $RASPBERRY_PI_USER_PASSWORD  ]]; then
    RASPBERRY_PI_USER_PASSWORD="raspberry";

    checkLogin $RASPBERRY_PI_USER $RASPBERRY_PI_USER_PASSWORD;
    #echo "DEFAULT_LOGIN_RESPONSE=$DEFAULT_LOGIN_RESPONSE";
    if [ $? == 0 ]; then
        echo "Logged into $RASPBERRY_PI_USER@$RASPBERRY_PI with default credentials. We will now change the password to secure the box.";
        NEW_RASPBERRY_PI_USER_PASSWORD="";
        while [[ -z $NEW_RASPBERRY_PI_USER_PASSWORD ]]; do
            echo "What would you like the new password to be?";
            read NEW_RASPBERRY_PI_USER_PASSWORD;
        done
        # actually set the password
        sshpass -p ${RASPBERRY_PI_USER_PASSWORD} ssh $RASPBERRY_PI_USER@$RASPBERRY_PI "echo $RASPBERRY_PI_USER:$NEW_RASPBERRY_PI_USER_PASSWORD | sudo chpasswd" 2>&1
        # TODO might be nice to confirm the change worked
        RASPBERRY_PI_USER_PASSWORD=$NEW_RASPBERRY_PI_USER_PASSWORD;
    else
        RASPBERRY_PI_USER_PASSWORD="";
    fi
fi

# function to check login with specific global variables
checkLogin2() {
    if [[ ! -z $RASPBERRY_PI_USER_PASSWORD ]]; then
        checkLogin $RASPBERRY_PI_USER $RASPBERRY_PI_USER_PASSWORD;
        if [ $? != 0 ]; then
            RASPBERRY_PI_USER_PASSWORD="";
        fi
    fi
}

checkLogin2;
while [[ -z $RASPBERRY_PI_USER_PASSWORD ]]; do
    echo "What is the password for $RASPBERRY_PI_USER@$RASPBERRY_PI? ";
    read RASPBERRY_PI_USER_PASSWORD;
    checkLogin2;
done

# get a password to use for the new account
echo "The next set of operations will create an account for $USER@$RASPBERRY_PI, if one doesn't exist.";
echo "What is the password for this new user?";
echo "(If you enter a blank password, a random one will be created)";
read USER_PASSWORD;
if [[ -z $USER_PASSWORD ]]; then
    # if the user doesn't want to give one, we can just make one up - they will use ssh with
    # certs when we are done anyway, and they have access to the default user password with
    # sudo rights if they need to reset it
    OLD_LC_CTYPE=$LC_CTYPE
    export LC_CTYPE=C
    USER_PASSWORD=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 16 | head -n 1)
    export LC_CTYPE=$OLD_LC_CTYPE
    echo "Created password for $USER ($USER_PASSWORD), make note of this for your records.";
fi

# try to login with the supplied credentials
checkLogin $USER $USER_PASSWORD
if [ $? != 0 ]; then
    echo "Creating new account...";
    # if that fails, create a new user and set the password
    sshpass -p ${RASPBERRY_PI_USER_PASSWORD} ssh $RASPBERRY_PI_USER@$RASPBERRY_PI "sudo adduser $USER" 2>&1
    sshpass -p ${RASPBERRY_PI_USER_PASSWORD} ssh $RASPBERRY_PI_USER@$RASPBERRY_PI "echo $USER:$USER_PASSWORD | sudo chpasswd" 2>&1

    # update sudoers so <me> can sudo without passwords
    sshpass -p ${RASPBERRY_PI_USER_PASSWORD} ssh $RASPBERRY_PI_USER@$RASPBERRY_PI "sudo echo $USER ALL=(ALL) NOPASSWD: ALL >> /etc/sudoers" 2>&1

else
    echo "Using existing account...";
fi

# from now on, we don't need to specify the user to the ssh commands, they will default to <me>

# if authorized keys doesn't exist
sshpass -p $USER_PASSWORD ssh $RASPBERRY_PI "test -e ~/.ssh/authorized_keys";
if [ $? != 0 ]; then
    # copy the identity so I can login quietly from now on - we copy the keys too, so we can
    # use git quietly
    echo "Installing certs...";
    sshpass -p $USER_PASSWORD ssh $RASPBERRY_PI mkdir -p -m 700 ~/.ssh;
    sshpass -p $USER_PASSWORD scp ~/.ssh/id_rsa.pub $RASPBERRY_PI:~/.ssh/authorized_keys;
    sshpass -p $USER_PASSWORD scp ~/.ssh/id_rsa $RASPBERRY_PI:~/.ssh/;
    sshpass -p $USER_PASSWORD scp ~/.ssh/id_rsa.pub $RASPBERRY_PI:~/.ssh/;
else
    echo "Using existing certs...";
fi

# from now on, should be able to do operations without sshpass

# copy bashrc from ./config to /home/<me>/.bashrc
scp ./bashrc $RASPBERRY_PI:~/.bashrc
ssh $RASPBERRY_PI "echo > .hushlogin"

# setup the remote environment
# "install" a recent jdk8 - I put this in my /home/<me>/bin folder so as not to interfere with any other configurations
# "install" maven - also in the /home/<me>/bin folder
ssh $RASPBERRY_PI mkdir -p ~/bin
scp ./bin/jdk-8u162-linux-arm32-vfp-hflt.tar.gz $RASPBERRY_PI:~/bin/
scp ./bin/apache-maven-3.5.3-bin.tar.gz $RASPBERRY_PI:~/bin/
ssh $RASPBERRY_PI cd bin && tar xvzf jdk-8u162-linux-arm32-vfp-hflt.tar.gz
ssh $RASPBERRY_PI cd bin && ln -s jdk-8u162-linux-arm32-vfp-hflt jdk8
ssh $RASPBERRY_PI cd bin && tar xvzf apache-maven-3.5.3-bin.tar.gz
ssh $RASPBERRY_PI cd bin && ln -s apache-maven-3.5.3-bin apache-maven

# create m2 folder - /home/<me>/m2
# copy settings.xml from ./config to /home/<me>/m2
ssh $RASPBERRY_PI mkdir -p ~/m2
scp ./maven-settings.xml $RASPBERRY_PI:~/m2/settings.xml

# force update on all software packages (sudo apt-get update && sudo apt-get upgrade)
ssh $RASPBERRY_PI "sudo apt-get update && sudo apt-get dist-upgrade"

# git clone repository
ssh $RASPBERRY_PI mkdir -p ~/work
ssh $RASPBERRY_PI "cd work && git clone git@github.com:brettonw/RaspberryPi.git"

