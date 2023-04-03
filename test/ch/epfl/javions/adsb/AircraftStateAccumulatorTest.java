package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.aircraft.IcaoAddress;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AircraftStateAccumulatorTest {
    @Test
    void aircraftStateAccumulatorConstructorThrowsIfStateSetterIsNull() {
        assertThrows(NullPointerException.class, () -> new AircraftStateAccumulator<>(null));
    }

    @Test
    void aircraftStateSetterStateSetterReturnsStateSetter() {
        for (int i = 0; i < 10; i += 1) {
            var stateSetter = new AircraftState();
            var accumulator = new AircraftStateAccumulator<>(stateSetter);
            assertSame(stateSetter, accumulator.stateSetter());
        }
    }

    @Test
    void aircraftStateAccumulatorUpdateUpdatesCategoryAndCallSign() {
        var icao = new IcaoAddress("ABCDEF");
        var stateSetter = new AircraftState();
        var accumulator = new AircraftStateAccumulator<>(stateSetter);
        var expectedLastMessageTimeStampNs = -1L;
        var expectedCategory = -1;
        var expectedCallSign = (CallSign) null;
        for (var i = 0; i < 8; i += 1) {
            assertEquals(expectedLastMessageTimeStampNs, stateSetter.lastMessageTimeStampNs);
            assertEquals(expectedCategory, stateSetter.category);
            assertEquals(expectedCallSign, stateSetter.callSign);

            expectedLastMessageTimeStampNs = 101L * i;
            expectedCategory = 0xA0 | i;
            expectedCallSign = new CallSign("BLA" + Integer.toString(i, 3));
            var message = new AircraftIdentificationMessage(expectedLastMessageTimeStampNs, icao, expectedCategory, expectedCallSign);
            accumulator.update(message);
        }
    }

    @Test
    void aircraftStateAccumulatorUpdateUpdatesVelocityAndTrackOrHeading() {
        var icao = new IcaoAddress("ABCDEF");
        var stateSetter = new AircraftState();
        var accumulator = new AircraftStateAccumulator<>(stateSetter);
        var expectedLastMessageTimeStampNs = -1L;
        var expectedVelocity = Double.NaN;
        var expectedTrackOrHeading = Double.NaN;
        for (var i = 0; i < 8; i += 1) {
            assertEquals(expectedLastMessageTimeStampNs, stateSetter.lastMessageTimeStampNs);
            assertEquals(expectedVelocity, stateSetter.velocity);
            assertEquals(expectedTrackOrHeading, stateSetter.trackOrHeading);

            expectedLastMessageTimeStampNs = 103L * i;
            expectedVelocity = 10.0 * i;
            expectedTrackOrHeading = 1.99999999 * Math.PI / (i + 1);
            var message = new AirborneVelocityMessage(expectedLastMessageTimeStampNs, icao, expectedVelocity, expectedTrackOrHeading);
            accumulator.update(message);
        }
    }

    @Test
    void aircraftStateAccumulatorUpdateUpdatesAltitudeButNotPositionWhenParityIsConstant() {
        var icao = new IcaoAddress("ABCDEF");
        for (int parity = 0; parity <= 1; parity += 1) {
            var stateSetter = new AircraftState();
            var accumulator = new AircraftStateAccumulator<>(stateSetter);

            var expectedLastMessageTimeStampNs = -1L;
            var expectedAltitude = Double.NaN;
            for (int i = 0; i < 100; i += 1) {
                assertEquals(expectedLastMessageTimeStampNs, stateSetter.lastMessageTimeStampNs);
                assertEquals(expectedAltitude, stateSetter.altitude);
                assertNull(stateSetter.position);

                expectedLastMessageTimeStampNs = 107L * i;
                expectedAltitude = -100d + 20d * i;
                var x = 0.999999 / (i + 1d);
                var y = 1d - x;
                var message = new AirbornePositionMessage(expectedLastMessageTimeStampNs, icao, expectedAltitude, parity, x, y);
                accumulator.update(message);
            }
        }
    }

    @Test
    void aircraftStateAccumulatorUpdateUpdatesAltitudeButNotPositionWhenMessagesTooFarApart() {
        var icao = new IcaoAddress("ABCDEF");
        var moreThan10s = 10_000_000_001L;
        var stateSetter = new AircraftState();
        var accumulator = new AircraftStateAccumulator<>(stateSetter);

        var x = 0.5;
        var y = 0.5;
        var parity = 0;
        var expectedLastMessageTimeStampNs = -1L;
        var expectedAltitude = Double.NaN;
        for (int i = 0; i < 100; i += 1) {
            assertEquals(expectedLastMessageTimeStampNs, stateSetter.lastMessageTimeStampNs);
            assertEquals(expectedAltitude, stateSetter.altitude);
            assertNull(stateSetter.position);

            expectedLastMessageTimeStampNs += moreThan10s;
            expectedAltitude = -100d + 23d * i;
            parity = 1 - parity;
            var message = new AirbornePositionMessage(expectedLastMessageTimeStampNs, icao, expectedAltitude, parity, x, y);
            accumulator.update(message);
        }
    }

    double cpr(int v) {
        return Math.scalb((double) v, -17);
    }

    @Test
    void aircraftStateAccumulatorUpdateUsesCorrectMessageToComputePosition() {
        var icao = new IcaoAddress("ABCDEF");
        var moreThan10s = 10_000_000_001L;
        var stateSetter = new AircraftState();
        var accumulator = new AircraftStateAccumulator<>(stateSetter);

        var timeStampNs = 109L;
        var altitude = 1000d;
        var x0 = cpr(98152);
        var y0 = cpr(98838);
        var x1 = cpr(95758);
        var y1 = cpr(81899);

        var m1 = new AirbornePositionMessage(timeStampNs, icao, altitude, 0, cpr(12), cpr(13));
        accumulator.update(m1);
        assertNull(stateSetter.position);

        timeStampNs += moreThan10s;
        var m2 = new AirbornePositionMessage(timeStampNs, icao, altitude, 0, x0, y0);
        accumulator.update(m2);
        assertNull(stateSetter.position);

        timeStampNs += 1000L;
        var m3 = new AirbornePositionMessage(timeStampNs, icao, altitude, 1, x1, y1);
        accumulator.update(m3);
        assertNotNull(stateSetter.position);
        var p = stateSetter.position;
        assertEquals(6.57520, Math.toDegrees(p.longitude()), 5e-5);
        assertEquals(46.52444, Math.toDegrees(p.latitude()), 5e-5);
    }

    @Test
    void aircraftStateAccumulatorCorrectlyHandlesLatitudeBandChange() {
        record ParityXY(int p, int x, int y) { }

        var xys = new ParityXY[]{
                new ParityXY(0, 98152, 106326),
                new ParityXY(1, 95758, 89262),
                new ParityXY(0, 95758, 106330),
                new ParityXY(1, 93364, 89266)
        };
        var expectedLongitudeDeg = 6.57520;
        var expectedLatitudesDeg = new double[]{
                Double.NaN, 46.8672, 46.8672, 46.8674
        };

        var icao = new IcaoAddress("ABCDEF");
        var stateSetter = new AircraftState();
        var accumulator = new AircraftStateAccumulator<>(stateSetter);

        var timeStampNs = 113L;
        var altitude = 567d;

        for (int i = 0; i < xys.length; i += 1) {
            var m = new AirbornePositionMessage(timeStampNs, icao, altitude, xys[i].p, cpr(xys[i].x), cpr(xys[i].y));
            accumulator.update(m);
            var expectedLatitudeDeg = expectedLatitudesDeg[i];
            if (Double.isNaN(expectedLatitudeDeg)) {
                assertNull(stateSetter.position);
            } else {
                assertEquals(expectedLongitudeDeg, Math.toDegrees(stateSetter.position.longitude()), 1e-4);
                assertEquals(expectedLatitudeDeg, Math.toDegrees(stateSetter.position.latitude()), 1e-4);
            }
            timeStampNs += 1000L;
        }
    }

    private static final class AircraftState implements AircraftStateSetter {
        long lastMessageTimeStampNs = -1L;
        int category = -1;
        CallSign callSign = null;
        GeoPos position = null;
        double altitude = Double.NaN;
        double velocity = Double.NaN;
        double trackOrHeading = Double.NaN;

        @Override
        public void setLastMessageTimeStampNs(long timeStampNs) {
            lastMessageTimeStampNs = timeStampNs;
        }

        @Override
        public void setCategory(int category) {
            this.category = category;
        }

        @Override
        public void setCallSign(CallSign callSign) {
            this.callSign = callSign;
        }

        @Override
        public void setPosition(GeoPos position) {
            this.position = position;
        }

        @Override
        public void setAltitude(double altitude) {
            this.altitude = altitude;
        }

        @Override
        public void setVelocity(double velocity) {
            this.velocity = velocity;
        }

        @Override
        public void setTrackOrHeading(double trackOrHeading) {
            this.trackOrHeading = trackOrHeading;
        }
    }
}