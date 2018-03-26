package com.brettonw.PCA9685;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Motor {
    protected static final Logger log = LogManager.getLogger (Motor.class);

    private AdafruitMotorHat controller;
    private MotorId motorId;

    public Motor (AdafruitMotorHat controller, MotorId motorId) {
        this.controller = controller;
        this.motorId = motorId;
    }

    public Motor run (double speed) {
        // speed is just setting the PWM in the 12-bit range (0..1) = (stopped..full speed)
        log.debug (motorId.name () + "@" + String.format ("%.04f", speed));
        controller.runMotor (motorId, speed);
        return this;
    }

    public Motor stop () {
        log.debug (motorId.name ());
        controller.runMotor (motorId, 0.0);
        return this;
    }
}
