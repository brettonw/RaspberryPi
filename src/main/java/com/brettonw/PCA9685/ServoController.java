package com.brettonw.PCA9685;

public interface ServoController {
    /**
     * set the pulse width to control a servo. the exact meaning of this is up to the servo itself.
     * @param servoId - which servo to set the pulse duration for
     * @param milliseconds - the width of the pulse in milliseconds
     * @return this, for chaining
     */
    void setPulseDuration (int servoId, double milliseconds);
}
