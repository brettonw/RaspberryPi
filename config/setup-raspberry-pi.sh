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
else
    echo "Found $RASPBERRY_PI";
fi

# check that we have sshpass
SSHPASS_PATH=$(which sshpass)
#echo "sshpass @ $SSHPASS_PATH";
if [ ! -x "$SSHPASS_PATH" ]; then
    echo "sshpass not found";
    exit;
fi

# function to check that login works
checkLogin() {
    local LOGIN_USER=$1;
    local LOGIN_PASSWORD=$2;
    local LOGIN_MESSAGE=$3;

    echo "Logging into $LOGIN_USER@$RASPBERRY_PI ($LOGIN_MESSAGE)...";
    local LOGIN_RESPONSE="$(sshpass -p $LOGIN_PASSWORD ssh -o StrictHostKeyChecking=no $LOGIN_USER@$RASPBERRY_PI printenv 2>&1)";
    echo $LOGIN_RESPONSE | grep "USER=$LOGIN_USER" &> /dev/null;
    if [ $? == 0 ]; then
        return 0;
    else
        #echo "...Failed";
        echo "Login Response: $LOGIN_RESPONSE";
    fi
    return 1;
}

# set up a few defaults
RASPBERRY_PI_USER="pi";
RASPBERRY_PI_USER_PASSWORD="raspberry";

# we want to try to login with the defaults
checkLogin $RASPBERRY_PI_USER $RASPBERRY_PI_USER_PASSWORD "with default credentials";
if [ $? == 0 ]; then
    # get a new password for the box
    echo "Change the default password to secure the box.";
    NEW_RASPBERRY_PI_USER_PASSWORD=$2;
    while [[ -z $NEW_RASPBERRY_PI_USER_PASSWORD ]]; do
        echo "What would you like the new password to be?";
        read NEW_RASPBERRY_PI_USER_PASSWORD;
    done

    # actually set the password
    echo "Changing $RASPBERRY_PI_USER password to ($NEW_RASPBERRY_PI_USER_PASSWORD)";
    sshpass -p ${RASPBERRY_PI_USER_PASSWORD} ssh $RASPBERRY_PI_USER@$RASPBERRY_PI "echo $RASPBERRY_PI_USER:$NEW_RASPBERRY_PI_USER_PASSWORD | sudo chpasswd" 2>&1
    RASPBERRY_PI_USER_PASSWORD=$NEW_RASPBERRY_PI_USER_PASSWORD;
else
    RASPBERRY_PI_USER_PASSWORD=$2;
    #echo "Using $RASPBERRY_PI_USER password ($RASPBERRY_PI_USER_PASSWORD)";
fi

# function to check login with specific global variables
checkLogin2() {
    local LOGIN_MESSAGE=$1;
    if [[ ! -z $RASPBERRY_PI_USER_PASSWORD ]]; then
        checkLogin $RASPBERRY_PI_USER $RASPBERRY_PI_USER_PASSWORD $LOGIN_MESSAGE;
        if [ $? != 0 ]; then
            RASPBERRY_PI_USER_PASSWORD="";
        fi
    fi
}

checkLogin2 "$RASPBERRY_PI_USER_PASSWORD";
while [[ -z $RASPBERRY_PI_USER_PASSWORD ]]; do
    echo "What is the password for $RASPBERRY_PI_USER@$RASPBERRY_PI? ";
    read RASPBERRY_PI_USER_PASSWORD;
    checkLogin2 "retry";
done

# get a password to use for the new account
echo "Creating account for $USER@$RASPBERRY_PI...";
#echo "What is the password for this new user?";
#echo "(If you enter a blank password, a random one will be created)";
#read USER_PASSWORD;
USER_PASSWORD="";
if [[ -z $USER_PASSWORD ]]; then
    # if the user doesn't want to give one, we can just make one up - they will use ssh with
    # certs when we are done anyway, and they have access to the default user password with
    # sudo rights if they need to reset it
    OLD_LC_ALL="$LC_ALL";
    export LC_ALL=C;
    USER_PASSWORD=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 16 | head -n 1);
    export LC_ALL="$OLD_LC_ALL";
    echo "Created password for $USER ($USER_PASSWORD).";
fi

