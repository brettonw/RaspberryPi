package com.brettonw.PCA9685;

import com.brettonw.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class AdafruitMotorHatTest {
    protected static final Logger log = LogManager.getLogger (AdafruitMotorHatTest.class);

    private MotorController motorController;

    public AdafruitMotorHatTest () {
        log.debug ("CONSTRUCT");
        motorController = new AdafruitMotorHat ();
    }

    private void runMotor (MotorId motorId) {
        /*
        log.debug ("RUN (" + motorId.name () + ")");
        if (motorController.hasDevice ()) {
            log.debug ("FORWARD");
            for (int i = 0; i <= 100; i ++) {
                motorController.runMotor (motorId, i / 100.0);
                Utility.waitL (1);
            }
            Utility.waitD (5);
            motorController.stopMotor (motorId);
            Utility.waitD (1);

            log.debug ("REVERSE");
            for (int i = 0; i <= 100; i++) {
                motorController.runMotor (motorId, i / 100.0);
                Utility.waitL (1);
            }
            Utility.waitD (5);
            motorController.stopMotor (motorId);
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

    private void backAndForth (StepperMotor stepper) {
        long startTime = System.currentTimeMillis ();

        log.info (stepper.getDescription () + " - forward");
        stepper.turn (0.5);
        stepper.stop ();
        Utility.waitD (0.5);

        log.info (stepper.getDescription () + " - backward");
        stepper.turn (-0.5);
        stepper.stop ();
        Utility.waitD (0.5);

        log.info (String.format ("%.04f", ((System.currentTimeMillis () - startTime) / 1000.0)) + " seconds");
    }

    @Test
    public void testStepper () {
        if (false) {
            backAndForth (StepperMotor.getFullStepper (motorController, MotorId.MOTOR_1, MotorId.MOTOR_2, 1.8));
            backAndForth (StepperMotor.getHalfStepper (motorController, MotorId.MOTOR_1, MotorId.MOTOR_2, 1.8));
            backAndForth (StepperMotor.getMicroStepper (motorController, MotorId.MOTOR_1, MotorId.MOTOR_2, 1.8, 5));
            backAndForth (StepperMotor.getMicroStepper (motorController, MotorId.MOTOR_1, MotorId.MOTOR_2, 1.8, 6));
            backAndForth (StepperMotor.getMicroStepper (motorController, MotorId.MOTOR_1, MotorId.MOTOR_2, 1.8, 8));
            backAndForth (StepperMotor.getMicroStepper (motorController, MotorId.MOTOR_1, MotorId.MOTOR_2, 1.8, 10));
            backAndForth (StepperMotor.getMicroStepper (motorController, MotorId.MOTOR_1, MotorId.MOTOR_2, 1.8, 12));
            backAndForth (StepperMotor.getMicroStepper (motorController, MotorId.MOTOR_1, MotorId.MOTOR_2, 1.8, 16));
            backAndForth (StepperMotor.getMicroStepper (motorController, MotorId.MOTOR_1, MotorId.MOTOR_2, 1.8, 20));
            backAndForth (StepperMotor.getMicroStepper (motorController, MotorId.MOTOR_1, MotorId.MOTOR_2, 1.8, 24));
            backAndForth (StepperMotor.getMicroStepper (motorController, MotorId.MOTOR_1, MotorId.MOTOR_2, 1.8, 32));
            backAndForth (StepperMotor.getMicroStepper (motorController, MotorId.MOTOR_1, MotorId.MOTOR_2, 1.8, 0.5));
        }
    }

}
