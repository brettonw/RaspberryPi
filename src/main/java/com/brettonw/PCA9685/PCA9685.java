package com.brettonw.PCA9685;

import com.brettonw.Utility;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * This is a software interface for the PCA9685. It is a 16-channel Pulse Width Modulator (PWM)
 * Controller (designed to drive LEDs) with 12 bits of resolution, and controlled over the I2C bus.
 * The 9685 is used in the Adafruit motor hat and the servo driver board
 * <p>
 * https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf
 */
public class PCA9685 {
    private static final Logger log = LogManager.getLogger (PCA9685.class);

    // registers (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - table 4)
    protected static final int MODE1 = 0x00;
    protected static final int MODE2 = 0x01;
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
    protected static final int CHANNEL_FORCE = 0x1000; // 4096

    // bits (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - mode 1, table 5)
    protected final static int RESTART = 0x80;
    protected final static int SLEEP = 0x10;
    protected final static int ALLCALL = 0x01;

    // bits (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - mode 2, table 6)
    protected final static int OUTDRV = 0x04;

    // internal variables
    protected I2CDevice i2cDevice;
    protected double pulseFrequency;

    /**
     *
     * @param address
     */
    public PCA9685 (int address) {
        this (address, DEFAULT_PULSE_FREQUENCY);
    }

    private static I2CDevice connect (int address) {
        // get the i2c bus, and device
        I2CDevice i2cDevice = null;
        try {
            i2cDevice = I2CFactory.getInstance (I2CBus.BUS_1).getDevice (address);
            log.debug ("Successfully connected to PCA9685 on i2c@" + address);
        } catch (Exception exception) {
            log.error ("Failure to connect to i2c@" + address, exception);
        }
        return i2cDevice;
    }

    /**
     *
     * @param address
     * @param pulseFrequency
     */
    public PCA9685 (int address, int pulseFrequency) {
        this (connect (address), pulseFrequency);
    }

    /**
     *
     * @param i2cDevice
     */
    public PCA9685 (I2CDevice i2cDevice) {
        this (i2cDevice, DEFAULT_PULSE_FREQUENCY);
    }

    /**
     *
     * @param i2cDevice
     * @param pulseFrequency
     */
    public PCA9685 (I2CDevice i2cDevice, int pulseFrequency) {
        try {
            this.i2cDevice = i2cDevice;

            // init, everything off
            setChannelPulse (CHANNEL_All, 0, 0);
            i2cDevice.write (MODE2, (byte) OUTDRV);
            i2cDevice.write (MODE1, (byte) ALLCALL);
            // the chip takes 500 microseconds to recover from changes to the control registers
            Thread.sleep (1);

            // wake up
            int mode1 = i2cDevice.read (MODE1) & ~SLEEP;
            i2cDevice.write (MODE1, (byte) mode1);
            // the chip takes 500 microseconds to recover from turning off the SLEEP bit
            Thread.sleep (1);

            // setup
            setPulseFrequency (pulseFrequency);
        } catch (Exception exception) {
            log.error (exception);
        }
    }

    public boolean hasDevice () {
        return (i2cDevice != null);
    }

    /**
     * set a channel's pulse parameters - this applies per tick of the clock (set by the
     * pulse frequency).
     *
     * @param channel - which channel of the PCM will be updated
     * @param on      - when to turn the pulse on within the tick, out of 4096, control
     *                bit 4 (value 4096) will force the output on for the whole cycle
     * @param off     - when to turn the pulse off within the tick, out of 4096, off
     *                should be greater than on, but control bit 4 (value 4096) will force
     *                the output off for the whole
     * @throws IOException
     */
    protected void setChannelPulse (int channel, int on, int off) {
        // (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - Section 7.3.3)
        try {
            log.trace (channel + " - ON:" + String.format ("0x%05x", on) + ", OFF:" + String.format ("0x%05x", off));
            int channelOffset = channel * CHANNEL_OFFSET_MULTIPLIER;
            i2cDevice.write (CHANNEL_BASE_ON_L + channelOffset, (byte) (on & 0x00FF));
            i2cDevice.write (CHANNEL_BASE_ON_H + channelOffset, (byte) (on >> 8));
            i2cDevice.write (CHANNEL_BASE_OFF_L + channelOffset, (byte) (off & 0x00FF));
            i2cDevice.write (CHANNEL_BASE_OFF_H + channelOffset, (byte) (off >> 8));
        } catch (IOException exception) {
            log.error (exception);
        }
    }

