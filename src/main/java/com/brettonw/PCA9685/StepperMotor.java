package com.brettonw.PCA9685;

import com.brettonw.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * stepper motors work by driving multiple coils in a sequence. this class drives bi-polar stepper
 * motors, or motors that have two coils. The coils are activated using a pair of waveforms that are
 * out of phase with each other. another way to think about the coil activations is to think of the
 * power levels as the points on a unit circle, and the cycles are progressing around the unit
 * circle.
 *
 * this type of stepper is made using teeth internally that cause some number of detent positions
 * for the motor. most motors will specify the "step angle", which is the angle associated with
 * these detents in degrees
 */
public class StepperMotor {
    protected static final Logger log = LogManager.getLogger (Motor.class);

    private String stepperType;
    private int stepsPerRevolution;
    private AdafruitMotorHat controller;
    private MotorId motorIdA;
    private MotorId motorIdB;
    private StepValue steps[];
    private int currentStepIndex;

    /**
     * the most basic stepper traverses the unit circle, starting at 0 degrees and proceeds at 90
     * degree intervals in 4 steps. i find this method to be unreliable, often missing steps, so I
     * don't suggest using it. the next most basic case also proceeds at 90 degrees intervals in 4
     * steps, but starts at a 45 degree offset and saturates the control values. this way, both
     * coils are always fully activated, making the steps robust.
     * @param stepAngle - the degrees per step from the motor specification
     * @param controller - the Adafruit motor controller, two motors are used to drive the stepper
     * @param motorIdA - the first of the two motors, or "coils"
     * @param motorIdB - the second of the two motors, or "coils"
     * @return
     */
    public static StepperMotor getFullStepper (double stepAngle, AdafruitMotorHat controller, MotorId motorIdA, MotorId motorIdB) {
        return new StepperMotor ("full", stepAngle, controller, motorIdA, motorIdB, 4, Math.PI / 4.0, true);
    }

    /**
     * a half-stepper starts at 0 degrees and proceeds at 45 degree intervals and saturates the
     * control values. the result is more precise than a full step controller, but the torque varies
     * because the motor alternates between a single coil and both coils being activated.
     * @param stepAngle - the degrees per step from the motor specification
     * @param controller - the Adafruit motor controller, two motors are used to drive the stepper
     * @param motorIdA - the first of the two motors, or "coils"
     * @param motorIdB - the second of the two motors, or "coils"
     * @return
     */
    public static StepperMotor getHalfStepper (double stepAngle, AdafruitMotorHat controller, MotorId motorIdA, MotorId motorIdB) {
        return new StepperMotor ("half", stepAngle, controller, motorIdA, motorIdB, 8, 0, true);
    }

    /**
     * a micro-stepper starts at 0 degrees and proceeds around the unit circle at sample points
     * according to the stepCount.
     * @param stepAngle - the degrees per step from the motor specification
     * @param controller - the Adafruit motor controller, two motors are used to drive the stepper
     * @param motorIdA - the first of the two motors, or "coils"
     * @param motorIdB - the second of the two motors, or "coils"
     * @param stepCount - the number of steps per internal cycle. at higher counts, this can drive
     *                  the motor very precisely and smoothly, but the tradeoff is speed. useful
     *                  numbers start at 5 and go up.
     * @return
     */
    public static StepperMotor getMicroStepper (double stepAngle, AdafruitMotorHat controller, MotorId motorIdA, MotorId motorIdB, int stepCount) {
        return new StepperMotor ("micro", stepAngle, controller, motorIdA, motorIdB, stepCount, 0, false);
    }

    private StepperMotor (String stepperType, double stepAngle, AdafruitMotorHat controller, MotorId motorIdA, MotorId motorIdB, int stepCount, double startAngle, boolean saturate) {
        this.stepperType = stepperType;
        stepsPerRevolution = (int) Math.round (360.0 / stepAngle);
        this.controller = controller;
        this.motorIdA = motorIdA;
        this.motorIdB = motorIdB;
        currentStepIndex = 0;

        // build the steps table - basically it is a representation of a list of 2d coordinates
        // taken to be positions on the unit circle, and traversed in angle order
        makeSteps (stepCount, startAngle, saturate);
    }

    private void makeSteps (int stepCount, double startAngle, boolean saturate) {
        steps = new StepValue[stepCount];
        double stepAngle = (Math.PI * 2.0) / stepCount;
        for (int i = 0; i < stepCount; ++i) {
            double step = startAngle + (stepAngle * i);
            steps[i] = new StepValue (Math.cos (step), Math.sin (step), saturate);
        }
        step (0);
    }

    private void step (int direction) {
        // add the direction for the step, and ensure the new index is in the valid region
        currentStepIndex += direction;
        do {
            currentStepIndex = (currentStepIndex + steps.length) % steps.length;
        }
        while (currentStepIndex < 0);
        log.trace ("currentStepIndex: " + currentStepIndex);

        log.trace ("A (" + String.format ("%.04f", steps[currentStepIndex].motor1) + "), B (" + String.format ("%.04f", steps[currentStepIndex].motor2) + ")");
        controller.runMotor (motorIdA, steps[currentStepIndex].motor1);
        controller.runMotor (motorIdB, steps[currentStepIndex].motor2);
    }

    public StepperMotor turn (double revolutions) {
        // do it as fast as possible
        return turn (revolutions, -1);
    }

    public StepperMotor turn (double revolutions, double time) {
        // steps per revolution is an artifical number based on the number of discrete positions of
        // the two energizing coils with the full step model - so we have to compensate if we use a
        // different cycle. the number of full cycles through the stepsPerRevolution is given by:
        // stepsPerRevolution / 4. one is led to believe that all steppers have a stepsPerRevolution
        // that is evenly divisible by 4.
        int stepCount = (int) Math.round (Math.abs (revolutions) * (((steps.length * stepsPerRevolution) / 4) + 1));

        // time is in seconds
        int millisecondsDelayPerStep = (int) Math.round ((1_000.0 * time) / stepCount);
        int direction = (int) Math.signum (revolutions);
        log.trace (stepCount + " steps (direction: " + direction + ", delay: " + millisecondsDelayPerStep + ")");
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

    public String getStepperType () {
        return stepperType;
    }
}