# try to login with the supplied credentials
checkLogin $USER $USER_PASSWORD "new user"
if [ $? != 0 ]; then
    # if that fails, create a new user and set the password
    echo "Creating new account...";
    sshpass -p ${RASPBERRY_PI_USER_PASSWORD} ssh $RASPBERRY_PI_USER@$RASPBERRY_PI "sudo adduser $USER  --gecos 'First Last,RoomNumber,WorkPhone,HomePhone' --disabled-password" 2>&1
    sshpass -p ${RASPBERRY_PI_USER_PASSWORD} ssh $RASPBERRY_PI_USER@$RASPBERRY_PI "echo $USER:$USER_PASSWORD | sudo chpasswd" 2>&1

    # update sudoers so <me> can sudo without passwords
    echo "Checking for $USER in /etc/sudoers...";
    sshpass -p ${RASPBERRY_PI_USER_PASSWORD} ssh $RASPBERRY_PI_USER@$RASPBERRY_PI "sudo grep $USER /etc/sudoers" 2>&1
    if [ $? != 0 ]; then
        echo "Adding $USER to /etc/sudoers...";
        sshpass -p ${RASPBERRY_PI_USER_PASSWORD} ssh $RASPBERRY_PI_USER@$RASPBERRY_PI "echo \"$USER ALL=(ALL) NOPASSWD: ALL\" | sudo tee -a /etc/sudoers" 2>&1;
    fi
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
    sshpass -p $USER_PASSWORD ssh $RASPBERRY_PI "mkdir -p -m 700 .ssh";
    echo "  ...authorized keys";
    sshpass -p $USER_PASSWORD scp ~/.ssh/id_rsa.pub $RASPBERRY_PI:.ssh/authorized_keys;
    echo "  ...id";
    sshpass -p $USER_PASSWORD scp ~/.ssh/id_rsa $RASPBERRY_PI:.ssh/;
    echo "  ...pub";
    sshpass -p $USER_PASSWORD scp ~/.ssh/id_rsa.pub $RASPBERRY_PI:.ssh/;
    echo "  ...known hosts";
    sshpass -p $USER_PASSWORD scp ./known_hosts $RASPBERRY_PI:.ssh/;
    echo "Done installing certs.";
else
    echo "Using existing certs...";
fi

# from now on, should be able to do operations without sshpass

# force update on all software packages (sudo apt-get update && sudo apt-get upgrade)
echo "Update raspberry pi...";
ssh $RASPBERRY_PI "sudo apt-get update --yes && sudo apt-get dist-upgrade --yes"

echo "Install git...";
ssh $RASPBERRY_PI "sudo apt-get install git --yes"

# copy bashrc from ./config to /home/<me>/.bashrc
echo "Configuring home...";
scp ./bashrc $RASPBERRY_PI:~/.bashrc
ssh $RASPBERRY_PI "echo > .hushlogin"

# setup the remote environment
# "install" a recent jdk8 - I put this in my /home/<me>/bin folder so as not to interfere with any other configurations
# "install" maven - also in the /home/<me>/bin folder
echo "Configuring bin...";
ssh $RASPBERRY_PI mkdir -p bin
echo "  ...java";
scp ./bin/jdk-8u162-linux-arm32-vfp-hflt.tar.gz $RASPBERRY_PI:bin
ssh $RASPBERRY_PI "cd bin; tar xvzf jdk-8u162-linux-arm32-vfp-hflt.tar.gz;"
ssh $RASPBERRY_PI "cd bin; ln -s jdk1.8.0_162 jdk8;"
ssh $RASPBERRY_PI "cd bin; rm -f jdk-8u162-linux-arm32-vfp-hflt.tar.gz;"

echo "  ...maven";
scp ./bin/apache-maven-3.5.3-bin.tar.gz $RASPBERRY_PI:bin
ssh $RASPBERRY_PI "cd bin; tar xvzf apache-maven-3.5.3-bin.tar.gz;"
ssh $RASPBERRY_PI "cd bin; ln -s apache-maven-3.5.3 apache-maven;"
ssh $RASPBERRY_PI "cd bin; rm -f apache-maven-3.5.3-bin.tar.gz;"

# create m2 folder - /home/<me>/m2
# copy settings.xml from ./config to /home/<me>/m2
echo "Configuring maven...";
ssh $RASPBERRY_PI mkdir -p m2
scp ./maven-settings.xml $RASPBERRY_PI:m2/settings.xml

# git clone repository
echo "Clone and test...";
ssh $RASPBERRY_PI mkdir -p work
ssh $RASPBERRY_PI "cd work; git clone git@github.com:brettonw/RaspberryPi.git; cd RaspberryPi; mvn clean test;"

