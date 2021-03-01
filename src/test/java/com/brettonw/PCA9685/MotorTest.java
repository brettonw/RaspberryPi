package com.brettonw.PCA9685;

import com.brettonw.PCA9685.Tester.I2CDeviceTester;
import com.brettonw.PCA9685.Tester.MotorControllerTester;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MotorTest {
    @Test
    public void testMotor () throws Exception {
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
            // (setPulseFrequency) - @1000 Hz, (pre-scale:0x05)
            .expect (0x0000, (byte) 0x10)
            .expect (0x00fe, (byte) 0x05)
            .expect (0x0000, (byte) 0x00)
            .expect (0x0000, (byte) 0x80);
        MotorControllerTester testController = new MotorControllerTester (testDevice);
        testDevice.report ();

        // (runMotor) - motorId: MOTOR_1, speed: 0.000
        testDevice
            .expect (0x002a, (byte) 0x00)
            .expect (0x002b, (byte) 0x00)
            .expect (0x002c, (byte) 0x00)
            .expect (0x002d, (byte) 0x10)
            .expect (0x002e, (byte) 0x00)
            .expect (0x002f, (byte) 0x00)
            .expect (0x0030, (byte) 0x00)
            .expect (0x0031, (byte) 0x10)
            .expect (0x0026, (byte) 0x00)
            .expect (0x0027, (byte) 0x00)
            .expect (0x0028, (byte) 0x00)
            .expect (0x0029, (byte) 0x10);
        Motor motor = new Motor (testController, MotorId.MOTOR_1);
        assertTrue (motor.getMotorId () == MotorId.MOTOR_1);
        assertTrue (testController.getSpeed (motor.getMotorId ()) == 0);
        assertTrue (testController.getSpeed (motor.getMotorId ()) == motor.getSpeed ());
        testDevice.report ();

        // (runMotor) - motorId: MOTOR_1, speed: 0.500
        testDevice
            .expect (0x002a, (byte) 0x00)
            .expect (0x002b, (byte) 0x10)
            .expect (0x002c, (byte) 0x00)
            .expect (0x002d, (byte) 0x00)
            .expect (0x002e, (byte) 0x00)
            .expect (0x002f, (byte) 0x00)
            .expect (0x0030, (byte) 0x00)
            .expect (0x0031, (byte) 0x10)
            .expect (0x0026, (byte) 0x00)
            .expect (0x0027, (byte) 0x00)
            .expect (0x0028, (byte) 0xff)
            .expect (0x0029, (byte) 0x07);
        motor.run (0.5);
        assertTrue (testController.getSpeed (motor.getMotorId ()) == 0.5);
        assertTrue (testController.getSpeed (motor.getMotorId ()) == motor.getSpeed ());
        testDevice.report ();

        // (runMotor) - motorId: MOTOR_1, speed: 1.000
        testDevice
            .expect (0x002a, (byte) 0x00)
            .expect (0x002b, (byte) 0x10)
            .expect (0x002c, (byte) 0x00)
            .expect (0x002d, (byte) 0x00)
            .expect (0x002e, (byte) 0x00)
            .expect (0x002f, (byte) 0x00)
            .expect (0x0030, (byte) 0x00)
            .expect (0x0031, (byte) 0x10)
            .expect (0x0026, (byte) 0x00)
            .expect (0x0027, (byte) 0x10)
            .expect (0x0028, (byte) 0x00)
            .expect (0x0029, (byte) 0x00);
        motor.run (1.0);
        assertTrue (testController.getSpeed (motor.getMotorId ()) == 1.0);
        assertTrue (testController.getSpeed (motor.getMotorId ()) == motor.getSpeed ());
        testDevice.report ();

        // (runMotor) - motorId: MOTOR_1, speed: 1.000
        testDevice
            .expect (0x002a, (byte) 0x00)
            .expect (0x002b, (byte) 0x10)
            .expect (0x002c, (byte) 0x00)
            .expect (0x002d, (byte) 0x00)
            .expect (0x002e, (byte) 0x00)
            .expect (0x002f, (byte) 0x00)
            .expect (0x0030, (byte) 0x00)
            .expect (0x0031, (byte) 0x10)
            .expect (0x0026, (byte) 0x00)
            .expect (0x0027, (byte) 0x10)
            .expect (0x0028, (byte) 0x00)
            .expect (0x0029, (byte) 0x00);
        motor.run (1.5);
        assertTrue (testController.getSpeed (motor.getMotorId ()) == 1.0);
        assertTrue (testController.getSpeed (motor.getMotorId ()) == motor.getSpeed ());
        testDevice.report ();

        // (runMotor) - motorId: MOTOR_1, speed: -0.500
        testDevice
            .expect (0x002a, (byte) 0x00)
            .expect (0x002b, (byte) 0x00)
            .expect (0x002c, (byte) 0x00)
            .expect (0x002d, (byte) 0x10)
            .expect (0x002e, (byte) 0x00)
            .expect (0x002f, (byte) 0x10)
            .expect (0x0030, (byte) 0x00)
            .expect (0x0031, (byte) 0x00)
            .expect (0x0026, (byte) 0x00)
            .expect (0x0027, (byte) 0x00)
            .expect (0x0028, (byte) 0xff)
            .expect (0x0029, (byte) 0x07);
        motor.run (-0.5);
        assertTrue (testController.getSpeed (motor.getMotorId ()) == -0.5);
        assertTrue (testController.getSpeed (motor.getMotorId ()) == motor.getSpeed ());
        testDevice.report ();

        // (runMotor) - motorId: MOTOR_1, speed: -1.000
        testDevice
            .expect (0x002a, (byte) 0x00)
            .expect (0x002b, (byte) 0x00)
            .expect (0x002c, (byte) 0x00)
            .expect (0x002d, (byte) 0x10)
            .expect (0x002e, (byte) 0x00)
            .expect (0x002f, (byte) 0x10)
            .expect (0x0030, (byte) 0x00)
            .expect (0x0031, (byte) 0x00)
            .expect (0x0026, (byte) 0x00)
            .expect (0x0027, (byte) 0x10)
            .expect (0x0028, (byte) 0x00)
            .expect (0x0029, (byte) 0x00);
        motor.run (-1.0);
        assertTrue (testController.getSpeed (motor.getMotorId ()) == -1.0);
        assertTrue (testController.getSpeed (motor.getMotorId ()) == motor.getSpeed ());
        testDevice.report ();

        // (runMotor) - motorId: MOTOR_1, speed: -1.000
        testDevice
            .expect (0x002a, (byte) 0x00)
            .expect (0x002b, (byte) 0x00)
            .expect (0x002c, (byte) 0x00)
            .expect (0x002d, (byte) 0x10)
            .expect (0x002e, (byte) 0x00)
            .expect (0x002f, (byte) 0x10)
            .expect (0x0030, (byte) 0x00)
            .expect (0x0031, (byte) 0x00)
            .expect (0x0026, (byte) 0x00)
            .expect (0x0027, (byte) 0x10)
            .expect (0x0028, (byte) 0x00)
            .expect (0x0029, (byte) 0x00);
        motor.run (-1.5);
        assertTrue (testController.getSpeed (motor.getMotorId ()) == -1.0);
        assertTrue (testController.getSpeed (motor.getMotorId ()) == motor.getSpeed ());
        testDevice.report ();

        // (runMotor) - motorId: MOTOR_1, speed: 0.000
        testDevice
            .expect (0x002a, (byte) 0x00)
            .expect (0x002b, (byte) 0x00)
            .expect (0x002c, (byte) 0x00)
            .expect (0x002d, (byte) 0x10)
            .expect (0x002e, (byte) 0x00)
            .expect (0x002f, (byte) 0x00)
            .expect (0x0030, (byte) 0x00)
            .expect (0x0031, (byte) 0x10)
            .expect (0x0026, (byte) 0x00)
            .expect (0x0027, (byte) 0x00)
            .expect (0x0028, (byte) 0x00)
            .expect (0x0029, (byte) 0x10);
        motor.stop ();
        assertTrue (testController.getSpeed (MotorId.MOTOR_1) == 0);
        assertTrue (testController.getSpeed (MotorId.MOTOR_2) == 0);
        assertTrue (testController.getSpeed (MotorId.MOTOR_3) == 0);
        assertTrue (testController.getSpeed (MotorId.MOTOR_4) == 0);
        testDevice.report ();
    }
}
