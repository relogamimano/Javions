package ch.epfl.javions.aircraft;


import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class IcaoAddressTest {

    @Test
    void failsOnIncorrectAddress() {
        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoAddress("4G1814");
        });
    }

    @Test
    void failsOnEmptyAddress(){
        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoAddress("");
        });
    }

    @Test
    void worksOnTrivialAdress(){
        assertDoesNotThrow(() -> {
            new IcaoAddress("4B1814");
        });
    }

}
