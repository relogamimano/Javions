package ch.epfl.javions.aircraft;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URLDecoder;

public class AirCraftDatabaseTest {


    @Test
    void worksWithValidIcaoAddress() throws IOException {
        String d = getClass().getResource("/aircraft.zip").getFile();
        d = URLDecoder.decode(d, UTF_8);
        AircraftDatabase tony = new AircraftDatabase(d);
        AircraftData a = tony.get(new IcaoAddress("01001C"));
        AircraftData actual = new AircraftData(new AircraftRegistration("SU-GBL"),
                new AircraftTypeDesignator("B735"), "BOEING 737-500",
                new AircraftDescription("L2J"), WakeTurbulenceCategory.MEDIUM);
        assertEquals(a, actual);
        System.out.println(a);
        System.out.println(actual);
    }

    @Test
    void nullForUnknownIcaoAdress() throws IOException{
        String d = getClass().getResource("/aircraft.zip").getFile();
        d = URLDecoder.decode(d, UTF_8);
        AircraftDatabase invalid = new AircraftDatabase(d);

        AircraftData b = invalid.get(new IcaoAddress("0AC100"));
        assertEquals(null, b );
    }
    


}
