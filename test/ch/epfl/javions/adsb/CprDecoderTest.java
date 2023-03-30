package ch.epfl.javions.adsb;

import ch.epfl.javions.adsb.CprDecoder;
import org.junit.jupiter.api.Test;

import static java.lang.Math.scalb;
import static java.lang.Math.toDegrees;
import static org.junit.jupiter.api.Assertions.*;

class CprDecoderTest {
    private static double cpr(double cpr) {
        return scalb(cpr, -17);
    }

    void checkDecodePosition(int cprX0,
                             int cprY0,
                             int cprX1,
                             int cprY1,
                             int mostRecent,
                             double expectedLonDeg,
                             double expectedLatDeg,
                             double delta) {
        var x0 = cpr(cprX0);
        var x1 = cpr(cprX1);
        var y0 = cpr(cprY0);
        var y1 = cpr(cprY1);
        var p = CprDecoder.decodePosition(x0, y0, x1, y1, mostRecent);
        assertNotNull(p);
        assertEquals(expectedLonDeg, toDegrees(p.longitude()), delta);
        assertEquals(expectedLatDeg, toDegrees(p.latitude()), delta);
    }

    @Test
    void cprDecoderDecodePositionWorksOnKnownExamples() {
        // Example given in stage 5
        var delta = 1e-6;
        checkDecodePosition(111600, 94445, 108865, 77558, 0, 7.476062, 46.323349, delta);

        // Example from https://mode-s.org/decode/content/ads-b/3-airborne-position.html#decoding-example
        checkDecodePosition(0b01100100010101100, 0b10110101101001000, 0b01100010000010010, 0b10010000110101110, 0, 3.919373, 52.257202, delta);

        // Examples from https://github.com/flightaware/dump1090/blob/master/cprtests.c
        checkDecodePosition(9432, 80536, 9192, 61720, 0, 0.700156, 51.686646, delta);
        checkDecodePosition(9432, 80536, 9192, 61720, 1, 0.701294, 51.686763, delta);
        checkDecodePosition(9413, 80534, 9144, 61714, 0, 0.698745, 51.686554, delta);
        checkDecodePosition(9413, 80534, 9144, 61714, 1, 0.697632, 51.686484, delta);
    }

    @Test
    void cprDecoderDecodePositionWorksWithOnlyOneLatitudeBand() {
        checkDecodePosition(2458, 92843, 2458, 60712, 0, 6.75, 88.25, 1e-2);
        checkDecodePosition(2458, 92843, 2458, 60712, 1, 6.75, 88.25, 1e-2);
    }

    @Test
    void cprDecoderDecodePositionWorksWithPositiveAndNegativeCoordinates() {
        for (var i = 0; i <= 1; i += 1) {
            checkDecodePosition(94663, 43691, 101945, 47332, i, -20d, -10d, 1e-4);
            checkDecodePosition(94663, 87381, 101945, 83740, i, -20d, 10d, 1e-4);
            checkDecodePosition(36409, 43691, 29127, 47332, i, 20d, -10d, 1e-4);
            checkDecodePosition(36409, 87381, 29127, 83740, i, 20d, 10d, 1e-4);
        }
    }

    @Test
    void cprDecoderDecodePositionReturnsNullWhenLatitudeIsInvalid() {
        assertNull(CprDecoder.decodePosition(0, 0, 0, cpr(34776), 0));
        assertNull(CprDecoder.decodePosition(0, 0, 0, cpr(34776), 1));
        assertNull(CprDecoder.decodePosition(0, cpr(5), 0, cpr(66706), 0));
        assertNull(CprDecoder.decodePosition(0, cpr(5), 0, cpr(66706), 1));
    }

    @Test
    void cprDecoderDecodePositionReturnsNullWhenSwitchingLatitudeBands() {
        var args = new int[][]{
                // Random values
                {43253, 99779, 122033, 118260},
                {67454, 100681, 123802, 124315},
                {129578, 70001, 82905, 105074},
                {30966, 110907, 122716, 79872},
                // Real values
                {85707, 77459, 81435, 60931},
                {100762, 106328, 98304, 89265},
                {104941, 106331, 104905, 89210},
        };

        for (var as : args) {
            var x0 = cpr(as[0]);
            var y0 = cpr(as[1]);
            var x1 = cpr(as[2]);
            var y1 = cpr(as[3]);
            assertNull(CprDecoder.decodePosition(x0, y0, x1, y1, 0));
            assertNull(CprDecoder.decodePosition(x0, y0, x1, y1, 1));
        }
    }
}