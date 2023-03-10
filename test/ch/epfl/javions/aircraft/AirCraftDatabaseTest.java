package ch.epfl.javions.aircraft;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;
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

        // test données

    private AircraftDatabase getDatabase() {
        // Try to get the database from the resources
        var aircraftResourceUrl = getClass().getResource("/aircraft.zip");
        if (aircraftResourceUrl != null)
            return new AircraftDatabase(URLDecoder.decode(aircraftResourceUrl.getFile(), UTF_8));

        // Try to get the database from the JAVIONS_AIRCRAFT_DATABASE environment variable
        // (only meant to simplify testing of several projects with a single database)
        var aircraftFileName = System.getenv("JAVIONS_AIRCRAFT_DATABASE");
        if (aircraftFileName != null)
            return new AircraftDatabase(aircraftFileName);

        throw new Error("Could not find aircraft database");
    }


    @Test
    void aircraftDatabaseGetReturnsNullWhenAddressDoesNotExist() throws IOException {
        var aircraftDatabase = getDatabase();
        assertNull(aircraftDatabase.get(new IcaoAddress("123456")));
    }

    @Test
    void aircraftDatabaseGetWorksWithFirstLineOfFile() throws IOException {
        var aircraftDatabase = getDatabase();
        var aircraftData = aircraftDatabase.get(new IcaoAddress("0086AB"));
        assertNotNull(aircraftData);
        assertEquals(new AircraftRegistration("ZS-CNA"), aircraftData.registration());
    }

    @Test
    void aircraftDatabaseGetWorksWithLastLineOfFile() throws IOException {
        var aircraftDatabase = getDatabase();
        var aircraftData = aircraftDatabase.get(new IcaoAddress("E808C0"));
        assertNotNull(aircraftData);
        assertEquals(new AircraftRegistration("CC-DAW"), aircraftData.registration());
    }

    @Test
    void aircraftDatabaseGetWorksWithAddressGreaterThanLastOneOfFile() throws IOException {
        var aircraftDatabase = getDatabase();
        var aircraftData = aircraftDatabase.get(new IcaoAddress("FFFF01"));
        assertNull(aircraftData);
    }

    @Test
    void aircraftDatabaseGetReturnsCorrectData() throws IOException {
        var aircraftDatabase = getDatabase();
        var aircraftData = aircraftDatabase.get(new IcaoAddress("4B1805"));
        assertNotNull(aircraftData);
        assertEquals(new AircraftRegistration("HB-JCN"), aircraftData.registration());
        assertEquals(new AircraftTypeDesignator("BCS3"), aircraftData.typeDesignator());
        assertEquals("AIRBUS A220-300", aircraftData.model());
        assertEquals(new AircraftDescription("L2J"), aircraftData.description());
        assertEquals(WakeTurbulenceCategory.MEDIUM, aircraftData.wakeTurbulenceCategory());
    }

    @Test
    void aircraftDatabaseGetWorksWithEmptyColumns() throws IOException {
        var aircraftDatabase = getDatabase();
        var aircraftData = aircraftDatabase.get(new IcaoAddress("AAAAAA"));
        assertNotNull(aircraftData);
        assertEquals(new AircraftRegistration("N787BK"), aircraftData.registration());
        assertEquals(new AircraftTypeDesignator(""), aircraftData.typeDesignator());
        assertEquals("", aircraftData.model());
        assertEquals(new AircraftDescription(""), aircraftData.description());
        assertEquals(WakeTurbulenceCategory.UNKNOWN, aircraftData.wakeTurbulenceCategory());
    }

}
