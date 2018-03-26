package com.brettonw.PCA9685;

// Servo Driver Board
// https://learn.adafruit.com/16-channel-pwm-servo-driver/overview
public class AdafruitServoDriver extends PCA9685 {

    // default i2c address of the Adafruit server hat
    public static final int DEFAULT_ADDRESS = 0x40;

    public AdafruitServoDriver () {
        this (DEFAULT_ADDRESS);
    }

    public AdafruitServoDriver (int address) {
        super (address);
    }

    public AdafruitServoDriver setPosition (int servoId, double position) {
        setChannelPulse (servoId, 0, (int) Math.round (position * CHANNEL_HIGH));
        return this;
    }
}
