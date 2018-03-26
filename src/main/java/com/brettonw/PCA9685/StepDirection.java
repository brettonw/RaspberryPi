package com.brettonw.PCA9685;

public enum StepDirection {
    FORWARD (1),
    BACKWARD (-1);

    private int stepAdd;

    StepDirection (int add) {
        stepAdd = add;
    }

    public int getStepAdd () {
        return stepAdd;
    }

}
