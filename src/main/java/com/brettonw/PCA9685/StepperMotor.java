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
        this (stepsPerRevolution, controller, motorIdA, motorIdB, StepType.FULL_STEP_DOUBLE_COIL);
    }

    public StepperMotor (int stepsPerRevolution, AdafruitMotorHat controller, MotorId motorIdA, MotorId motorIdB, StepType stepType) {
        this.controller = controller;
        this.motorIdA = motorIdA;
        this.motorIdB = motorIdB;
        currentStepIndex = 0;

        // build the steps table - basically it is a representation of a list of 2d coordinates
        // taken to be positions on the unit circle, and traversed in angle order
        switch (stepType) {
            case FULL_STEP_SINGLE_COIL:
                // the most basic list of 2D coordinates starts at 0 degrees and proceeds at 90
                // degree intervals in 4 steps. i find this method to be unreliable, often missing
                // steps, so I don't suggest using it, but it is provided for completeness
                steps = makeSteps (0, 4, true);
                break;
            case FULL_STEP_DOUBLE_COIL:
                // the next most basic case starts at 45 degrees, and proceeds in 4, 90 degrees
                // steps. this is more reliable, and more powerful, as both motors are always
                // energized.
                steps = makeSteps (Math.PI / 4.0, 4, true);
                break;
            case HALF_STEP:
                // half step starts at the 45 degree angle and proceeds in 8 45 degree steps. this
                // is smoother than the full steps, but has varied torque across the steps
                steps = makeSteps (0, 8, true);
                break;
            case MICRO_STEP_8:
                // similar to the half step (but it's not saturated), 8 steps around the circle, so
                // the torque should be constant over all the steps
                steps = makeSteps (0, 8);
                break;
            case MICRO_STEP_16:
                // smooth, slow
                steps = makeSteps (0, 16);
                break;
            case MICRO_STEP_32:
                // super smooth, super slow
                steps = makeSteps (0, 32);
                break;

            //experimental
            case WAVE_STEP_6:
                // doing non-quadrant-symmetric walks around the unit circle, a compromise between
                // the smoother operation that comes with more steps and the slow speed that also
                // comes with more steps.
                steps = makeSteps (0, 6);
                break;
            case WAVE_STEP_11:
                // doing non-quadrant-symmetric walks around the unit circle, a compromise between
                // the smoother operation that comes with more steps and the slow speed that also
                // comes with more steps.
                steps = makeSteps (0, 11);
                break;
            case WAVE_STEP_20:
                // doing non-quadrant-symmetric walks around the unit circle, a compromise between
                // the smoother operation that comes with more steps and the slow speed that also
                // comes with more steps.
                steps = makeSteps (0, 20);
                break;
        }

        // steps per revolution is an artifical number based on the number of discrete positions of
        // the two energizing coils with the full step model - so we have to compensate if we use a
        // different cyle. the number of full cycles through the steps revolution is given by:
        // stepsPerRevolution / 4. one is led to believe that all steppers have a stepsPerRevolution
        // that is evenly divisible by 4.
        this.stepsPerRevolution = (steps.length * stepsPerRevolution) / 4;

        energize ();
    }

    private static StepValue[] makeSteps (double startAngle, int stepCount) {
        return makeSteps (startAngle, stepCount, false);
    }

    private static StepValue[] makeSteps (double startAngle, int stepCount, boolean saturate) {
        StepValue steps[] = new StepValue[stepCount];
        double stepAngle = (Math.PI * 2.0) / stepCount;
        for (int i = 0; i < stepCount; ++i) {
            double step = startAngle + (stepAngle * i);
            steps[i] = new StepValue (Math.cos (step), Math.sin (step), saturate);
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
            currentStepIndex = (currentStepIndex + steps.length) % steps.length;
        }
        while (currentStepIndex < 0);
        log.debug ("currentStepIndex: " + currentStepIndex);

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
        int stepCount = (int) Math.round (Math.abs (revolutions) * (stepsPerRevolution + 1));
        // time is in seconds
        int millisecondsDelayPerStep = (int) Math.round ((1_000 * time) / stepCount);
        int direction = (int) Math.signum (revolutions);
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
