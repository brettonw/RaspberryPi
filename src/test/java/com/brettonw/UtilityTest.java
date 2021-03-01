package com.brettonw;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilityTest {
    @Test
    public void testClamp () {
        assertTrue (Utility.clamp (1.1, 0, 1) == 1);
        assertTrue (Utility.clamp (-1.1, 0, 1) == 0);
        assertTrue (Utility.clamp (0.5, 0, 1) == 0.5);
    }

    @Test
    public void testSaturate () {
        assertTrue (Utility.saturate (1.1) == 1);
        assertTrue (Utility.saturate (-1.1) == -1);
        assertTrue (Utility.saturate (0.6) == 1);
        assertTrue (Utility.saturate (-0.6) == -1);
        assertTrue (Utility.saturate (0.4) == 0);
        assertTrue (Utility.saturate (-0.4) == 0);
        assertTrue (Utility.saturate (0) == 0);
    }
}
