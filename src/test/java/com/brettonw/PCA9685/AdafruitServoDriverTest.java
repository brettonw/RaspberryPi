package com.brettonw.PCA9685;

import com.brettonw.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class AdafruitServoDriverTest {
    private static final Logger log = LogManager.getLogger (AdafruitServoDriverTest.class);

    private ServoController servoController;

    public AdafruitServoDriverTest () {
        servoController = new AdafruitServoDriver ();
    }

    @Test
    public void testServo () {
        if (false) {
            Servo servo1 = new Servo (servoController, ServoId.SERVO_01, 1, 2);
            Servo servo9 = new Servo (servoController, ServoId.SERVO_09, 1, 2);
            Utility.waitD (3.0);
            for (int j = 0; j < 5; ++j) {
                servo1.setPosition (1);
                servo9.setPosition (1);
                Utility.waitD (1.0);
                servo1.setPosition (0);
                servo9.setPosition (0);
                Utility.waitD (1.0);
                servo1.setPosition (-1);
                servo9.setPosition (-1);
                Utility.waitD (1.0);
                servo1.setPosition (0);
                servo9.setPosition (0);
                Utility.waitD (3.0);
            }
        }
    }
}
