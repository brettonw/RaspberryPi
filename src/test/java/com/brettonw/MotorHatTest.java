package com.brettonw;

import com.olivierld.AdafruitMotorHat;
import com.pi4j.io.i2c.I2CFactory;
import org.junit.Test;

import java.io.IOException;

import static com.olivierld.TimeUtil.delay;

/**
 * DC Motor demo
 * <p>
 * I used motors like https://www.adafruit.com/product/2941
 * or https://www.adafruit.com/product/711
 */
public class MotorHatTest {
    // The I2C address of the motor HAT, default is 0x60.
    private int addr = 0x60;
    // The ID of the left motor, default is 1.
    private static AdafruitMotorHat.Motor motorID = AdafruitMotorHat.Motor.M1;
    // Amount to offset the speed of the left motor, can be positive or negative and use
    // useful for matching the speed of both motors.  Default is 0.
    private int trim = 0;

    private AdafruitMotorHat mh;
    private AdafruitMotorHat.AdafruitDCMotor motor;

    public MotorHatTest () throws I2CFactory.UnsupportedBusNumberException {
        this.mh = new AdafruitMotorHat ();
        this.motor = mh.getMotor (motorID);
        try {
            this.motor.run (AdafruitMotorHat.ServoCommand.RELEASE);
        } catch (IOException ioe) {
            ioe.printStackTrace ();
        }
        // if stopOnExit...
    }

    public void stop () {
        try {
            this.motor.run (AdafruitMotorHat.ServoCommand.RELEASE);
        } catch (IOException ioe) {
            ioe.printStackTrace ();
        }
    }

    public void setSpeed (int speed) throws IllegalArgumentException, IOException {
        if (speed < 0 || speed > 255) {
            throw new IllegalArgumentException ("Speed must be an int belonging to [0, 255]");
        }
        int leftSpeed = speed + this.trim;
        leftSpeed = Math.max (0, Math.min (255, leftSpeed));
        this.motor.setSpeed (leftSpeed);
    }

    public void forward (int speed) throws IOException {
        forward (speed, 0);
    }

    public void forward (int speed, float seconds) throws IOException {
        this.motor.setSpeed (speed);
        this.motor.run (AdafruitMotorHat.ServoCommand.FORWARD);
        if (seconds > 0) {
            delay (seconds);
            this.stop ();
        }
    }

    public void backward (int speed) throws IOException {
        backward (speed, 0);
    }

    public void backward (int speed, float seconds) throws IOException {
        this.motor.setSpeed (speed);
        this.motor.run (AdafruitMotorHat.ServoCommand.BACKWARD);
        if (seconds > 0) {
            delay (seconds);
            this.stop ();
        }
    }

    @Test
    public void testMotorHat () throws Exception {
        motorID = AdafruitMotorHat.Motor.M1;
        MotorHatTest omd = new MotorHatTest ();

        Runtime.getRuntime ().addShutdownHook (new Thread (() -> {
            System.out.println ("Oops!");
            omd.stop ();
        }));

        int speed = 100; // 0..255
        System.out.println ("Forward 100...");
        omd.forward (speed, 5f);
        System.out.println ("Backward 100...");
        omd.backward (speed, 5f);

        speed = 50;
        System.out.println ("Forward 50...");
        omd.forward (speed, 5f);
        System.out.println ("Backward 50...");
        omd.backward (speed, 5f);

        // Speed variation test
        System.out.println ("Accelerating...");
        for (speed = 0; speed <= 255; speed++) {
            System.out.println (String.format ("Speed %d", speed));
            omd.forward (speed);
            delay (0.05f);
        }
        System.out.println ("De-celarating...");
        for (speed = 255; speed >= 0; speed--) {
            System.out.println (String.format ("Speed %d", speed));
            omd.forward (speed);
            delay (0.05f);
        }
        omd.stop ();

        System.out.println ("Done.");
    }
}
