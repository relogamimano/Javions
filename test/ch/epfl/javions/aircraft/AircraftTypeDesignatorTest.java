package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AircraftTypeDesignatorTest{

    @Test
    void worksOnEmptyString(){
        assertDoesNotThrow( () -> {
            new AircraftTypeDesignator("");
        });
    }

    @Test
    void worksOnTrivialDesignator(){
        assertDoesNotThrow(() -> {
            new AircraftTypeDesignator("A20N");
        });
    }

    @Test
    void failsOnInvalidDesignator(){
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftTypeDesignator("AAAAAA");
        });
    }
    // is this argument illegal?
}
