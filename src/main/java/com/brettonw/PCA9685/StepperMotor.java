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
        this (stepsPerRevolution, controller, motorIdA, motorIdB, StepType.FULL_STEP_DOUBLE);
    }

    public StepperMotor (int stepsPerRevolution, AdafruitMotorHat controller, MotorId motorIdA, MotorId motorIdB, StepType stepType) {
        this (stepsPerRevolution, controller, motorIdA, motorIdB, stepType, 0);
    }

    public StepperMotor (int stepsPerRevolution, AdafruitMotorHat controller, MotorId motorIdA, MotorId motorIdB, StepType stepType, int stepParameter) {
        this.controller = controller;
        this.motorIdA = motorIdA;
        this.motorIdB = motorIdB;
        currentStepIndex = 0;

        // build the steps table - basically it is a representation of a list of 2d coordinates
        // taken to be positions on the unit circle, and traversed in angle order
        switch (stepType) {
            case FULL_STEP_SINGLE:
                // the most basic list of 2D coordinates starts at 0 degrees and proceeds at 90
                // degree intervals in 4 steps. i find this method to be unreliable, often missing
                // steps, so I don't suggest using it, but it is provided for completeness
                steps = saturate (makeSteps (0, 4));
                break;
            case FULL_STEP_DOUBLE:
                // the next most basic case starts at 45 degrees, and proceeds in 4, 90 degrees
                // steps. this is more reliable, and more powerful, as both motors are always
                // energized.
                steps = saturate (makeSteps (Math.PI / 4.0, 4));
                break;
            case HALF_STEP:
                // half step starts at the 45 degree angle and proceeds in 8 45 degree steps. this
                // is smoother than the full steps, but has varied torque across the steps
                steps = saturate (makeSteps (Math.PI / 4.0, 8));
                break;
            case MICRO_STEP: {
                // start at 45 degrees, so both coils are fully energized to start
                // stepParameter is a bit shift value expected to be in the range 0..4
                steps = new StepValue[8 << stepParameter];
                double stepAngle = (Math.PI * 2.0) / steps.length;
                double startAngle = Math.PI / 4.0;
                for (int i = 0; i < steps.length; ++i) {
                    double step = startAngle + (stepAngle * i);
                    steps[i] = new StepValue (Math.cos (step), Math.sin (step));
                }
            }
                break;
            case WAVE_STEP: {
                //
            }
                break;
        }
        this.stepsPerRevolution = stepsPerRevolution * (steps.length / 4);

        energize ();
    }

    private static StepValue[] makeSteps (double startAngle, int stepCount) {
        StepValue steps[] = new StepValue[stepCount];
        double stepAngle = (Math.PI * 2.0) / stepCount;
        for (int i = 0; i < stepCount; ++i) {
            double step = startAngle + (stepAngle * i);
            steps[i] = new StepValue (Math.cos (step), Math.sin (step));
        }
        return steps;
    }

    private static StepValue[] saturate (StepValue steps[]) {
        for (StepValue stepValue : steps) {
            stepValue.motor1 = (Math.abs (stepValue.motor1) > 0.5) ? Math.signum (stepValue.motor1) : 0;
            stepValue.motor2 = (Math.abs (stepValue.motor2) > 0.5) ? Math.signum (stepValue.motor2) : 0;
        }
        return steps;
    }

    private void energize () {
        log.trace ("A (" + String.format ("%.04f", steps[currentStepIndex].motor1) + "), B (" + String.format ("%.04f", steps[currentStepIndex].motor2) + ")");
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

        energize ();
    }

    public StepperMotor step (StepDirection direction) {
        step (direction.getStepAdd ());
        return this;
    }

    public StepperMotor turn (double revolutions) {
        // do it as fast as possible
        return turn (revolutions, -1);
    }

    public StepperMotor turn (double revolutions, double time) {
        int stepCount = (int) Math.round (Math.abs (revolutions) * stepsPerRevolution);
        // time is in seconds
        int millisecondsDelayPerStep = (int) Math.round ((1_000 * time) / stepCount);
        int direction = (revolutions >= 0) ? 1 : -1;
        //log.debug (stepCount + " steps (direction: " + direction + ", delay: " + millisecondsDelayPerStep + ")");
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
