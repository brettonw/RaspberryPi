package com.brettonw.PCA9685;

import com.brettonw.Utility;

class CycleValue {
    public double motor1;
    public double motor2;

    public CycleValue (double motor1, double motor2) {
        this (motor1, motor2, false);
    }

    public CycleValue (double motor1, double motor2, boolean saturate) {
        this.motor1 = saturate ? (Utility.saturate (motor1)) : motor1;
        this.motor2 = saturate ? (Utility.saturate (motor2)) : motor2;
    }
}

