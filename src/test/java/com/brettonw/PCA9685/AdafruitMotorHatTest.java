package com.brettonw.PCA9685;

import com.brettonw.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class AdafruitMotorHatTest {
    protected static final Logger log = LogManager.getLogger (AdafruitMotorHatTest.class);

    private AdafruitMotorHat motorHat;
    private AdafruitServoDriver servoDriver;

    public AdafruitMotorHatTest () {
        log.debug ("CONSTRUCT");
        motorHat = new AdafruitMotorHat ();
        servoDriver = new AdafruitServoDriver ();
    }

    private void runMotor (MotorId motorId) {
        /*
        log.debug ("RUN (" + motorId.name () + ")");
        if (motorHat.hasDevice ()) {
            log.debug ("FORWARD");
            for (int i = 0; i <= 100; i ++) {
                motorHat.runMotor (motorId, i / 100.0);
                Utility.waitL (1);
            }
            Utility.waitD (5);
            motorHat.stopMotor (motorId);
            Utility.waitD (1);

            log.debug ("REVERSE");
            for (int i = 0; i <= 100; i++) {
                motorHat.runMotor (motorId, i / 100.0);
                Utility.waitL (1);
            }
            Utility.waitD (5);
            motorHat.stopMotor (motorId);
            Utility.waitD (1);
        }
        */
    }

    @Test
    public void testRunMotors () {
        /*
        runMotor (AdafruitMotorHat.Motor.MOTOR_1);
        runMotor (AdafruitMotorHat.Motor.MOTOR_2);
        runMotor (AdafruitMotorHat.Motor.MOTOR_3);
        runMotor (AdafruitMotorHat.Motor.MOTOR_4);
        */
    }

    @Test
    public void testServo () {
        servoDriver.setPosition (0, 0);
        Utility.waitD (3.0);
        for (int j = 0; j < 5; ++j) {
            for (int i = 0; i <= 100; ++i) {
                log.debug (i + "");
                servoDriver.setPosition (0, i / 100.0);
                Utility.waitL (100);
            }
            Utility.waitD (1.0);
            for (int i = 100; i >= 0; --i) {
                log.debug (i + "");
                servoDriver.setPosition (0, i / 100.0);
                Utility.waitL (100);
            }
            Utility.waitD (1.0);
        }

    }

    @Test
    public void testStepper () {
        if (false) {
            StepperMotor stepper;
            long startTime, endTime;

            Utility.waitD (1.0);
            stepper = new StepperMotor (200, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2);
            log.info ("Full step forward");
            //stepper.turn (-1.0, 200.0);
            Utility.waitD (1.0);
            for (int i = 0; i < 10; ++i) {
                stepper.step (StepDirection.FORWARD);
                Utility.waitD (1.0);
            }
            stepper.stop ();

            log.info ("Full step backward");
            Utility.waitD (1.0);
            for (int i = 0; i < 10; ++i) {
                stepper.step (StepDirection.BACKWARD);
                Utility.waitD (1.0);
            }
            stepper.stop ();

            stepper = new StepperMotor (200, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2);
            log.info ("Full step forward");
            //stepper.turn (-1.0, 200.0);
            Utility.waitD (1.0);
            for (int i = 0; i < 50; ++i) {
                stepper.step (StepDirection.FORWARD);
                Utility.waitL (5);
            }
            stepper.stop ();

            log.info ("Full step backward");
            Utility.waitD (1.0);
            for (int i = 0; i < 50; ++i) {
                stepper.step (StepDirection.BACKWARD);
                Utility.waitL (5);
            }
            stepper.stop ();
            Utility.waitD (1.0);

            stepper = new StepperMotor (200, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, StepType.HALF_STEP);
            log.info ("Half step forward");
            stepper.turn (1.0, 5.0);
            stepper.stop ();
            Utility.waitD (1.0);
            log.info ("Half Step backward");
            stepper.turn (-1.0, 5.0);
            stepper.stop ();
            Utility.waitD (1.0);

            stepper = new StepperMotor (200, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, StepType.MICRO_STEP);
            log.info ("Sub step forward");
            stepper.turn (1.0, 5.0);
            stepper.stop ();
            Utility.waitD (1.0);
            log.info ("Sub step backward");
            stepper.turn (-1.0, 5.0);
            stepper.stop ();
            Utility.waitD (1.0);

            stepper = new StepperMotor (200, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, StepType.MICRO_STEP, 1);
            log.info ("Sub step forward");
            stepper.turn (1.0, 5.0);
            stepper.stop ();
            Utility.waitD (1.0);
            log.info ("Sub step backward");
            stepper.turn (-1.0, 5.0);
            stepper.stop ();
            Utility.waitD (1.0);

            stepper = new StepperMotor (200, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, StepType.MICRO_STEP, 2);
            log.info ("Sub step forward");
            stepper.turn (1.0, 5.0);
            stepper.stop ();
            Utility.waitD (1.0);
            log.info ("Sub step backward");
            stepper.turn (-1.0, 5.0);
            stepper.stop ();
            Utility.waitD (1.0);

            stepper = new StepperMotor (200, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, StepType.MICRO_STEP);
            log.info ("Sub step forward");
            startTime = System.currentTimeMillis ();
            stepper.turn (1.0);
            stepper.stop ();
            Utility.waitD (1.0);
            endTime = System.currentTimeMillis ();
            log.info (String.format ("%.04f", ((endTime - startTime) / 1000.0)) + " seconds");
            log.info ("Sub step backward");
            startTime = System.currentTimeMillis ();
            stepper.turn (-1.0);
            endTime = System.currentTimeMillis ();
            log.info (String.format ("%.04f", ((endTime - startTime) / 1000.0)) + " seconds");
            stepper.stop ();
        }
    }

}
