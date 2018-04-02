package com.brettonw.PCA9685;

import com.pi4j.io.i2c.I2CDevice;

import java.util.HashMap;
import java.util.Map;

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
    private static final Map<ServoId, Integer> servoSpecs;

    static {
        servoSpecs = new HashMap<> (ServoId.values ().length);
        servoSpecs.put (ServoId.SERVO_01, 0);
        servoSpecs.put (ServoId.SERVO_02, 1);
        servoSpecs.put (ServoId.SERVO_03, 2);
        servoSpecs.put (ServoId.SERVO_04, 3);

        servoSpecs.put (ServoId.SERVO_05, 4);
        servoSpecs.put (ServoId.SERVO_06, 5);
        servoSpecs.put (ServoId.SERVO_07, 6);
        servoSpecs.put (ServoId.SERVO_08, 7);

        servoSpecs.put (ServoId.SERVO_09, 8);
        servoSpecs.put (ServoId.SERVO_10, 9);
        servoSpecs.put (ServoId.SERVO_11, 10);
        servoSpecs.put (ServoId.SERVO_12, 11);

        servoSpecs.put (ServoId.SERVO_13, 12);
        servoSpecs.put (ServoId.SERVO_14, 13);
        servoSpecs.put (ServoId.SERVO_15, 14);
        servoSpecs.put (ServoId.SERVO_16, 15);
    }

    // default i2c address of the Adafruit server hat
    private static final int DEFAULT_ADDRESS = 0x40;
    private final static int DEFAULT_PULSE_FREQUENCY = 50;

    /**
     *
     */
    public AdafruitServoDriver () {
        this (DEFAULT_ADDRESS);
    }

    /**
     *
     * @param address
     */
    public AdafruitServoDriver (int address) {
        super (address, DEFAULT_PULSE_FREQUENCY);
    }

    /**
     *
     * @param i2CDevice
     */
    public AdafruitServoDriver (I2CDevice i2CDevice) {
        super (i2CDevice, DEFAULT_PULSE_FREQUENCY);
    }

    /**
     * set the pulse width to control a servo. the exact meaning of this is up to the servo itself.
     * @param servoId - which servo to set the pulse duration for
     * @param milliseconds - the width of the puls in milliseconds
     * @return this, for chaining
     */
    public void setPulseDuration (ServoId servoId, double milliseconds) {
        setChannelPulseMs (servoSpecs.get (servoId), milliseconds);
    }
}
