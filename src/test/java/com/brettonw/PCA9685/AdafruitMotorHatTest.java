package com.brettonw.PCA9685;

import org.junit.Test;

/**
 * DC Motor demo
 * <p>
 * I used motors like https://www.adafruit.com/product/2941
 * or https://www.adafruit.com/product/711
 */
public class AdafruitMotorHatTest {


    @Test
    public void test1 () {
        AdafruitMotorHat motorHat = new AdafruitMotorHat ();
        if (motorHat.hasDevice ()) {
            for (int i = 0; i <= PCA9685.CHANNEL_HIGH; i += 16) {
                motorHat.runMotor (AdafruitMotorHat.Motor.MOTOR_1, i / 4095.0);
                motorHat.waitL (1);
            }
            motorHat.waitD (5);
            motorHat.stopMotor (AdafruitMotorHat.Motor.MOTOR_1);
            motorHat.waitD (1);

            for (int i = 0; i <= 4095; i += 16) {
                motorHat.runMotor (AdafruitMotorHat.Motor.MOTOR_1, -i / 4095.0);
                motorHat.waitL (1);
            }
            motorHat.waitD (5);
            motorHat.stopMotor (AdafruitMotorHat.Motor.MOTOR_1);
            motorHat.waitL (1);
            motorHat.waitD (1);
        }
    }

}
