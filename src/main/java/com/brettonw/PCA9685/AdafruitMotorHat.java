package com.brettonw.PCA9685;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// DC and Stepper Motor Hat
// https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/overview
// this "hat" is a combination 9685 16 Channel Pulse Width Modulation Controller (PWM) for
// LEDs, and 2 6612 H-bridge motor controllers driven off the modulated outputs. the "hat"
// supports four motors (a stepper motor is driven as if it were two motors)
// https://cdn-shop.adafruit.com/datasheets/TB6612FNG_datasheet_en_20121101.pdf
public class AdafruitMotorHat extends PCA9685 {
    protected static final Logger log = LogManager.getLogger (AdafruitMotorHat.class);

    public static final int DEFAULT_ADDRESS = 0x60;

    public AdafruitMotorHat () {
        this (DEFAULT_ADDRESS);
    }

    public AdafruitMotorHat (int address) {
        super (address);
    }

    private void runMotor (int modulator, int frontPin, int backPin, double speed) {
        try {
            if (speed < 0.0) {
                setChannelOff (frontPin);
                setChannelOn (backPin);
                setChannelPulse (modulator, (int) (-speed * CHANNEL_HIGH));
            } else if (speed > 0.0) {
                setChannelOn (frontPin);
                setChannelOff (backPin);
                setChannelPulse (modulator, (int) (speed * CHANNEL_HIGH));
            } else if (speed == 0.0) {
                setChannelOff (frontPin);
                setChannelOff (backPin);
                setChannelOff (modulator);
            }
        }
        catch (Exception exception) {
            log.error (exception);
        }
    }

    public void runMotor (MotorId motorId, double speed) {
        switch (motorId) {
            case MOTOR_1: runMotor (8, 9, 10, speed); break;
            case MOTOR_2: runMotor (13, 12, 11, speed); break;
            case MOTOR_3: runMotor (2, 3, 4, speed); break;
            case MOTOR_4: runMotor (7, 6, 5, speed); break;
        }
    }
}
