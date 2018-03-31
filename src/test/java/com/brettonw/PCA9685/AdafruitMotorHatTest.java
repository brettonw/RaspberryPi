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
        if (false) {
            //servoDriver.setPosition (0, 0);
            Utility.waitD (3.0);
            for (int j = 0; j < 5; ++j) {
                for (int i = 0; i <= 100; ++i) {
                    log.debug (i + "");
                    servoDriver.setPosition (0, i / 100.0);
                    Utility.waitL (10);
                }
                Utility.waitD (1.0);
                for (int i = 100; i >= 0; --i) {
                    log.debug (i + "");
                    servoDriver.setPosition (0, i / 100.0);
                    Utility.waitL (10);
                }
                Utility.waitD (1.0);
            }
        }
    }

    private void backAndForth (StepType stepType) {
        long startTime = System.currentTimeMillis ();
        StepperMotor stepper = new StepperMotor (200, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, stepType);

        log.info (stepType.name () + " - forward");
        stepper.turn (1.0, 5.0);
        stepper.stop ();
        Utility.waitD (1.0);

        log.info (stepType.name () + " - backward");
        stepper.turn (-1.0, 5.0);
        stepper.stop ();
        Utility.waitD (1.0);

        log.info (String.format ("%.04f", ((System.currentTimeMillis () - startTime) / 1000.0)) + " seconds");
    }

    @Test
    public void testStepper () {
        backAndForth (StepType.FULL_STEP_DOUBLE_COIL);
        backAndForth (StepType.HALF_STEP);
        backAndForth (StepType.MICRO_STEP_8);
        backAndForth (StepType.MICRO_STEP_16);
        backAndForth (StepType.MICRO_STEP_32);
        backAndForth (StepType.WAVE_STEP_6);
        backAndForth (StepType.WAVE_STEP_11);
        backAndForth (StepType.WAVE_STEP_20);
        backAndForth (StepType.FULL_STEP_SINGLE_COIL);
    }

}
