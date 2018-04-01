package com.brettonw.PCA9685;

import com.brettonw.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * DC Motor demo
 * <p>
 * I used motors like https://www.adafruit.com/product/2941
 * or https://www.adafruit.com/product/711
 */
public class AdafruitServoDriverTest {
    protected static final Logger log = LogManager.getLogger (AdafruitServoDriverTest.class);

    private AdafruitServoDriver servoDriver;

    public AdafruitServoDriverTest () {
        servoDriver = new AdafruitServoDriver ();
    }

    @Test
    public void testServo () {
        if (true) {
            //servoDriver.setPosition (0, 0);
            int min = 50, max = 170;
            Utility.waitD (3.0);
            for (int j = 0; j < 5; ++j) {
                for (int i = min; i <= max; i += 5) {
                    log.debug (i + "");
                    servoDriver.setPosition (0, i / 1000.0);
                    Utility.waitL (10);
                }
                Utility.waitD (1.0);
                for (int i = max; i >= min; i -= 5) {
                    log.debug (i + "");
                    servoDriver.setPosition (0, i / 1000.0);
                    Utility.waitL (10);
                }
                Utility.waitD (1.0);
            }
        }
    }
}
