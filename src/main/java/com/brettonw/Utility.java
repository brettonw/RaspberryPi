package com.brettonw;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Utility {
    protected static final Logger log = LogManager.getLogger (Utility.class);

    // internal wait functions
    public final static void waitL (long milliseconds) {
        if (milliseconds >= 0) {
            try {
                Thread.sleep (milliseconds);
            } catch (InterruptedException exception) {
                log.error (exception);
            }
        }
    }

    public final static void waitD (double seconds) {
        waitL (Math.round (seconds * 1_000L));
    }
}
