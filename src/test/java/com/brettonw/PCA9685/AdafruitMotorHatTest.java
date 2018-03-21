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

    private void runMotor (AdafruitMotorHat.Motor motor) {
        log.debug ("RUN (" + motor.name () + ")");
        if (motorHat.hasDevice ()) {
            log.debug ("FORWARD");
            for (int i = 0; i <= 100; i ++) {
                motorHat.runMotor (motor, i / 100.0);
                Utility.waitL (1);
            }
            Utility.waitD (5);
            motorHat.stopMotor (motor);
            Utility.waitD (1);

            log.debug ("REVERSE");
            for (int i = 0; i <= 100; i++) {
                motorHat.runMotor (motor, i / 100.0);
                Utility.waitL (1);
            }
            Utility.waitD (5);
            motorHat.stopMotor (motor);
            Utility.waitD (1);
        }
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
        AdafruitMotorHat.StepValue[] steps = AdafruitMotorHat.makeSteps (2);
        for (int i = 0; i < 100; ++i) {
            motorHat.stepMotorWhole (AdafruitMotorHat.Stepper.STEPPER_1, steps, 2);
        }
        motorHat.stepMotorStop (AdafruitMotorHat.Stepper.STEPPER_1);
        for (int i = 0; i < 100; ++i) {
            motorHat.stepMotorWhole (AdafruitMotorHat.Stepper.STEPPER_2, steps);
        }
        motorHat.stepMotorStop (AdafruitMotorHat.Stepper.STEPPER_2);
    }

}
