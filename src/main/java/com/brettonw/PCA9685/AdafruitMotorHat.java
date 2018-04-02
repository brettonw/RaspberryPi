package com.brettonw.PCA9685;

import com.pi4j.io.i2c.I2CDevice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * DC and Stepper Motor Hat
 *
 * https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/overview
 * https://cdn-shop.adafruit.com/datasheets/TB6612FNG_datasheet_en_20121101.pdf
 *
 * this "hat" is a combination 9685 16 Channel Pulse Width Modulation Controller (PWM) for LEDs, and
 * 2 6612 H-bridge motor controllers driven off the modulated outputs. the "hat" supports four
 * motors (a stepper motor is driven as if it were two motors)
 *
 */
public class AdafruitMotorHat extends PCA9685 implements MotorController {
    private static final Logger log = LogManager.getLogger (AdafruitMotorHat.class);

    // internal class for the components of a motor
    static class MotorSpec {
        int modulator;
        int frontPin;
        int backPin;

        MotorSpec (int modulator, int frontPin, int backPin) {
            this.modulator = modulator;
            this.frontPin = frontPin;
            this.backPin = backPin;
        }
    }

    private static final Map<MotorId, MotorSpec> motorSpecs;
    static {
        motorSpecs = new HashMap<> (4);
        motorSpecs.put (MotorId.MOTOR_1, new MotorSpec (8, 9, 10));
        motorSpecs.put (MotorId.MOTOR_2, new MotorSpec (13, 12, 11));
        motorSpecs.put (MotorId.MOTOR_3, new MotorSpec (2, 3, 4));
        motorSpecs.put (MotorId.MOTOR_4, new MotorSpec (7, 6, 5));
    }

    public static final int DEFAULT_ADDRESS = 0x60;

    public AdafruitMotorHat () {
        this (DEFAULT_ADDRESS);
    }

    /**
     *
     * @param address
     */
    public AdafruitMotorHat (int address) {
        super (address);
    }

    public AdafruitMotorHat (I2CDevice i2CDevice) {
        super (i2CDevice);
    }

    /**
     * run a motor
     * @param motorId - which motor to run
     * @param speed - the speed to run it at in the range 0..1, 0 is stopped.
     */
    public void runMotor (MotorId motorId, double speed) {
        MotorSpec motorSpec = motorSpecs.get (motorId);
        if (speed < 0.0) {
            setChannelOff (motorSpec.frontPin);
            setChannelOn (motorSpec.backPin);
            setChannelPulse (motorSpec.modulator, (int) (-speed * CHANNEL_HIGH));
        } else if (speed > 0.0) {
            setChannelOn (motorSpec.frontPin);
            setChannelOff (motorSpec.backPin);
            setChannelPulse (motorSpec.modulator, (int) (speed * CHANNEL_HIGH));
        } else if (speed == 0.0) {
            setChannelOff (motorSpec.frontPin);
            setChannelOff (motorSpec.backPin);
            setChannelOff (motorSpec.modulator);
        }
    }
}
