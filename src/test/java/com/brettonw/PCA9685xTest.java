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

        pca9865x.runMotor (PCA9685x.Motor.MOTOR_1, PCA9685x.Direction.FORWARD, 1.0);
        pca9865x.waitD (3.0);
        pca9865x.runMotor (PCA9685x.Motor.MOTOR_1, PCA9685x.Direction.REVERSE, 1.0);
        pca9865x.waitD (3.0);
        pca9865x.stopMotor (PCA9685x.Motor.MOTOR_1);
    }

}
