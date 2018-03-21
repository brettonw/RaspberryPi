package com.brettonw.PCA9685;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

// This is a software interface for the PCA9685. It is a 16-channel Pulse Width Modulator (PWM)
// Controller (designed to drive LEDs) with 12 bits of resolution, and controlled over the I2C bus.
// The 9685 is used in the Adafruit motor hat and the servo driver board
// https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf
public class PCA9685 {
    protected static final Logger log = LogManager.getLogger (PCA9685.class);

    // registers (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - table 4)
    protected static final int MODE1 = 0x00;
    protected static final int MODE2 = 0x01;
    protected static final int SUBADR1 = 0x02;
    protected static final int SUBADR2 = 0x03;
    protected static final int SUBADR3 = 0x04;
    protected static final int PRE_SCALE = 0xFE;

    // these registers are used as the base address of the full set of supported channels,
    // technically they are LED0_ON_L, LED0_ON_H, LED0_OFF_L, and LED0_OFF_H
    protected static final int CHANNEL_BASE_ON_L = 0x06;
    protected static final int CHANNEL_BASE_ON_H = 0x07;
    protected static final int CHANNEL_BASE_OFF_L = 0x08;
    protected static final int CHANNEL_BASE_OFF_H = 0x09;

    // values used for offsetting the registers by channel, "ALL" is a special channel
    protected static final int CHANNEL_OFFSET_MULTIPLIER = 4;
    protected static final int CHANNEL_All = 0x3D;

    // the pulse width modulators (PWM) have 12-bit resolution
    protected static final int CHANNEL_HIGH = 0x0FFF; // 4095

    // bits (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - mode 1, table 5)
    protected final static int RESTART = 0x80;
    protected final static int SLEEP = 0x10;
    protected final static int ALLCALL = 0x01;

    // bits (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - mode 2, table 6)
    protected final static int INVRT = 0x10;
    protected final static int OUTDRV = 0x04;

    // internal variables
    protected I2CBus i2cBus;
    protected I2CDevice i2cDevice;

    public PCA9685 (int address) {
        this (address, DEFAULT_OUTPUT_MODULATION_FREQUENCY);
    }

    public PCA9685 (int address, int frequency) {
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
            Thread.sleep (1);

            // wake up
            int mode1 = i2cDevice.read (MODE1) & ~SLEEP;
            i2cDevice.write (MODE1, (byte)mode1);
            // the chip takes 500 microseconds to recover from turning off the SLEEP bit
            Thread.sleep (1);

            // setup
            setOutputModulationFrequency (frequency);
        }
        catch (Exception exception) {
            log.error ("Failure to connect to i2c@" + address, exception);
            i2cDevice = null;
        }
    }

    public boolean hasDevice () {
        return (i2cDevice != null);
    }

    protected void setChannel (int channel, int on, int off) throws IOException {
        log.trace ("Set Channel (" + channel + ") - ON:" + String.format ("0x%04x", on) + ", OFF:" + String.format ("0x%04x", off));
        int channelOffset = channel * CHANNEL_OFFSET_MULTIPLIER;
        i2cDevice.write (CHANNEL_BASE_ON_L + channelOffset, (byte) (on & 0xFF));
        i2cDevice.write (CHANNEL_BASE_ON_H + channelOffset, (byte) (on >> 8));
        i2cDevice.write (CHANNEL_BASE_OFF_L + channelOffset, (byte) (off & 0xFF));
        i2cDevice.write (CHANNEL_BASE_OFF_H + channelOffset, (byte) (off >> 8));
    }

    // values used for setting the modulation update frequency
    private final static int DEFAULT_OUTPUT_MODULATION_FREQUENCY = 1_600;
    private static final double CLOCK_FREQUENCY = 25_000_000.0; // 25MHz
    private static final double CHANNEL_RESOLUTION = 4_096.0;   // 12-bit precision
    private static final int MIN_PRE_SCALE = 0x03;
    private static final int MAX_PRE_SCALE = 0xFF;

    private void setOutputModulationFrequency (int frequency) throws IOException {
        try {
            // (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - Section 7.3.5)
            int preScale = ((int) (Math.round (CLOCK_FREQUENCY / (CHANNEL_RESOLUTION * frequency)))) - 1;
            preScale = Math.min (Math.max (MIN_PRE_SCALE, preScale), MAX_PRE_SCALE);
            log.debug ("Setting modulation update frequency to " + frequency + " Hz, (pre-scale:" + String.format ("0x%02x", preScale) + ")");

            // PRE_SCALE can only be set when the SLEEP bit of the MODE1 register is set to logic 1.
            int oldMode = i2cDevice.read (MODE1);
            byte newMode = (byte) ((oldMode & 0x7F) | SLEEP);
            i2cDevice.write (MODE1, newMode);
            i2cDevice.write (PRE_SCALE, (byte) (Math.floor (preScale)));
            i2cDevice.write (MODE1, (byte) oldMode);

            // SLEEP bit must be 0 for at least 500us before 1 is written into the RESTART bit.
            Thread.sleep (1);
            i2cDevice.write (MODE1, (byte) (oldMode | RESTART));
        } catch (InterruptedException exception) {
            log.error (exception);
        }
    }


}
