package com.brettonw.PCA9685;

import com.brettonw.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StepperMotor {
    protected static final Logger log = LogManager.getLogger (Motor.class);

    private AdafruitMotorHat controller;
    private MotorId motorIdA;
    private MotorId motorIdB;
    private StepValue steps[];
    private int stepsPerRevolution;
    private int currentStepIndex;

    public StepperMotor (int stepsPerRevolution, AdafruitMotorHat controller, MotorId motorIdA, MotorId motorIdB) {
        this (stepsPerRevolution, controller, motorIdA, motorIdB, StepType.FULL_STEP);
    }

    public StepperMotor (int stepsPerRevolution, AdafruitMotorHat controller, MotorId motorIdA, MotorId motorIdB, StepType stepType) {
        this (stepsPerRevolution, controller, motorIdA, motorIdB, stepType, 0);
    }

    public StepperMotor (int stepsPerRevolution, AdafruitMotorHat controller, MotorId motorIdA, MotorId motorIdB, StepType stepType, int subStepShift) {
        this.controller = controller;
        this.motorIdA = motorIdA;
        this.motorIdB = motorIdB;
        currentStepIndex = 0;

        // build the steps table
        switch (stepType) {
            case FULL_STEP:
                steps = new StepValue[] {
                    new StepValue (1, 0),
                    new StepValue (0, 1),
                    new StepValue (-1, 0),
                    new StepValue (0, -1)
                };
                break;
            case HALF_STEP:
                steps = new StepValue[]{
                    new StepValue (1, 0),
                    new StepValue (1, 1),
                    new StepValue (0, 1),
                    new StepValue (-1, 1),
                    new StepValue (-1, 0),
                    new StepValue (-1, -1),
                    new StepValue (0, -1),
                    new StepValue (1, -1)
                };
                break;
            case SUB_STEP: {
                steps = new StepValue[16 << subStepShift];
                double stepAngle = (Math.PI * 2.0) / steps.length;
                for (int i = 0; i < steps.length; ++i) {
                    double step = stepAngle * i;
                    steps[i] = new StepValue (Math.cos (step), Math.sin (step));
                }
            }
                break;
        }
        this.stepsPerRevolution = stepsPerRevolution * (steps.length / 4);

        // energize the motors
        controller.runMotor (motorIdA, steps[currentStepIndex].motor1);
        controller.runMotor (motorIdB, steps[currentStepIndex].motor2);
    }

    private void step (int direction) {
        // add the direction for the step, and ensure the new index is in the valid region
        currentStepIndex += direction;
        do {
            currentStepIndex = (currentStepIndex + steps.length) & (steps.length - 1);
        }
        while (currentStepIndex < 0);

        // now energize the motors
        controller.runMotor (motorIdA, steps[currentStepIndex].motor1);
        controller.runMotor (motorIdB, steps[currentStepIndex].motor2);
    }

    public StepperMotor step (StepDirection direction) {
        step (direction.getStepAdd ());
        return this;
    }

    public StepperMotor turn (double revolutions, double time) {
        int stepCount = (int) Math.round (Math.abs (revolutions) * stepsPerRevolution);
        // time is in seconds
        int millisecondsDelayPerStep = (int) Math.round ((1000 * time) / stepCount);
        int direction = (revolutions >= 0) ? 1 : -1;
        log.debug (stepCount + " steps (direction: " + direction + ", delay: " + millisecondsDelayPerStep + ")");
        for (int i = 0; i < stepCount; ++i) {
            step (direction);
            Utility.waitL (millisecondsDelayPerStep);
        }
        return this;
    }

    public StepperMotor run (double speed) {
        // this should be threaded in the future
        return this;
    }

    public StepperMotor stop () {
        controller.runMotor (motorIdA, 0);
        controller.runMotor (motorIdB, 0);
        return this;
    }

}
