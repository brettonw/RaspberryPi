package com.brettonw.PCA9685;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Motor
 *
 * a simple motor class controlled by PCM through the adafruit motorhat
 */
public class Motor {
    protected static final Logger log = LogManager.getLogger (Motor.class);

    private MotorController controller;
    private MotorId motorId;

    /**
     *
     * @param controller
     * @param motorId
     */
    public Motor (MotorController controller, MotorId motorId) {
        this.controller = controller;
        this.motorId = motorId;
    }

    /**
     *
     * @param speed
     * @return
     */
    public Motor run (double speed) {
        // speed is just setting the PWM in the 12-bit range (0..1) = (stopped..full speed)
        log.trace (motorId.name () + "@" + String.format ("%.04f", speed));
        controller.runMotor (motorId, speed);
        return this;
    }

    /**
     *
     * @return
     */
    public Motor stop () {
        log.debug (motorId.name ());
        controller.runMotor (motorId, 0.0);
        return this;
    }
}
