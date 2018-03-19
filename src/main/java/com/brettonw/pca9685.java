package com.brettonw;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

// This is a controller for the Adafruit motor hat
// https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/overview
// it is a combination 9685 16 Channel Pulse Width Modulation Controller (PWM) for LEDs,
// and 2 6612 H-bridge motor controllers
// https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf
// https://cdn-shop.adafruit.com/datasheets/TB6612FNG_datasheet_en_20121101.pdf
// The device is controlled over the I2C bus via the 9685
public class pca9685 {
    private static final Logger log = LogManager.getLogger (pca9685.class);

    // registers (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - table 4)
    public enum Register {
        // table 4
        MODE1 (0x00),
        MODE2 (0x01),
        SUBADR1 (0x02),
        SUBADR2 (0x03),
        SUBADR3 (0x04),

        // these are used as the base address of the full set of supported channels
        LED0_ON_L (0x06),
        LED0_ON_H (0x07),
        LED0_OFF_L (0x08),
        LED0_OFF_H (0x09),

        ALL_LED_ON_L (0xFA),
        ALL_LED_ON_H (0xFB),
        ALL_LED_OFF_L (0xFC),
        ALL_LED_OFF_H (0xFD),
        PRE_SCALE (0xFE);

        private final int register;

        private Register (int register) {
            this.register = register;
        }

        public int getRegister () { return register; }

        public void write (I2CDevice i2cDevice, byte value) throws IOException {
            log.info ("Write " + String.format ("0x%02x", value) + " to register " + name () + " (" + String.format ("0x%02x", register) + ")");
            i2cDevice.write (register, value);
        }

        public void write (I2CDevice i2cDevice, int channel, byte value) throws IOException {
            log.info ("Write " + String.format ("0x%02x", value) + " to register " + name () + " + Channel " + channel + " (" + String.format ("0x%02x", register) + ")");
            i2cDevice.write (register + (4 * channel), value);
        }

        public int read (I2CDevice i2cDevice) throws IOException {
            return i2cDevice.read (register);
        }
    }

    // bits (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - mode 1, table 5)
    public final static int RESTART = 0x80;
    public final static int SLEEP = 0x10;
    public final static int ALLCALL = 0x01;

    // bits (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - mode 2, table 6)
    public final static int INVRT = 0x10;
    public final static int OUTDRV = 0x04;

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

    public final static void waitF (float seconds) {
        waitL (Math.round (seconds * 1_000L));
    }

    public pca9685 (int address) {
        try {
            // get the i2c bus, and device
            i2cBus = I2CFactory.getInstance (I2CBus.BUS_1);
            i2cDevice = i2cBus.getDevice (address);
            log.debug ("Successfully connected to i2c@" + address);

            // init
            setAllChannels ((byte) 0, (byte) 0);
            Register.MODE2.write (i2cDevice, (byte) OUTDRV);
            Register.MODE1.write (i2cDevice, (byte) ALLCALL);
            waitL (5);

            // wake it up
            int mode1 = Register.MODE1.read (i2cDevice) & ~ SLEEP;
            Register.MODE1.write (i2cDevice, (byte)mode1);
            waitL (5);
        }
        catch (Exception exception) {
            log.error ("Failure to connect to i2c@" + address, exception);
        }
    }

    private void write (int register, byte value) throws IOException {
        log.info ("Write " + String.format ("0x%02x", value) + " to register (" + String.format ("0x%02x", register) + ")");
        i2cDevice.write (register, value);
    }

    public void setAllChannels (byte on, byte off) throws IOException {
        log.info ("Set All Channels - ON:" + String.format ("0x%02x", on) + ", OFF:" + String.format ("0x%02x", off));
        Register.ALL_LED_ON_L.write (i2cDevice, (byte) (on & 0xFF));
        Register.ALL_LED_ON_H.write (i2cDevice, (byte) (on >> 8));
        Register.ALL_LED_OFF_L.write (i2cDevice, (byte) (off & 0xFF));
        Register.ALL_LED_OFF_H.write (i2cDevice, (byte) (off >> 8));
    }

    public void setChannel (int channel, byte on, byte off) throws IOException {
        log.info ("Set Channel (" + channel + ") - ON:" + String.format ("0x%02x", on) + ", OFF:" + String.format ("0x%02x", off));
        Register.LED0_ON_L.write (i2cDevice, channel, (byte) (on & 0xFF));
        Register.LED0_ON_L.write (i2cDevice, channel, (byte) (on >> 8));
        Register.LED0_ON_L.write (i2cDevice, channel, (byte) (off & 0xFF));
        Register.LED0_ON_L.write (i2cDevice, channel, (byte) (off >> 8));
    }

    public void setPwmFrequencyPreScale (int frequency) throws IOException {
        // sets the PWM frequency (https://cdn-shop.adafruit.com/datasheets/PCA9685.pdf - Section 7.3.5)
        double preScale = 25_000_000.0 / 4096.0; // 25MHz @ 12-bit precision
        preScale = Math.floor (((preScale / (double) frequency) - 1.0) + 0.5);
        log.info ("Setting PWM frequency to " + frequency + " Hz, (pre-scale:" + preScale + ")");

        int oldMode = Register.MODE1.read (i2cDevice);
        byte newMode = (byte) ((oldMode & 0x7F) | SLEEP);
        Register.MODE1.write (i2cDevice, newMode);
        Register.PRE_SCALE.write (i2cDevice, (byte) (Math.floor (preScale)));
        Register.MODE1.write (i2cDevice, (byte) oldMode);

        // The SLEEP bit must be logic 0 for at least 500 ms, before a logic 1 is
        // written into the RESTART bit.
        waitL (5);
        Register.MODE1.write (i2cDevice, (byte) (oldMode | RESTART));
    }

    public pca9685 () {
        this (DEFAULT_ADDRESS);
    }
}
