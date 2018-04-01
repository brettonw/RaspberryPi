package com.brettonw.PCA9685;

import com.brettonw.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Servo
 *
 * analog servos have 3-wire inputs, two power leads and a signal line. they work by sending a timed
 * pulse on the signal line at a fixed frequency (nominal 50Hz), which signals the position the
 * servo should be at.
 *
 * for example:
 * http://www.micropik.com/PDF/SG90Servo.pdf
 */
public class Servo {
    protected static final Logger log = LogManager.getLogger (StepperMotor.class);

    private ServoController servoController;
    private int servoId;
    private double min;
    private double max;

    public Servo (ServoController servoController, int servoId, double min, double max) {
        this.servoController = servoController;
        this.servoId = servoId;
        this.min = min;
        this.max = max;
    }

    /**
     *
     * @param position - in the range 0..1
     * @return
     */
    public Servo setPosition (double position) {
        servoController.setPulseDuration (servoId, min + (Utility.clamp (position, 0, 1) * (max - min)));
        return this;
    }
}
