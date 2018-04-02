package com.brettonw.PCA9685.Tester;

import com.pi4j.io.i2c.I2CDevice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class I2CDeviceTester implements I2CDevice {
    private static final Logger log = LogManager.getLogger (I2CDeviceTester.class);

    // internal class for expected list
    class Expectation {
        int address;
        byte b;

        Expectation (int address, byte b) {
            this.address = address;
            this.b = b;
        }

        boolean satisfy (int address, byte b) {
            return (address == this.address) && (b == this.b);
        }
    }

    private List<Expectation> expectations;
    private String errorState;

    public I2CDeviceTester () {
        expectations = new ArrayList<> (10);
    }

    @Override
    public int getAddress () {
        return 0;
    }

    @Override
    public void write (byte b) throws IOException {

    }

    @Override
    public void write (byte[] buffer, int offset, int size) throws IOException {

    }

    @Override
    public void write (byte[] buffer) throws IOException {

    }

    @Override
    public void write (int address, byte b) throws IOException {
        // get the current expectation
        if (expectations.size () > 0) {
            Expectation currentExpectation = expectations.remove (0);
            if (currentExpectation.satisfy (address, b)) {
                //log.debug ("EXPECTED! (" + String.format ("0x%04x", address) + ", " + String.format ("0x%02x", b) + ")");
            } else {
                log.debug ("UNSATISFIED! .expect (" + String.format ("0x%04x", address) + ", (byte)" + String.format ("0x%02x", b) + ")");
                errorState = "Unsatisfied expectation.";
            }
        } else {
            log.debug ("UNEXPECTED! .expect (" + String.format ("0x%04x", address) + ", (byte)" + String.format ("0x%02x", b) + ")");
            errorState = "Unexpected write.";
        }
    }

    @Override
    public void write (int address, byte[] buffer, int offset, int size) throws IOException {

    }

    @Override
    public void write (int address, byte[] buffer) throws IOException {

    }

    @Override
    public int read () throws IOException {
        return 0;
    }

    @Override
    public int read (byte[] buffer, int offset, int size) throws IOException {
        return 0;
    }

    @Override
    public int read (int address) throws IOException {
        return 0;
    }

    @Override
    public int read (int address, byte[] buffer, int offset, int size) throws IOException {
        return 0;
    }

    @Override
    public void ioctl (long command, int value) throws IOException {

    }

    @Override
    public void ioctl (long command, ByteBuffer data, IntBuffer offsets) throws IOException {

    }

    @Override
    public int read (byte[] writeBuffer, int writeOffset, int writeSize, byte[] readBuffer, int readOffset, int readSize) throws IOException {
        return 0;
    }

    public I2CDeviceTester expect (int address, byte b) {
        expectations.add (new Expectation (address, b));

        // clear any errors
        errorState = null;
        return this;
    }

    public void report () throws Exception {
        if (errorState == null) {
            int count = expectations.size ();
            if (expectations.size () > 0) {
                Expectation currentExpectation = expectations.remove (0);
                log.debug ("UNMET! .expect (" + String.format ("0x%04x", currentExpectation.address) + ", (byte)" + String.format ("0x%02x", currentExpectation.b) + ")");
            }
            if (count > 0) {
                throw new Exception ("Unmet expectation" + ((count > 1) ? "s" : "") + " (" + count + ")");
            }
        } else {
            throw new Exception (errorState);
        }
    }
}
