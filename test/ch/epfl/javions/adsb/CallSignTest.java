package ch.epfl.javions.adsb;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class CallSignTest {
    @Test
    void callSignConstructorThrowsWithInvalidCallSign() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CallSign("callsign");
        });
    }

    @Test
    void callSignConstructorAcceptsEmptyCallSign() {
        assertDoesNotThrow(() -> {
            new CallSign("");
        });
    }

    @Test
    void callSignConstructorAcceptsValidCallSign() {
        assertDoesNotThrow(() -> {
            new CallSign("AFR39BR");
        });
    }

}
