package com.brettonw.PCA9685;

import com.brettonw.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Stepper Motor
 *
 * stepper motors work by driving multiple coils in a sequence. this class drives bi-polar stepper
 * motors, or motors that have two coils. The coils are activated using a pair of waveforms that are
 * out of phase with each other. another way to think about the coil activations is to think of the
 * power levels as the points on a unit circle, and the steps are progressing in cycles around the
 * unit circle.
 *
 * this type of stepper is made using teeth internally that cause some number of detent positions
 * for the motor. most motors will specify the "step angle", which is the angle associated with
 * these detents in degrees (typical: 1.8 degrees)
 */
public class StepperMotor {
    private static final Logger log = LogManager.getLogger (StepperMotor.class);

    // internal class for the values in a cycle
    class CycleValue {
        double motor1;
        double motor2;

        CycleValue (double motor1, double motor2, boolean saturate) {
            this.motor1 = saturate ? (Utility.saturate (motor1)) : motor1;
            this.motor2 = saturate ? (Utility.saturate (motor2)) : motor2;
        }
    }

    private static final int MINIMUM_CYCLE_DELAY = 10;

    private final String stepperType;
    private final double stepAngle;
    private final int stepsPerRevolution;
    private final MotorController motorController;
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
     * @param motorController  - the motor motorController, two motors are used to drive the stepper
     * @param motorIdA - the first of the two motors, or "coils"
     * @param motorIdB - the second of the two motors, or "coils"
     * @return
     */
    public static StepperMotor getFullStepper (MotorController motorController, MotorId motorIdA, MotorId motorIdB, double stepAngle) {
        return new StepperMotor ("full", motorController, motorIdA, motorIdB, stepAngle, 4, Math.PI / 4.0, true);
    }

    /**
     * a half-stepper starts at 0 degrees, proceeds at 45 degree intervals, and saturates the
     * control values. the result is more precise than a full step motorController, but the torque varies
     * because the motor alternates between a single coil and both coils being activated.
     * @param stepAngle - the degrees per step from the motor specification
     * @param motorController  - the motor motorController, two motors are used to drive the stepper
     * @param motorIdA - the first of the two motors, or "coils"
     * @param motorIdB - the second of the two motors, or "coils"
     * @return
     */
    public static StepperMotor getHalfStepper (MotorController motorController, MotorId motorIdA, MotorId motorIdB, double stepAngle) {
        return new StepperMotor ("half", motorController, motorIdA, motorIdB, stepAngle, 8, 0, true);
    }

    /**
     * a micro-stepper starts at 0 degrees and proceeds around the unit circle at sample points
     * according to the stepCount.
     *
     * @param stepAngle   - the degrees per step from the motor specification
     * @param motorController  - the motor motorController, two motors are used to drive the stepper
     * @param motorIdA    - the first of the two motors, or "coils"
     * @param motorIdB    - the second of the two motors, or "coils"
     * @param cycleLength - the number of internal steps per cycle. at higher counts, this can drive
     *                    the motor very precisely and smoothly, but the tradeoff is speed. useful
     *                    numbers start at 5 and go up.
     * @return
     */
    public static StepperMotor getMicroStepper (MotorController motorController, MotorId motorIdA, MotorId motorIdB, double stepAngle, int cycleLength) {
        return new StepperMotor ("micro", motorController, motorIdA, motorIdB, stepAngle, cycleLength, 0, false);
    }

    /**
     * a micro-stepper starts at 0 degrees and proceeds around the unit circle at sample points
     * according to the desired angular resolution.
     *
     * @param stepAngle   - the degrees per step from the motor specification
     * @param motorController  - the motor motorController, two motors are used to drive the stepper
     * @param motorIdA    - the first of the two motors, or "coils"
     * @param motorIdB    - the second of the two motors, or "coils"
     * @param resolution - the desired accuracy of the motor.
     * @return
     */
    public static StepperMotor getMicroStepper (MotorController motorController, MotorId motorIdA, MotorId motorIdB, double resolution, double stepAngle) {
        // compute the closest approximation to the desired resolution
        int cycleLength = (int) Math.round ((stepAngle * 4.0) / resolution);
        return new StepperMotor ("micro", motorController, motorIdA, motorIdB, stepAngle, cycleLength, 0, false);
    }

    private StepperMotor (String stepperType, MotorController motorController, MotorId motorIdA, MotorId motorIdB, double stepAngle, int cycleLength, double startAngle, boolean saturate) {
        this.stepperType = stepperType;
        stepsPerRevolution = (int) Math.round (360.0 / stepAngle);
        this.motorController = motorController;
        this.motorIdA = motorIdA;
        this.motorIdB = motorIdB;
        this.stepAngle = stepAngle;
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
        motorController.runMotor (motorIdA, cycle[current].motor1);
        motorController.runMotor (motorIdB, cycle[current].motor2);
    }

    /**
     *
     * @param revolutions
     * @return
     */
    public StepperMotor turn (double revolutions) {
        // do it as fast as possible
        return turn (revolutions, 0);
    }

    /**
     *
     * @param revolutions
     * @param time
     * @return
     */
    public StepperMotor turn (double revolutions, double time) {
        // XXX TODO this should be (optionally) threaded in the future

        // stepsPerRevolution is an artifical number based on the number of discrete positions of
        // the two energizing coils with the full step model - so we have to compensate if we use a
        // different cycle length. the number of full cycles through the stepsPerRevolution is given
        // by: stepsPerRevolution / 4. one is led to believe that all steppers have a
        // stepsPerRevolution that is evenly divisible by 4.
        int stepCount = (int) Math.round (Math.abs (revolutions) * (((cycle.length * stepsPerRevolution) / 4) + 1));

        // time is in seconds
        int microsecondsDelayPerStep = (int) Math.round ((2 * 1_000_000.0 * time) / stepCount);
        int direction = (int) Math.signum (revolutions);
        log.debug (stepCount + " steps (direction: " + direction + ", delay: " + microsecondsDelayPerStep + ")");
        double halfway = stepCount / 2.0;
        for (int i = 0; i < stepCount; ++i) {
            step (direction);
            double proportion = Math.abs ((halfway - i) / halfway);
            int delay = (int) Math.round (microsecondsDelayPerStep * proportion);
            log.debug ("delay: " + delay + "us");
            Utility.waitShort (delay);
        }
        return this;
    }

    /**
     *
     * @return
     */
    public StepperMotor stop () {
        motorController.runMotor (motorIdA, 0);
        motorController.runMotor (motorIdB, 0);
        return this;
    }

    /**
     *
     * @param minimumCycleDelay
     * @return
     */
    public StepperMotor setMinimumCycleDelay (int minimumCycleDelay) {
        this.minimumCycleDelay = minimumCycleDelay;
        return this;
    }

    /**
     *
     * @return
     */
    public double getResolution () {
        return (stepAngle * 4.0) / cycle.length;
    }

    /**
     *
     * @return
     */
    public String getDescription () {
        return stepperType + "-step, cycle-length (per 4 steps): " + cycle.length + ", resolution: " + String.format ("%.03f", getResolution ()) + " degrees/step";
    }
}
