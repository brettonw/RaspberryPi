package com.brettonw.PCA9685;

/**
 * Servo Driver Board
 *
 * https://learn.adafruit.com/16-channel-pwm-servo-driver/overview
 *
 * this breakout board is a straightforward implementation of a 9685 16-Channel Pulse Width
 * Modulation Controller (PWM) for LEDs with 12-bits of resolution. We use it to provide a bunch of
 * PWM outputs for servos.
 *
 */
public class AdafruitServoDriver extends PCA9685 implements ServoController {

    // default i2c address of the Adafruit server hat
    public static final int DEFAULT_ADDRESS = 0x40;
    private final static int DEFAULT_PULSE_FREQUENCY = 50;

    public AdafruitServoDriver () {
        this (DEFAULT_ADDRESS);
    }

    public AdafruitServoDriver (int address) {
        super (address, DEFAULT_PULSE_FREQUENCY);
    }

    /**
     * set the pulse width to control a servo. the exact meaning of this is up to the servo itself.
     * @param servoId - which servo to set the pulse duration for
     * @param milliseconds - the width of the puls in milliseconds
     * @return this, for chaining
     */
    public void setPulseDuration (int servoId, double milliseconds) {
        setChannelPulseMs (servoId, milliseconds);
    }
}
