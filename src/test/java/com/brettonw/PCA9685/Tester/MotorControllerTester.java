package com.brettonw.PCA9685.Tester;

import com.brettonw.PCA9685.AdafruitMotorHat;
import com.brettonw.PCA9685.MotorId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class MotorControllerTester extends AdafruitMotorHat {
    private static final Logger log = LogManager.getLogger (MotorControllerTester.class);

    private Map<MotorId, Double> speeds;

    public MotorControllerTester (I2CDeviceTester testDevice) {
        super (testDevice);
        speeds = new HashMap<MotorId, Double> (MotorId.values ().length);
        for (MotorId motorId : MotorId.values ()) {
            speeds.put (motorId, 0.0);
        }
    }

    @Override
    public void runMotor (MotorId motorId, double speed) {
        log.debug ("motorId: " + motorId.name () + ", speed: " + String.format ("%.03f", speed));
        speeds.put (motorId, speed);
        super.runMotor (motorId, speed);
    }

    public double getSpeed (MotorId motorId) {
        // only be valid for motors that are actually implements
        return speeds.get (motorId);
    }
}
