package ch.epfl.javions;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.*;

class Math2Test {
    private static final double DELTA = 1e-7;


    @Test
    void math2ClampClampsValueBelowMin() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var min = rng.nextInt(-100_000, 100_000);
            var max = min + rng.nextInt(100_000);
            var v = min - rng.nextInt(500);
            assertEquals(min, Math2.clamp(min, v, max));
        }
    }

    @Test
    void math2ClampClampsValueAboveMax() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var min = rng.nextInt(-100_000, 100_000);
            var max = min + rng.nextInt(100_000);
            var v = max + rng.nextInt(500);
            assertEquals(max, Math2.clamp(min, v, max));
        }
    }

    @Test
    void math2ClampPreservesValuesBetweenMinAndMax() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var min = rng.nextInt(-100_000, 100_000);
            var v = min + rng.nextInt(100_000);
            var max = v + rng.nextInt(100_000);
            assertEquals(v, Math2.clamp(min, v, max));
        }
    }

    @Test
    void math2AsinhWorksOnKnownValues() {
        var actual1 = Math2.asinh(Math.PI);
        var expected1 = 1.8622957433108482;
        assertEquals(expected1, actual1, DELTA);

        var actual2 = Math2.asinh(Math.E);
        var expected2 = 1.7253825588523148;
        assertEquals(expected2, actual2, DELTA);

        var actual3 = Math2.asinh(2022);
        var expected3 = 8.304989641287715;
        assertEquals(expected3, actual3, DELTA);

        var actual4 = Math2.asinh(-2022);
        var expected4 = -8.304989641057409;
        assertEquals(expected4, actual4, DELTA);

        var actual5 = Math2.asinh(-1.23456);
        var expected5 = -1.0379112743027366;
        assertEquals(expected5, actual5, DELTA);
    }
}