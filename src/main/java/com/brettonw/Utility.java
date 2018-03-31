package com.brettonw;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Utility {
    protected static final Logger log = LogManager.getLogger (Utility.class);

    // internal wait functions
    public static void waitL (long milliseconds) {
        if (milliseconds >= 0) {
            try {
                Thread.sleep (milliseconds);
            } catch (InterruptedException exception) {
                log.error (exception);
            }
        }
    }

    public static void waitD (double seconds) {
        waitL (Math.round (seconds * 1_000L));
    }

    public static double saturate (double value) {
        return (Math.abs (value) > 0.5) ? Math.signum (value) : 0;
    }
}
