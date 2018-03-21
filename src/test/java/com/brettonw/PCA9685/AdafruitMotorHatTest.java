package com.brettonw.PCA9685;

import com.brettonw.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * DC Motor demo
 * <p>
 * I used motors like https://www.adafruit.com/product/2941
 * or https://www.adafruit.com/product/711
 */
public class AdafruitMotorHatTest {
    protected static final Logger log = LogManager.getLogger (AdafruitMotorHatTest.class);

    private AdafruitMotorHat motorHat;

    public AdafruitMotorHatTest () {
        log.debug ("CONSTRUCT");
        motorHat = new AdafruitMotorHat ();
    }

    private void runMotor (AdafruitMotorHat.Motor motor) {
        log.debug ("RUN (" + motor.name () + ")");
        if (motorHat.hasDevice ()) {
            log.debug ("FORWARD");
            for (int i = 0; i <= PCA9685.CHANNEL_HIGH; i += 16) {
                motorHat.runMotor (motor, i / 4095.0);
                Utility.waitL (1);
            }
            Utility.waitD (5);
            motorHat.stopMotor (motor);
            Utility.waitD (1);

            log.debug ("REVERSE");
            for (int i = 0; i <= 4095; i += 16) {
                motorHat.runMotor (motor, -i / 4095.0);
                Utility.waitL (1);
            }
            Utility.waitD (5);
            motorHat.stopMotor (motor);
            Utility.waitD (1);
        }
    }

    @Test
    public void test1 () {
        runMotor (AdafruitMotorHat.Motor.MOTOR_1);
        runMotor (AdafruitMotorHat.Motor.MOTOR_2);
        runMotor (AdafruitMotorHat.Motor.MOTOR_3);
        runMotor (AdafruitMotorHat.Motor.MOTOR_4);
    }

}
