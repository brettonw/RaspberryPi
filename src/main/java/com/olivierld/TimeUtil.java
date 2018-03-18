package com.olivierld;

/**
 * Adapted from https://github.com/OlivierLD/raspberry-pi4j-samples
 */
public class TimeUtil {
    /**
     * @param howMuch in ms.
     */
    public final static void delay (long howMuch) {
        try {
            Thread.sleep (howMuch);
        } catch (InterruptedException ie) {
            ie.printStackTrace ();
        }
    }

    public final static void delay (float howMuch) {
        delay (Math.round (howMuch * 1_000L));
    }
}
