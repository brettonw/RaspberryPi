package com.brettonw.PCA9685;

import com.brettonw.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * stepper motors work by driving multiple coils in a sequence. this class drives bi-polar stepper
 * motors, or motors that have two coils. The coils are activated using a pair of waveforms that are
 * out of phase with each other. another way to think about the coil activations is to think of the
 * power levels as the points on a unit circle, and the steps are progressing in cycles around the
 * unit circle.
 *
 * this type of stepper is made using teeth internally that cause some number of detent positions
 * for the motor. most motors will specify the "step angle", which is the angle associated with
 * these detents in degrees
 */
public class StepperMotor {
    protected static final Logger log = LogManager.getLogger (Motor.class);

    public static final int MINIMUM_CYCLE_DELAY = 2;

    private final String stepperType;
    private final double stepAngle;
    private final int stepsPerRevolution;
    private final AdafruitMotorHat controller;
    private final MotorId motorIdA;
    private final MotorId motorIdB;
    private final CycleValue cycle[];
    private int current;
    private int minimumCycleDelay;

    /**
     * the most basic stepper traverses the unit circle, starting at 0 degrees and proceeds at 90
     * degree intervals in 4 cycle. i find this method to be unreliable, often missing steps, so I
     * don't suggest using it. the next most basic case also proceeds at 90 degrees intervals in 4
     * steps, but starts at a 45 degree offset and saturates the control values. this way, both
     * coils are always fully activated, making the cycle robust.
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
     * a half-stepper starts at 0 degrees, proceeds at 45 degree intervals, and saturates the
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
     *
     * @param stepAngle   - the degrees per step from the motor specification
     * @param controller  - the Adafruit motor controller, two motors are used to drive the stepper
     * @param motorIdA    - the first of the two motors, or "coils"
     * @param motorIdB    - the second of the two motors, or "coils"
     * @param cycleLength - the number of internal steps per cycle. at higher counts, this can drive
     *                    the motor very precisely and smoothly, but the tradeoff is speed. useful
     *                    numbers start at 5 and go up.
     * @return
     */
    public static StepperMotor getMicroStepper (double stepAngle, AdafruitMotorHat controller, MotorId motorIdA, MotorId motorIdB, int cycleLength) {
        return new StepperMotor ("micro", stepAngle, controller, motorIdA, motorIdB, cycleLength, 0, false);
    }

    /**
     * a micro-stepper starts at 0 degrees and proceeds around the unit circle at sample points
     * according to the desired angular resolution.
     *
     * @param stepAngle   - the degrees per step from the motor specification
     * @param controller  - the Adafruit motor controller, two motors are used to drive the stepper
     * @param motorIdA    - the first of the two motors, or "coils"
     * @param motorIdB    - the second of the two motors, or "coils"
     * @param resolution - the desired accuracy of the motor.
     * @return
     */
    public static StepperMotor getMicroStepper (double stepAngle, AdafruitMotorHat controller, MotorId motorIdA, MotorId motorIdB, double resolution) {
        double cycleAngle = stepAngle * 4.0;
        int cycleLength = (int) Math.round (cycleAngle / resolution);
        return new StepperMotor ("micro", stepAngle, controller, motorIdA, motorIdB, cycleLength, 0, false);
    }

    private StepperMotor (String stepperType, double stepAngle, AdafruitMotorHat controller, MotorId motorIdA, MotorId motorIdB, int cycleLength, double startAngle, boolean saturate) {
        this.stepperType = stepperType;
        this.stepAngle = stepAngle;
        stepsPerRevolution = (int) Math.round (360.0 / stepAngle);
        this.controller = controller;
        this.motorIdA = motorIdA;
        this.motorIdB = motorIdB;
        current = 0;
        minimumCycleDelay = MINIMUM_CYCLE_DELAY;

        // build the cycle table - basically it is a representation of a list of 2d coordinates
        // taken to be positions on the unit circle, and traversed in angle order
        cycle = new CycleValue[cycleLength];
        double cycleAngle = (Math.PI * 2.0) / cycleLength;
        for (int i = 0; i < cycleLength; ++i) {
            double angle = startAngle + (cycleAngle * i);
            cycle[i] = new CycleValue (Math.cos (angle), Math.sin (angle), saturate);
        }

        log.info (getDescription () + ", with " + stepsPerRevolution + " steps per revolution");

        // and now... energize the coils at the start of the cycle
        step (0);
    }

    private void step (int direction) {
        // add the direction for the step, and ensure the new index is in the valid region
        current += direction;
        do { current = (current + cycle.length) % cycle.length; } while (current < 0);
        log.trace ("current: " + current);

        log.trace ("A (" + String.format ("%.04f", cycle[current].motor1) + "), B (" + String.format ("%.04f", cycle[current].motor2) + ")");
        controller.runMotor (motorIdA, cycle[current].motor1);
        controller.runMotor (motorIdB, cycle[current].motor2);
    }

    public StepperMotor turn (double revolutions) {
        // do it as fast as possible
        return turn (revolutions, 0);
    }

    public StepperMotor turn (double revolutions, double time) {
        // XXX TODO this should be threaded in the future

        // stepsPerRevolution is an artifical number based on the number of discrete positions of
        // the two energizing coils with the full step model - so we have to compensate if we use a
        // different cycle length. the number of full cycles through the stepsPerRevolution is given
        // by: stepsPerRevolution / 4. one is led to believe that all steppers have a
        // stepsPerRevolution that is evenly divisible by 4.
        int stepCount = (int) Math.round (Math.abs (revolutions) * (((cycle.length * stepsPerRevolution) / 4) + 1));

        // time is in seconds
        int millisecondsDelayPerStep = Math.max((int) Math.round ((1_000.0 * time) / stepCount), minimumCycleDelay);
        int direction = (int) Math.signum (revolutions);
        log.info (stepCount + " steps (direction: " + direction + ", delay: " + millisecondsDelayPerStep + ")");
        for (int i = 0; i < stepCount; ++i) {
            step (direction);
            Utility.waitL (millisecondsDelayPerStep);
        }
        return this;
    }

    public StepperMotor stop () {
        controller.runMotor (motorIdA, 0);
        controller.runMotor (motorIdB, 0);
        return this;
    }

    public String getDescription () {
        return stepperType + "-step, cycle-length (per 4 steps): " + cycle.length + ", resolution: " + String.format ("%.04f", (stepAngle * 4) / cycle.length) + " degrees/step";
    }

    public StepperMotor setMinimumCycleDelay (int minimumCycleDelay) {
        this.minimumCycleDelay = minimumCycleDelay;
        return this;
    }
}
