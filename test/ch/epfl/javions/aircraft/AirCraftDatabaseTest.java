package ch.epfl.javions.aircraft;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URLDecoder;

public class AirCraftDatabaseTest {

        @Test
        void dataBaseWorks()  {
            String d = getClass().getResource("/aircraft.zip").getFile();        d = URLDecoder.decode(d, UTF_8);
            AircraftDatabase tony = new AircraftDatabase(d);        try {
                AircraftData a = tony.get(new IcaoAddress("01001C"));            System.out.println(a);
                AircraftData actual = new AircraftData(new AircraftRegistration("SU-GBL"),                    new AircraftTypeDesignator("B735"), "BOEING 737-500",
                        new AircraftDescription("L2J"), WakeTurbulenceCategory.MEDIUM);            System.out.println(actual);
                assertEquals(a, actual);        }
            catch (IOException e){            System.out.println("hello");
            }

        }
        @Test    void dataBaseConstructorThrows() {
            assertThrows(NullPointerException.class, () -> new AircraftDatabase(null));    }

        @Test    void dataBaseGetReturnsNull() {
            String d = getClass().getResource("/aircraft.zip").getFile();        d = URLDecoder.decode(d, UTF_8);
            AircraftDatabase tony = new AircraftDatabase(d);        AircraftData actual = null;
            System.out.println(actual);        try {
                AircraftData a = tony.get(new IcaoAddress("08101C"));            System.out.println(a);
                assertEquals(a, actual);        } catch (IOException e) {
                System.out.println("hello");        }
            //08001C,5H-PWB,AT75,ATR-72-500,L2T,M
        }



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
