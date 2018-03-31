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

    private void backAndForth (StepperMotor stepper) {
        long startTime = System.currentTimeMillis ();

        log.info (stepper.getStepperType () + " - forward");
        stepper.turn (0.5, 5.0);
        stepper.stop ();
        Utility.waitD (0.5);

        log.info (stepper.getStepperType () + " - backward");
        stepper.turn (-0.5, 5.0);
        stepper.stop ();
        Utility.waitD (0.5);

        log.info (String.format ("%.04f", ((System.currentTimeMillis () - startTime) / 1000.0)) + " seconds");
    }

    @Test
    public void testStepper () {
        backAndForth (StepperMotor.getFullStepper (1.8, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2));
        backAndForth (StepperMotor.getHalfStepper (1.8, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2));
        backAndForth (StepperMotor.getMicroStepper (1.8, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, 6));
        backAndForth (StepperMotor.getMicroStepper (1.8, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, 8));
        backAndForth (StepperMotor.getMicroStepper (1.8, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, 10));
        backAndForth (StepperMotor.getMicroStepper (1.8, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, 20));
        backAndForth (StepperMotor.getMicroStepper (1.8, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, 30));
        backAndForth (StepperMotor.getMicroStepper (1.8, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, 5));
    }

}