    /**
     * set a channel's pulse parameters - this applies per tick of the clock (set by the
     * pulse frequency).
     *
     * @param channel - which channel of the PCM will be updated
     * @param width   - proportion of the pulse to be on, 0-4_095
     * @throws IOException
     */
    protected void setChannelPulse (int channel, int width) {
        switch (width) {
            case 0:
                setChannelPulse (channel, 0, CHANNEL_FORCE);
                break;
            case CHANNEL_HIGH:
                setChannelPulse (channel, CHANNEL_FORCE, 0);
                break;
            default:
                setChannelPulse (channel, 0, width);
                break;
        }
    }

    protected void setChannelOn (int channel) {
        setChannelPulse (channel, CHANNEL_FORCE, 0);
    }

    protected void setChannelOff (int channel) {
        setChannelPulse (channel, 0, CHANNEL_FORCE);
    }

    protected void setChannelPulseMs (int channel, double milliseconds) {
        int width = (int) Math.round ((CHANNEL_HIGH * milliseconds * pulseFrequency) / 1.0e3);
        setChannelPulse (channel, width);
    }

    // values used for setting the pulse frequency, the default is 1ms per cycle
    private final static int DEFAULT_PULSE_FREQUENCY = 1_000;
    private static final double CLOCK_FREQUENCY = 25_000_000.0; // 25MHz
    private static final double CHANNEL_RESOLUTION = 4_096.0;   // 12-bit precision
    private static final int MIN_PRE_SCALE = 0x03;
    private static final int MAX_PRE_SCALE = 0xFF;

    /**
     * Set the frequency of pulses across the whole controller - each channel has 12-bits
     * of resolution (4,096 division) for setting the pulse duration within the cycle
     *
     * @param requestedPulseFrequency requested number of pulses per second for the whole board,
     *                                value in Hertz (Hz). The code tries to accomodate the request
     *                                as best as it can.
     */
    public void setPulseFrequency (int requestedPulseFrequency) {
        try {
            // (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - Section 7.3.5)
            int preScale = ((int) (Math.round (CLOCK_FREQUENCY / (CHANNEL_RESOLUTION * requestedPulseFrequency)))) - 1;
            log.trace ("pre-scale:" + String.format ("0x%02x", preScale));
            preScale = Math.min (Math.max (MIN_PRE_SCALE, preScale), MAX_PRE_SCALE);
            log.debug ("@" + requestedPulseFrequency + " Hz, (pre-scale:" + String.format ("0x%02x", preScale) + ")");

            // compute the *actual* pulse frequency by inverting the equation
            pulseFrequency = (CLOCK_FREQUENCY / (CHANNEL_RESOLUTION * (preScale + 1)));
            log.debug ("@" + String.format ("%.04f", pulseFrequency) + "Hz (actual)");

            // PRE_SCALE can only be set when the SLEEP bit of the MODE1 register is set to logic 1.
            int oldMode = i2cDevice.read (MODE1);
            byte newMode = (byte) ((oldMode & 0x7F) | SLEEP);
            i2cDevice.write (MODE1, newMode);
            i2cDevice.write (PRE_SCALE, (byte) (Math.floor (preScale)));
            i2cDevice.write (MODE1, (byte) oldMode);

            // SLEEP bit must be 0 for at least 500us before 1 is written into the RESTART bit.
            Utility.waitL (1);
            i2cDevice.write (MODE1, (byte) (oldMode | RESTART));
        } catch (IOException exception) {
            log.error (exception);
        }
    }
}
