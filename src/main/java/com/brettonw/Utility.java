package com.brettonw;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Utility {
    protected static final Logger log = LogManager.getLogger (Utility.class);

    // internal wait functions
    public static void waitL (int milliseconds) {
        if (milliseconds >= 0) {
            try {
                Thread.sleep (milliseconds);
            } catch (InterruptedException exception) {
                log.error (exception);
            }
        }
    }

    public static void waitD (double seconds) {
        waitL ((int) Math.round (seconds * 1_000));
    }

    public static void waitBusy (int microseconds) {
        final long startTime = System.nanoTime ();
        final long nanoseconds = microseconds * 1_000;
        long elapsed;
        do {
            elapsed = System.nanoTime () - startTime;
        } while (elapsed < nanoseconds);
    }

    public static void waitShort (int microseconds) {
        if (microseconds < 2_000) {
            waitBusy (microseconds);
        } else {
            int milliseconds = Math.round (microseconds / 1_000);
            waitL (milliseconds);
            waitBusy (microseconds - (milliseconds * 1_000));
        }
    }

    public static double saturate (double value) {
        return (Math.abs (value) > 0.5) ? Math.signum (value) : 0;
    }

    public static double clamp (double value, double min, double max) {
        return Math.min (max, Math.max (min, value));
    }
}
