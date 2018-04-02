package com.brettonw.PCA9685;

import com.brettonw.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Motor
 * <p>
 * brushed motors have relatively simple controllers that use two wires, and switch the power on and
 * off really fast using a modulated width pulse.
 */
public class Motor {
    private static final Logger log = LogManager.getLogger (Motor.class);

    private MotorController controller;
    private MotorId motorId;
    private double speed;

    /**
     * @param controller - the motor controller to use
     * @param motorId    - the id corresponding to the driver pins for this motor on the controller
     */
    public Motor (MotorController controller, MotorId motorId) {
        this.controller = controller;
        this.motorId = motorId;
        stop ();
    }

    /**
     * @param speed - setting the speed at the controller (-1..1)
     *              -1 = full speed backward, 0 = stopped, 1 = full speed forward
     * @return
     */
    public Motor run (double speed) {
        this.speed = speed = Utility.clamp (speed, -1, 1);
        log.trace (motorId.name () + "@" + String.format ("%.04f", speed));
        controller.runMotor (motorId, speed);
        return this;
    }

    /**
     * @return
     */
    public Motor stop () {
        speed = 0;
        log.trace (motorId.name ());
        controller.runMotor (motorId, speed);
        return this;
    }

    /**
     * @return
     */
    public MotorId getMotorId () {
        return motorId;
    }

    /**
     * @return
     */
    public double getSpeed () {
        return speed;
    }
}
