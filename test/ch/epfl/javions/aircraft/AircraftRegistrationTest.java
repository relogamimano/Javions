package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class AircraftRegistrationTest {

    @Test
    void failsOnEmptyString(){
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftRegistration("");
        });
    }

    @Test
    void failsOnInvalidRegistration(){
        assertThrows(IllegalArgumentException.class, () ->{
            new AircraftRegistration("AB()");
        });
    }

    @Test
    void worksOnTrivialRegistration(){
        assertDoesNotThrow(() -> {
            new AircraftRegistration("HB-JDC");
        });
    }
}
