package com.brettonw.PCA9685;

import com.brettonw.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class AdafruitMotorHatTest {
    protected static final Logger log = LogManager.getLogger (AdafruitMotorHatTest.class);

    private AdafruitMotorHat motorHat;

    public AdafruitMotorHatTest () {
        log.debug ("CONSTRUCT");
        motorHat = new AdafruitMotorHat ();
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
    public void testStepper () {
        StepperMotor stepper;
        long startTime, endTime;

        stepper = new StepperMotor (200, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2);
        log.info ("Full step backward");
        //stepper.turn (-1.0, 200.0);
        stepper.step (StepDirection.BACKWARD);
        Utility.waitD (1.0);
        stepper.step (StepDirection.BACKWARD);
        Utility.waitD (1.0);
        stepper.step (StepDirection.BACKWARD);
        Utility.waitD (1.0);
        stepper.step (StepDirection.BACKWARD);
        Utility.waitD (1.0);
        stepper.step (StepDirection.BACKWARD);
        Utility.waitD (1.0);
        stepper.step (StepDirection.BACKWARD);
        Utility.waitD (1.0);
        stepper.step (StepDirection.BACKWARD);
        Utility.waitD (1.0);
        stepper.step (StepDirection.BACKWARD);
        Utility.waitD (1.0);
        stepper.step (StepDirection.BACKWARD);
        Utility.waitD (1.0);
        stepper.step (StepDirection.BACKWARD);
        Utility.waitD (1.0);
        stepper.stop ();

/*
        stepper = new StepperMotor (200, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, StepType.HALF_STEP);
        log.info ("Half step forward");
        stepper.turn (1.0, 5.0);
        log.info ("Half Step backward");
        stepper.turn (-1.0, 5.0);
        stepper.stop ();

        stepper = new StepperMotor (200, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, StepType.MICRO_STEP);
        log.info ("Sub step forward");
        stepper.turn (1.0, 5.0);
        log.info ("Sub step backward");
        stepper.turn (-1.0, 5.0);
        stepper.stop ();

        stepper = new StepperMotor (200, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, StepType.MICRO_STEP, 1);
        log.info ("Sub step forward");
        stepper.turn (1.0, 5.0);
        log.info ("Sub step backward");
        stepper.turn (-1.0, 5.0);
        stepper.stop ();

        stepper = new StepperMotor (200, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, StepType.MICRO_STEP, 2);
        log.info ("Sub step forward");
        stepper.turn (1.0, 5.0);
        log.info ("Sub step backward");
        stepper.turn (-1.0, 5.0);
        stepper.stop ();
*/
        stepper = new StepperMotor (200, motorHat, MotorId.MOTOR_1, MotorId.MOTOR_2, StepType.MICRO_STEP);
        log.info ("Sub step forward");
        startTime = System.currentTimeMillis ();
        stepper.turn (1.0);
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
