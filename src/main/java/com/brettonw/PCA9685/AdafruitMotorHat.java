package com.brettonw.PCA9685;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

// DC and Stepper Motor Hat
// https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/overview
// this "hat" is a combination 9685 16 Channel Pulse Width Modulation Controller (PWM) for
// LEDs, and 2 6612 H-bridge motor controllers driven off the modulated outputs. the "hat"
// supports four motors (a stepper motor is driven as if it were two motors)
// https://cdn-shop.adafruit.com/datasheets/TB6612FNG_datasheet_en_20121101.pdf
public class AdafruitMotorHat extends PCA9685 {
    protected static final Logger log = LogManager.getLogger (AdafruitMotorHat.class);

    public static final int DEFAULT_ADDRESS = 0x60;

    public AdafruitMotorHat () {
        this (DEFAULT_ADDRESS);
    }

    public AdafruitMotorHat (int address) {
        super (address);
    }

    private void setPin (int pin, boolean high) throws IOException {
        setChannelPulse (pin, high ? CHANNEL_HIGH : 0, high ? 0 : CHANNEL_HIGH);
    }

    private void runMotor (int modulator, int frontPin, int backPin, double speed) {
        try {
            if (speed < 0.0) {
                setPin (frontPin, false);
                setPin (backPin, true);
                setChannelPulse (modulator, 0, (int) (-speed * CHANNEL_HIGH));
            } else if (speed > 0.0) {
                setPin (frontPin, true);
                setPin (backPin, false);
                setChannelPulse (modulator, 0, (int) (speed * CHANNEL_HIGH));
            } else if (speed == 0.0) {
                setPin (frontPin, false);
                setPin (backPin, false);
                setChannelPulse (modulator, 0, 0);
            }
        }
        catch (Exception exception) {
            log.error (exception);
        }
    }

    public void runMotor (MotorId motorId, double speed) {
        switch (motorId) {
            case MOTOR_1: runMotor (8, 9, 10, speed); break;
            case MOTOR_2: runMotor (13, 12, 11, speed); break;
            case MOTOR_3: runMotor (2, 3, 4, speed); break;
            case MOTOR_4: runMotor (7, 6, 5, speed); break;
        }
    }

    /*
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

    private static final Map<Stepper, MotorId[]> STEPPER_MOTOR_INDEX = new HashMap <Stepper, MotorId[]> (2) {{
        put (Stepper.STEPPER_1, new MotorId[]{ MotorId.MOTOR_1, MotorId.MOTOR_2 });
        put (Stepper.STEPPER_2, new MotorId[]{ MotorId.MOTOR_3, MotorId.MOTOR_4 });

    }};

    private void stepMotorInternal (Stepper stepper, StepValue[] steps, int stepIndex) {
        // ensure that the step index is valid
        int mask = steps.length - 1;
        do {
            stepIndex = (stepIndex + steps.length) & mask;
        }
        while (stepIndex < 0);
        runMotorInternal (STEPPER_MOTOR_INDEX.get (stepper)[0], steps[stepIndex].motor1);
        runMotorInternal (STEPPER_MOTOR_INDEX.get (stepper)[1], steps[stepIndex].motor2);
    }

    public AdafruitMotorHat stepMotor (Stepper stepper, StepValue[] steps, int stepIndex) {
        log.debug (stepper.name () + "@" + stepIndex);
        stepMotorInternal (stepper, steps, stepIndex);
        return this;
    }

    public AdafruitMotorHat stepMotor (Stepper stepper, StepValue[] steps, int stepIndexStart, int stepCount, int delay) {
        log.debug (stepper.name () + "@" + stepIndexStart + " for " + stepCount + " steps");
        int stepIndexEnd = stepIndexStart + stepCount;
        for (int i = stepIndexStart; i < stepIndexEnd; ++i) {
            stepMotorInternal (stepper, steps, i);
            if (delay >= 0) {
                Utility.waitL (delay);
            }
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

    public AdafruitMotorHat stepMotorStop (Stepper stepper) {
        switch (stepper) {
            case STEPPER_1:
                runMotorInternal (MotorId.MOTOR_1, 0);
                runMotorInternal (MotorId.MOTOR_2, 0);
                break;
            case STEPPER_2:
                runMotorInternal (MotorId.MOTOR_3, 0);
                runMotorInternal (MotorId.MOTOR_4, 0);
                break;
        }
        return this;
    }
    */
}
