package ch.epfl.javions;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebMercatorTest {

    private static final double DELTA = 1e-7;

    @Test
    void webMercatorXWorksOnKnownValues() {
        var actual1 = WebMercator.x(1, Math.toRadians(-180));
        var expected1 = 0.0;
        assertEquals(expected1, actual1, DELTA);

        var actual2 = WebMercator.x(2, Math.toRadians(-90));
        var expected2 = 256.0;
        assertEquals(expected2, actual2, DELTA);

        var actual3 = WebMercator.x(3, Math.toRadians(-45));
        var expected3 = 768.0;
        assertEquals(expected3, actual3, DELTA);

        var actual4 = WebMercator.x(4, Math.toRadians(0));
        var expected4 = 2048.0;
        assertEquals(expected4, actual4, DELTA);

        var actual5 = WebMercator.x(5, Math.toRadians(45));
        var expected5 = 5120.0;
        assertEquals(expected5, actual5, DELTA);

        var actual6 = WebMercator.x(6, Math.toRadians(90));
        var expected6 = 12288.0;
        assertEquals(expected6, actual6, DELTA);

        var actual7 = WebMercator.x(7, Math.toRadians(180));
        var expected7 = 32768.0;
        assertEquals(expected7, actual7, DELTA);

        var actual8 = WebMercator.x(8, Math.toRadians(12.3456));
        var expected8 = 35015.44789333333;
        assertEquals(expected8, actual8, DELTA);
    }

    @Test
    void webMercatorYWorksOnKnownValues() {
        var actual1 = WebMercator.y(1, Math.toRadians(-85));
        var expected1 = 511.16138762953835;
        assertEquals(expected1, actual1, DELTA);

        var actual2 = WebMercator.y(2, Math.toRadians(-45));
        var expected2 = 655.6415621988301;
        assertEquals(expected2, actual2, DELTA);

        var actual3 = WebMercator.y(3, Math.toRadians(0));
        var expected3 = 1024.0;
        assertEquals(expected3, actual3, DELTA);

        var actual4 = WebMercator.y(4, Math.toRadians(45));
        var expected4 = 1473.4337512046795;
        assertEquals(expected4, actual4, DELTA);

        var actual5 = WebMercator.y(5, Math.toRadians(85));
        var expected5 = 13.417797927355878;
        assertEquals(expected5, actual5, DELTA);

        var actual6 = WebMercator.y(6, Math.toRadians(12.3456));
        var expected6 = 7625.739193000258;
        assertEquals(expected6, actual6, DELTA);
    }
}