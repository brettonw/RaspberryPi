package com.brettonw.PCA9685;

import com.brettonw.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class AdafruitServoDriverTest {
    protected static final Logger log = LogManager.getLogger (AdafruitServoDriverTest.class);

    private ServoController servoController;

    public AdafruitServoDriverTest () {
        servoController = new AdafruitServoDriver ();
    }

    @Test
    public void testServo () {
        if (true) {
            Servo servo = new Servo (servoController, 0, 1, 2);
            servo.setPosition (0.5);
            Utility.waitD (3.0);
            for (int j = 0; j < 5; ++j) {
                for (int i = 0; i <= 100; i += 5) {
                    log.debug (i + "");
                    servo.setPosition (i / 100.0);
                    Utility.waitL (10);
                }
                Utility.waitD (1.0);
                for (int i = 100; i >= 0; i -= 5) {
                    log.debug (i + "");
                    servo.setPosition (i / 100.0);
                    Utility.waitL (10);
                }
                Utility.waitD (1.0);
            }
        }
    }
}
