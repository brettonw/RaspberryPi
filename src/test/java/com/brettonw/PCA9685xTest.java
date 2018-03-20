package com.brettonw;

import org.junit.Test;

/**
 * DC Motor demo
 * <p>
 * I used motors like https://www.adafruit.com/product/2941
 * or https://www.adafruit.com/product/711
 */
public class PCA9685xTest {


    @Test
    public void testPCA9685x () {
        PCA9685x    pca9865x = new PCA9685x ();

        for (int i = 0; i <= 4095; i += 4) {
            pca9865x.runMotor (PCA9685x.Motor.MOTOR_1, i / 4095.0);
            pca9865x.waitL (1);
        }
        pca9865x.waitD (3);
        pca9865x.stopMotor (PCA9685x.Motor.MOTOR_1);
        pca9865x.waitD (1);

        for (int i = 0; i <= 4095; i += 4) {
            pca9865x.runMotor (PCA9685x.Motor.MOTOR_1, -i / 4095.0);
            pca9865x.waitL (1);
        }
        pca9865x.waitD (3);
        pca9865x.stopMotor (PCA9685x.Motor.MOTOR_1);
        pca9865x.waitL (1);
        pca9865x.waitD (1);
    }

}
