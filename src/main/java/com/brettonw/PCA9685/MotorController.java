package com.brettonw.PCA9685;

/**
 * Motor Controller is an interface to a board that can control multiple motors.
 */
public interface MotorController {
    /**
     * run a motor
     * @param motorId - which motor to run
     * @param speed - the speed to run it at  int he range 0..1, 0 is stopped.
     */
    void runMotor (MotorId motorId, double speed);
}

