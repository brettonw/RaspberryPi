package com.brettonw.PCA9685;

import com.brettonw.PCA9685.Tester.I2CDeviceTester;
import com.brettonw.PCA9685.Tester.ServoControllerTester;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ServoTest {
    @Test
    public void testServo () throws Exception {
        I2CDeviceTester testDevice = new I2CDeviceTester ();

        // set expectations for the device initialization
        testDevice
            .expect (0x00fa, (byte) 0x00)
            .expect (0x00fb, (byte) 0x00)
            .expect (0x00fc, (byte) 0x00)
            .expect (0x00fd, (byte) 0x00)
            .expect (0x0001, (byte) 0x04)
            .expect (0x0000, (byte) 0x01)
            .expect (0x0000, (byte) 0x00)
            // (setPulseFrequency) - @50 Hz, (pre-scale:0x79)
            .expect (0x0000, (byte) 0x10)
            .expect (0x00fe, (byte) 0x79)
            .expect (0x0000, (byte) 0x00)
            .expect (0x0000, (byte) 0x80);
        ServoControllerTester testController = new ServoControllerTester (testDevice);
        testDevice.report ();

        double min = 1, max = 2;

        testDevice
            // (setPulseDuration) - servoId: SERVO_01, milliseconds: 1.000
            .expect (0x0006, (byte) 0x00)
            .expect (0x0007, (byte) 0x00)
            .expect (0x0008, (byte) 0x33)
            .expect (0x0009, (byte) 0x01);
        Servo servo = new Servo (testController, ServoId.SERVO_01, min, max);
        assertTrue (servo.getServoId () == ServoId.SERVO_01);
        assertTrue (servo.getPosition () == 0);
        assertTrue (testController.getPulseDuration (servo.getServoId ()) == ((min + max) * 0.5));
        testDevice.report ();

        testDevice
                .expect (0x0006, (byte) 0x00)
                .expect (0x0007, (byte) 0x00)
                .expect (0x0008, (byte) 0x66)
                .expect (0x0009, (byte) 0x01);
        servo.setPosition (0.5);
        assertTrue (servo.getPosition () == 0.5);
        assertTrue (testController.getPulseDuration (servo.getServoId ()) == min + ((max - min) * 0.75));
        testDevice.report ();

        testDevice
            .expect (0x0006, (byte) 0x00)
            .expect (0x0007, (byte) 0x00)
            .expect (0x0008, (byte) 0x9a)
            .expect (0x0009, (byte) 0x01);
        servo.setPosition (1);
        assertTrue (servo.getPosition () == 1);
        assertTrue (testController.getPulseDuration (servo.getServoId ()) == max);
        testDevice.report ();

        testDevice
            .expect (0x0006, (byte) 0x00)
            .expect (0x0007, (byte) 0x00)
            .expect (0x0008, (byte) 0x9a)
            .expect (0x0009, (byte) 0x01);
        servo.setPosition (1.5);
        assertTrue (servo.getPosition () == 1);
        assertTrue (testController.getPulseDuration (servo.getServoId ()) == max);
        testDevice.report ();

        testDevice
                .expect (0x0006, (byte) 0x00)
                .expect (0x0007, (byte) 0x00)
                .expect (0x0008, (byte) 0x00)
                .expect (0x0009, (byte) 0x01);
        servo.setPosition (-0.5);
        assertTrue (servo.getPosition () == -0.5);
        assertTrue (testController.getPulseDuration (servo.getServoId ()) == min + ((max - min) * 0.25));
        testDevice.report ();

        testDevice
                .expect (0x0006, (byte) 0x00)
                .expect (0x0007, (byte) 0x00)
                .expect (0x0008, (byte) 0xcd)
                .expect (0x0009, (byte) 0x00);
        servo.setPosition (-1.0);
        assertTrue (servo.getPosition () == -1.0);
        assertTrue (testController.getPulseDuration (servo.getServoId ()) == min);
        testDevice.report ();

        testDevice
                .expect (0x0006, (byte) 0x00)
                .expect (0x0007, (byte) 0x00)
                .expect (0x0008, (byte) 0xcd)
                .expect (0x0009, (byte) 0x00);
        servo.setPosition (-1.5);
        assertTrue (servo.getPosition () == -1.0);
        assertTrue (testController.getPulseDuration (servo.getServoId ()) == min);
        testDevice.report ();
    }
}
