package com.brettonw.PCA9685.Tester;

import com.brettonw.PCA9685.AdafruitServoDriver;
import com.brettonw.PCA9685.ServoId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ServoControllerTester extends AdafruitServoDriver {
    private static final Logger log = LogManager.getLogger (ServoControllerTester.class);

    private Map<ServoId, Double> durations;

    public ServoControllerTester (I2CDeviceTester testDevice) {
        super (testDevice);
        durations = new HashMap<> (ServoId.values ().length);
        for (ServoId servoId : ServoId.values ()) {
            durations.put (servoId, 0.0);
        }
    }

    @Override
    public void setPulseDuration (ServoId servoId, double milliseconds) {
        log.debug ("servoId: " + servoId + ", milliseconds: " + String.format ("%.03f", milliseconds));
        durations.put (servoId, milliseconds);
        super.setPulseDuration (servoId, milliseconds);
    }

    public double getPulseDuration (ServoId servoId) {
        return durations.get (servoId);
    }
}
