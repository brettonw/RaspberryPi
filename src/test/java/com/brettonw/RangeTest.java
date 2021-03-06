package com.brettonw;

import com.pi4j.io.gpio.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

public class RangeTest {
    protected static final Logger log = LogManager.getLogger (RangeTest.class);

    long timeout = 1_000_000_000;
    private long waitForPinLow (GpioPinDigitalInput pin) {
        long elapsed = 0;
        long startTime = System.nanoTime ();
        while (pin.isHigh () && (elapsed < timeout)) {
            elapsed = System.nanoTime () - startTime;
        }
        return pin.isLow () ? elapsed : -elapsed;
    }

    private long waitForPinHigh (GpioPinDigitalInput pin) {
        long elapsed = 0;
        long startTime = System.nanoTime ();
        while (pin.isLow () && (elapsed < timeout)) {
            elapsed = System.nanoTime () - startTime;
        }
        return pin.isHigh () ? elapsed : -elapsed;
    }

    @Test
    public void testHcSr04 () throws InterruptedException {
        log.info ("started.");

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance ();

        // provision pins
        final GpioPinDigitalOutput pin23 = gpio.provisionDigitalOutputPin (RaspiPin.GPIO_04, "TRIG", PinState.LOW);
        final GpioPinDigitalInput pin24 = gpio.provisionDigitalInputPin (RaspiPin.GPIO_05, "ECHO");

        // set shutdown state for this pin
        pin23.setShutdownOptions (true, PinState.LOW);

        log.info ("waiting for TRIG to settle.");
        Utility.waitD (1.0);
        pin23.low ();
        Utility.waitD (2.0);

        for (int i = 0; i < ((10 * 1000) / 60); ++i) {

            //log.info ("sending 10ms pulse.");
            pin23.high ();
            Utility.waitBusy (10);
            pin23.low ();

            // now wait for the response
            long timeToPinHigh = waitForPinHigh (pin24);
            long elapsed = 0;
            if (timeToPinHigh > 0) {
                if ((elapsed = waitForPinLow (pin24)) > 0) {
                    // compute the round trip time - half of which was spent travelling the distance to the
                    // target, and half was spent travelling back
                    double time = elapsed / 1e9;
                    double speedOfSound = 343.0; // m/s
                    double distance = speedOfSound * (time / 2.0);
                    log.info ("Distance: " + String.format ("%02fcm", distance * 100));
                }
            }

            // http://www.micropik.com/PDF/HCSR04.pdf says to use over 60ms cycle to prevent reading echoes
            Utility.waitL (60);
        }

        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        gpio.shutdown ();

        System.out.println ("Exiting ControlGpioExample");
    }
}
