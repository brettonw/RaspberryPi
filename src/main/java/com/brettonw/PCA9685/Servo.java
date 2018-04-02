package com.brettonw.PCA9685;

import com.brettonw.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Servo
 * <p>
 * analog servos have 3-wire inputs, two power leads and a signal line. they work by sending a timed
 * pulse on the signal line at a fixed frequency (nominal 50Hz), which signals the position the
 * servo should be at.
 * <p>
 * for example:
 * http://www.micropik.com/PDF/SG90Servo.pdf
 */
public class Servo {
    private static final Logger log = LogManager.getLogger (StepperMotor.class);

    private ServoController servoController;
    private ServoId servoId;
    private double min;
    private double max;
    private double position;

    /**
     * @param servoController the controller to use for this servo
     * @param servoId         the id corresponding to the driver pins for this servo on the controller
     * @param min             the pulse width in ms corresponding to the min position
     * @param max             the pulse width in ms corresponding to the max position
     */
    public Servo (ServoController servoController, ServoId servoId, double min, double max) {
        this.servoController = servoController;
        this.servoId = servoId;
        this.min = min;
        this.max = max;
        setPosition (0);
    }

    /**
     * @param position - in the range 0..1
     * @return
     */
    public Servo setPosition (double position) {
        this.position = position = Utility.clamp (position, 0, 1);
        double pulseDurationMilliseconds = min + ((max - min) * position);
        log.trace (servoId.name () + "@" + String.format ("%.04f", pulseDurationMilliseconds));
        servoController.setPulseDuration (servoId, pulseDurationMilliseconds);
        return this;
    }

    /**
     * @return
     */
    public double getPosition () {
        return position;
    }

    /**
     * @return
     */
    public ServoId getServoId () {
        return servoId;
    }
}
