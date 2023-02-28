package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AircraftDescriptionTest {

    @Test
    void worksOnEmptyString(){
        assertDoesNotThrow(() -> {
            new AircraftDescription("");
        });
    }

    @Test
    void worksOnTrivialDescription(){
        assertDoesNotThrow(() ->{
            new AircraftDescription("L2J");
        });
    }

    @Test
    void failsOnInvalidDescription(){
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftDescription("0A2");
        });
    }
}
