package com.brettonw.PCA9685;

import com.brettonw.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

// DC and Stepper Motor Hat
// https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/overview
// this "hat" is a combination 9685 16 Channel Pulse Width Modulation Controller (PWM) for LEDs,
// and 2 6612 H-bridge motor controllers driven off the modulated outputs
// https://cdn-shop.adafruit.com/datasheets/TB6612FNG_datasheet_en_20121101.pdf
public class AdafruitMotorHat extends PCA9685 {
    protected static final Logger log = LogManager.getLogger (AdafruitMotorHat.class);

    public static final int DEFAULT_ADDRESS = 0x60;

    public enum Motor {
        MOTOR_1, MOTOR_2, MOTOR_3, MOTOR_4
    }

    public AdafruitMotorHat () {
        this (DEFAULT_ADDRESS);
    }

    public AdafruitMotorHat (int address) {
        super (address);
    }

    private void setPin (int pin, boolean high) throws IOException {
        setChannel (pin, high ? CHANNEL_HIGH : 0, high ? 0 : CHANNEL_HIGH);
    }

    private void runMotor (int modulator, int forward, int backward, double speed) {
        try {
            if (speed < 0.0) {
                setPin (forward, false);
                setPin (backward, true);
                setChannel (modulator, 0, (int) (-speed * CHANNEL_HIGH));
            } else if (speed > 0.0) {
                setPin (forward, true);
                setPin (backward, false);
                setChannel (modulator, 0, (int) (speed * CHANNEL_HIGH));
            } else if (speed == 0.0) {
                setPin (forward, false);
                setPin (backward, false);
                setChannel (modulator, 0, 0);
            }
        }
        catch (Exception exception) {
            log.error (exception);
        }
    }

    private void runMotorInternal (Motor motor, double speed) {
        switch (motor) {
            case MOTOR_1: runMotor (8, 9, 10, speed); break;
            case MOTOR_2: runMotor (13, 12, 11, speed); break;
            case MOTOR_3: runMotor (2, 3, 4, speed); break;
            case MOTOR_4: runMotor (7, 6, 5, speed); break;
        }
    }

    public AdafruitMotorHat runMotor (Motor motor, double speed) {
        // speed is just setting the PWM in the 12-bit range (0..1) = (stopped..full speed)
        log.debug (motor.name () + "@" + String.format ("%.04f", speed));
        runMotorInternal (motor, speed);
        return this;
    }

    public AdafruitMotorHat stopMotor (Motor motor) {
        log.debug (motor.name ());
        runMotorInternal (motor, 0.0);
        return this;
    }

    public enum Stepper {
        STEPPER_1, STEPPER_2
    }

    private static final int DEFAULT_DELAY = 10;

    public static class StepValue {
        public double motor1;
        public double motor2;
        public StepValue (double motor1, double motor2) {
            this.motor1 = motor1;
            this.motor2 = motor2;
        }
    }

    public static StepValue[] makeSteps (int stepsPerCycle) {
        // make and return an array of steps based on how many substeps we want
        // double check stepsPerCycle is a power of 2
        if ((stepsPerCycle & (stepsPerCycle - 1)) == 0) {
            stepsPerCycle *= 4;
            StepValue steps[] = new StepValue[stepsPerCycle];
            double stepAngle = (Math.PI * 2.0) / stepsPerCycle;
            for (int i = 0; i < stepsPerCycle; ++i) {
                double step = stepAngle * i;
                steps[i] = new StepValue (Math.cos (step), Math.sin (step));
            }
            return steps;
        }
        log.error ("Invalid stepsPerCycle - must be a power of 2");
        return null;
    }

    private void stepMotorInternal (Stepper stepper, StepValue[] steps, int stepIndex) {
        switch (stepper) {
            case STEPPER_1:
                runMotorInternal (Motor.MOTOR_1, steps[stepIndex].motor1);
                runMotorInternal (Motor.MOTOR_2, steps[stepIndex].motor2);
                break;
            case STEPPER_2:
                runMotorInternal (Motor.MOTOR_3, steps[stepIndex].motor1);
                runMotorInternal (Motor.MOTOR_4, steps[stepIndex].motor2);
                break;
        }
    }

    public AdafruitMotorHat stepMotor (Stepper stepper, StepValue[] steps, int stepIndex) {
        log.debug (stepper.name () + "@" + stepIndex);
        int mask = steps.length - 1;
        stepMotorInternal (stepper, steps, stepIndex & mask);
        return this;
    }

    public AdafruitMotorHat stepMotor (Stepper stepper, StepValue[] steps, int stepIndexStart, int stepCount, int delay) {
        log.debug (stepper.name () + "@" + stepIndexStart + " for " + stepCount + " steps");
        int stepIndexEnd = stepIndexStart + stepCount;
        int mask = steps.length - 1;
        for (int i = stepIndexStart; i < stepIndexEnd; ++i) {
            stepMotorInternal (stepper, steps, i & mask);
            Utility.waitL (delay);
        }
        return this;
    }

    public AdafruitMotorHat stepMotor (Stepper stepper, StepValue[] steps, int stepIndexStart, int stepCount) {
        return stepMotor (stepper, steps, stepIndexStart, stepCount, DEFAULT_DELAY);
    }

    public AdafruitMotorHat stepMotorWhole (Stepper stepper, StepValue[] steps, int delay) {
        return stepMotor (stepper, steps, 0, steps.length, delay);
    }

    public AdafruitMotorHat stepMotorWhole (Stepper stepper, StepValue[] steps) {
        return stepMotorWhole (stepper, steps, DEFAULT_DELAY);
    }
}
