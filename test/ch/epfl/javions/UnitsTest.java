package ch.epfl.javions;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class UnitsTest {
    private static final double DELTA = 1e-7;


    @Test
    void unitConvertWorksOnSomeUnits() {
        var actual1 = Units.convert(2.34, Units.Angle.TURN, Units.Angle.DEGREE);
        var expected1 = 842.4;
        assertEquals(expected1, actual1, DELTA);

        var actual2 = Units.convert(2.34, Units.Angle.DEGREE, Units.Angle.T32);
        var expected2 = 2.7917287424E7;
        assertEquals(expected2, actual2, DELTA);

        var actual3 = Units.convert(2.34, Units.Length.KILOMETER, Units.Length.INCH);
        var expected3 = 92125.98425196849;
        assertEquals(expected3, actual3, DELTA);

        var actual4 = Units.convert(2.34, Units.Length.INCH, Units.Length.FOOT);
        var expected4 = 0.195;
        assertEquals(expected4, actual4, DELTA);

        var actual5 = Units.convert(2.34, Units.Length.FOOT, Units.Length.NAUTICAL_MILE);
        var expected5 = 3.851144708423326E-4;
        assertEquals(expected5, actual5, DELTA);

        var actual6 = Units.convert(2.34, Units.Time.MINUTE, Units.Time.HOUR);
        var expected6 = 0.039;
        assertEquals(expected6, actual6, DELTA);

        var actual7 = Units.convert(2.34, Units.Speed.KNOT, Units.Speed.KILOMETER_PER_HOUR);
        var expected7 = 4.33368;
        assertEquals(expected7, actual7, DELTA);
    }

    @Test
    void unitConvertFromWorksOnSomeUnits() {
        var actual1 = Units.convertFrom(2.34, Units.Angle.TURN);
        var expected1 = 14.70265361880023;
        assertEquals(expected1, actual1, DELTA);

        var actual2 = Units.convertFrom(2.34, Units.Angle.DEGREE);
        var expected2 = 0.04084070449666731;
        assertEquals(expected2, actual2, DELTA);

        var actual3 = Units.convertFrom(2.34, Units.Length.CENTIMETER);
        var expected3 = 0.0234;
        assertEquals(expected3, actual3, DELTA);

        var actual4 = Units.convertFrom(2.34, Units.Length.INCH);
        var expected4 = 0.059436;
        assertEquals(expected4, actual4, DELTA);

        var actual5 = Units.convertFrom(2.34, Units.Length.FOOT);
        var expected5 = 0.713232;
        assertEquals(expected5, actual5, DELTA);

        var actual6 = Units.convertFrom(2.34, Units.Time.MINUTE);
        var expected6 = 140.39999999999998;
        assertEquals(expected6, actual6, DELTA);

        var actual7 = Units.convertFrom(2.34, Units.Speed.KNOT);
        var expected7 = 1.2038;
        assertEquals(expected7, actual7, DELTA);
    }

    @Test
    void unitConvertToWorksOnSomeUnits() {
        var actual1 = Units.convertTo(2.34, Units.Angle.TURN);
        var expected1 = 0.3724225668350351;
        assertEquals(expected1, actual1, DELTA);

        var actual2 = Units.convertTo(2.34, Units.Angle.DEGREE);
        var expected2 = 134.07212406061262;
        assertEquals(expected2, actual2, DELTA);

        var actual3 = Units.convertTo(2.34, Units.Length.KILOMETER);
        var expected3 = 0.00234;
        assertEquals(expected3, actual3, DELTA);

        var actual4 = Units.convertTo(2.34, Units.Length.INCH);
        var expected4 = 92.12598425196849;
        assertEquals(expected4, actual4, DELTA);

        var actual5 = Units.convertTo(2.34, Units.Length.FOOT);
        var expected5 = 7.6771653543307075;
        assertEquals(expected5, actual5, DELTA);

        var actual6 = Units.convertTo(2.34, Units.Time.MINUTE);
        var expected6 = 0.039;
        assertEquals(expected6, actual6, DELTA);

        var actual7 = Units.convertTo(2.34, Units.Speed.KNOT);
        var expected7 = 4.548596112311015;
        assertEquals(expected7, actual7, DELTA);
    }
}