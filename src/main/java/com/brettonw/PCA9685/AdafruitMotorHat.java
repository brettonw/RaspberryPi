package com.brettonw.PCA9685;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

// DC and Stepper Motor Hat
// https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/overview
// this "hat" is a combination 9685 16 Channel Pulse Width Modulation Controller (PWM) for LEDs,
// and 2 6612 H-bridge motor controllers driven off the modulated outputs
// https://cdn-shop.adafruit.com/datasheets/TB6612FNG_datasheet_en_20121101.pdf
public class AdafruitMotorHat extends PCA9685 {
    protected static final Logger log = LogManager.getLogger (AdafruitMotorHat.class);

    public static final int DEFAULT_ADDRESS = 0x60;

    public enum Motor {
        MOTOR_1, MOTOR_2, MOTOR_3, MOTOR_4
    }

    public AdafruitMotorHat () {
        this (DEFAULT_ADDRESS);
    }

    public AdafruitMotorHat (int address) {
        super (address);
    }

    private void setPin (int pin, boolean high) throws IOException {
        setChannel (pin, high ? CHANNEL_HIGH : 0, high ? 0 : CHANNEL_HIGH);
    }

    private void runMotor (int modulator, int forward, int backward, double speed) {
        try {
            if (speed < 0.0) {
                setPin (forward, false);
                setPin (backward, true);
                setChannel (modulator, 0, (int) (-speed * CHANNEL_HIGH));
            } else if (speed > 0.0) {
                setPin (forward, true);
                setPin (backward, false);
                setChannel (modulator, 0, (int) (speed * CHANNEL_HIGH));
            } else if (speed == 0.0) {
                setPin (forward, false);
                setPin (backward, false);
                setChannel (modulator, 0, 0);
            }
        }
        catch (Exception exception) {
            log.error (exception);
        }
    }

    private void runMotorInternal (Motor motor, double speed) {
        switch (motor) {
            case MOTOR_1: runMotor (8, 9, 10, speed); break;
            case MOTOR_2: runMotor (13, 12, 11, speed); break;
            case MOTOR_3: runMotor (2, 3, 4, speed); break;
            case MOTOR_4: runMotor (7, 6, 5, speed); break;
        }
    }

    public PCA9685 runMotor (Motor motor, double speed) {
        log.debug ("Run " + motor.name () + "@" + speed);
        runMotorInternal (motor, speed);
        return this;
    }

    public PCA9685 stopMotor (Motor motor) {
        log.debug ("Stop " + motor.name ());
        runMotorInternal (motor, 0.0);
        return this;
    }

    public enum StepType {
        SINGLE, DOUBLE, INTERLEAVE, MICRO
    }

    public enum StepperMotor {
        STEPPER_MOTOR_1, STEPPER_MOTOR_2
    }

}
