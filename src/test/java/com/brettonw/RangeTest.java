package com.brettonw;

import com.pi4j.io.gpio.*;
import org.junit.Test;

public class RangeTest {
    @Test
    public void testGPIO () throws InterruptedException {
        System.out.println ("<--Pi4J--> GPIO Control Example ... started.");

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance ();

        // provision gpio pin #01 as an output pin and turn on
        final GpioPinDigitalOutput pin23 = gpio.provisionDigitalOutputPin (RaspiPin.GPIO_04, "TRIG", PinState.LOW);
        final GpioPinDigitalInput pin24 = gpio.provisionDigitalInputPin (RaspiPin.GPIO_05, "ECHO");

        /*
        // set shutdown state for this pin
        pin.setShutdownOptions (true, PinState.LOW);

        System.out.println ("--> GPIO state should be: ON");

        Thread.sleep (1000);

        // turn off gpio pin #01
        pin.low ();
        System.out.println ("--> GPIO state should be: OFF");

        Thread.sleep (1000);

        // toggle the current state of gpio pin #01 (should turn on)
        pin.toggle ();
        System.out.println ("--> GPIO state should be: ON");

        Thread.sleep (1000);

        // toggle the current state of gpio pin #01  (should turn off)
        pin.toggle ();
        System.out.println ("--> GPIO state should be: OFF");

        Thread.sleep (1000);

        // turn on gpio pin #01 for 1 second and then off
        System.out.println ("--> GPIO state should be: ON ");
        pin.pulse (1000, true); // set second argument to 'true' use a blocking call

        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        gpio.shutdown ();
        */

        System.out.println ("Exiting ControlGpioExample");
    }
}
