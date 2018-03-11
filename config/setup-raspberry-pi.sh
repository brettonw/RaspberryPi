#!/usr/bin/env bash

# setup raspberry pi, enable wifi or wired networking
# give it a unique hostname on the network

# get the name of the machine we are logging into
RASPBERRY_PI=$1;
if [[ -z  $RASPBERRY_PI  ]]; then
    echo "Usage: $0 <hostname> <password>";
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

# get a password to use for the new account - if the user doesn't want to give one, we can
# just make one up - they will use ssh with certs when we are done anyway, and they
# (presumably) have access to the default user password with sudo rights if they need to
# reset it


# create user on raspberry pi
echo "NEXT";

# update sudoers so <me> can sudo without passwords

# login as user <me>
# copy ssh config so I can login quietly
# copy bashrc from ./config to /home/<me>/.bashrc
# echo > .hushlogin
# "install" a recent jdk8 - I put this in my /home/<me>/bin folder so as not to interfere with any other configurations
# "install" maven - also in the /home/<me>/bin folder
# create m2 folder - /home/<me>/m2
# copy settings.xml from ./config to /home/<me>/m2
# git clone repository
