package com.brettonw;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

// This is a controller for the PCA9685 used in the Adafruit motor hat
// https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/overview
// it is a combination 9685 16 Channel Pulse Width Modulation Controller (PWM) for LEDs,
// and 2 6612 H-bridge motor controllers driven off the modulated outputs
// https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf
// https://cdn-shop.adafruit.com/datasheets/TB6612FNG_datasheet_en_20121101.pdf
// The device is controlled over the I2C bus via the 9685
public class PCA9685x {
    private static final Logger log = LogManager.getLogger (PCA9685x.class);

    // registers (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - table 4)
    private static final int MODE1 = 0x00;
    private static final int MODE2 = 0x01;
    private static final int SUBADR1 = 0x02;
    private static final int SUBADR2 = 0x03;
    private static final int SUBADR3 = 0x04;
    private static final int PRE_SCALE = 0xFE;

    // these registers are used as the base address of the full set of supported channels,
    // technically they are LED0_ON_L, LED0_ON_H, LED0_OFF_L, and LED0_OFF_H
    private static final int CHANNEL_BASE_ON_L = 0x06;
    private static final int CHANNEL_BASE_ON_H = 0x07;
    private static final int CHANNEL_BASE_OFF_L = 0x08;
    private static final int CHANNEL_BASE_OFF_H = 0x09;

    // values used for offsetting the registers by channel, "ALL" is a special channel
    private static final int CHANNEL_OFFSET_MULTIPLIER = 4;
    private static final int CHANNEL_All = 0x3D;

    // the pulse width modulators (PWM) have 12-bit resolution
    private static final int CHANNEL_HIGH = 0x0FFF; // 4095

    // bits (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - mode 1, table 5)
    private final static int RESTART = 0x80;
    private final static int SLEEP = 0x10;
    private final static int ALLCALL = 0x01;

    // bits (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - mode 2, table 6)
    private final static int INVRT = 0x10;
    private final static int OUTDRV = 0x04;

    // default i2c address of the Adafruit server hat
    public static final int DEFAULT_ADDRESS = 0x60;

    // internal variables
    private I2CBus i2cBus;
    private I2CDevice i2cDevice;

    // internal wait functions
    public final static void waitL (long milliseconds) {
        try {
            Thread.sleep (milliseconds);
        } catch (InterruptedException exception) {
            log.error (exception);
        }
    }

    public final static void waitD (double seconds) {
        waitL (Math.round (seconds * 1_000L));
    }

    public PCA9685x () {
        this (DEFAULT_ADDRESS, DEFAULT_OUTPUT_MODULATION_FREQUENCY);
    }

    public PCA9685x (int address, int frequency) {
        try {
            // get the i2c bus, and device
            i2cBus = I2CFactory.getInstance (I2CBus.BUS_1);
            i2cDevice = i2cBus.getDevice (address);
            log.debug ("Successfully connected to i2c@" + address);

            // init, everything off
            setChannel (CHANNEL_All, 0, 0);
            i2cDevice.write (MODE2, (byte) OUTDRV);
            i2cDevice.write (MODE1, (byte) ALLCALL);
            // the chip takes 500 microseconds to recover from changes to the control registers
            waitL (1);

            // wake up
            int mode1 = i2cDevice.read (MODE1) & ~SLEEP;
            i2cDevice.write (MODE1, (byte)mode1);
            // the chip takes 500 microseconds to recover from turning off the SLEEP bit
            waitL (1);

            // setup
            setOutputModulationFrequency (frequency);
        }
        catch (Exception exception) {
            log.error ("Failure to connect to i2c@" + address, exception);
        }
    }

    private void setChannel (int channel, int on, int off) throws IOException {
        log.debug ("Set Channel (" + channel + ") - ON:" + String.format ("0x%04x", on) + ", OFF:" + String.format ("0x%04x", off));
        int channelOffset = channel * CHANNEL_OFFSET_MULTIPLIER;
        i2cDevice.write (CHANNEL_BASE_ON_L + channelOffset, (byte) (on & 0xFF));
        i2cDevice.write (CHANNEL_BASE_ON_H + channelOffset, (byte) (on >> 8));
        i2cDevice.write (CHANNEL_BASE_OFF_L + channelOffset, (byte) (off & 0xFF));
        i2cDevice.write (CHANNEL_BASE_OFF_H + channelOffset, (byte) (off >> 8));
    }

    private void setPin (int pin, boolean high) throws IOException {
        setChannel (pin, high ? CHANNEL_HIGH : 0, high ? 0 : CHANNEL_HIGH);
    }

    // values used for setting the modulation update frequency
    private final static int DEFAULT_OUTPUT_MODULATION_FREQUENCY = 1_600;
    private static final double CLOCK_FREQUENCY = 25_000_000.0; // 25MHz
    private static final double CHANNEL_RESOLUTION = 4_096.0;   // 12-bit precision
    private static final int MIN_PRE_SCALE = 0x03;
    private static final int MAX_PRE_SCALE = 0xFF;

    private void setOutputModulationFrequency (int frequency) throws IOException {
        // (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - Section 7.3.5)
        int preScale = ((int) (Math.round (CLOCK_FREQUENCY / (CHANNEL_RESOLUTION * frequency)))) - 1;
        log.debug ("Setting modulation update frequency to " + frequency + " Hz, (pre-scale:" + String.format ("0x%02x", preScale) + ")");

        // PRE_SCALE can only be set when the SLEEP bit of the MODE1 register is set to logic 1.
        int oldMode = i2cDevice.read (MODE1);
        byte newMode = (byte) ((oldMode & 0x7F) | SLEEP);
        i2cDevice.write (MODE1, newMode);
        i2cDevice.write (PRE_SCALE, (byte) (Math.floor (preScale)));
        i2cDevice.write (MODE1, (byte) oldMode);

        // SLEEP bit must be 0 for at least 500 ms before 1 is written into the RESTART bit.
        waitL (1);
        i2cDevice.write (MODE1, (byte) (oldMode | RESTART));
    }

    public enum Motor {
        MOTOR_1, MOTOR_2, MOTOR_3, MOTOR_4
    }

    private void runMotor (int modulator, int forward, int backward, double speed) {
        try {
            if (speed < 0.0) {
                setPin (forward, false);
                setPin (backward, true);
                setChannel (modulator, 0, (int) (-speed * (CHANNEL_RESOLUTION - 1.0)));
            } else if (speed > 0.0) {
                setPin (forward, true);
                setPin (backward, false);
                setChannel (modulator, 0, (int) (speed * (CHANNEL_RESOLUTION - 1.0)));
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

    public void runMotor (Motor motor, double speed) {
        log.debug ("Run " + motor.name () + "@" + speed);
        runMotorInternal (motor, speed);
    }

    public void stopMotor (Motor motor) {
        log.debug ("Stop " + motor.name ());
        runMotorInternal (motor, 0.0);
    }

    public enum StepType {
        SINGLE, DOUBLE, INTERLEAVE, MICRO
    }

    public enum StepperMotor {
        STEPPER_MOTOR_1, STEPPER_MOTOR_2
    }


}
