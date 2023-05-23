package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.*;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.gui.AircraftStateManager;
import ch.epfl.javions.gui.ObservableAircraftState;
import javafx.collections.ObservableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.common.input.EOFException;

import java.io.*;
import java.sql.SQLOutput;
import java.text.DecimalFormat;
import java.util.*;

public class AircraftStateManagerTest {
    @Test
    void vzyMarcheSTP() {
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(getClass().getResource("/messages_20230318_0915.bin").getFile())))) {
            byte[] bytes = new byte[RawMessage.LENGTH];

            AircraftStateManager aircraftStateManager = new AircraftStateManager(
                    new AircraftDatabase(getClass().getResource("/aircraft.zip").getFile()));
            int index = 0;
            System.out.printf("| %-6s | %-9s | %6s | %30s | %45s | %8s | %7s |%n",
                    "OACI", "Indicatif", "Immat.", "Modèle", "Position", "Alt.", "Vit.");
            Set<ObservableAircraftState> stateSet = aircraftStateManager.states();
            while (true){
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString messageBytes = new ByteString(bytes);
                RawMessage rawMessage = new RawMessage(timeStampNs, messageBytes);
                Message message = MessageParser.parse(rawMessage);
                aircraftStateManager.updateWithMessage(message);

                for (int i = 0; i < 50; i++) System.out.println();

                System.out.printf("| %-6s | %-9s | %6s | %33s | %20s | %20s | %5.0s | %5.0s |%n",
                        "OACI", "Indicatif", "Immat.", "Modèle", "Position lat", "Position long", "Alt.", "Vit.");
                for (ObservableAircraftState state : stateSet) {

                    System.out.printf("| %-6s | %-9s | %6s | %33s | %20f | %20f | %5.0f | %5.0f |%n",
                            (state.getIcaoAddress() != null ? state.getIcaoAddress().string() : ""),
                            (state.getCallSign() != null ? state.getCallSign().string() : ""),
                            (state.getAircraftData() != null ? state.getAircraftData().registration().string() : ""),
                            (state.getAircraftData() != null ? state.getAircraftData().model() : ""),
                            Units.convertTo(state.getPosition().latitude(), Units.Angle.DEGREE),
                            Units.convertTo(state.getPosition().longitude(), Units.Angle.DEGREE),
                            state.getAltitude(),
                            Units.convertTo(state.getVelocity(), Units.Speed.KILOMETER_PER_HOUR));
                }
                Thread.sleep(100);
//                System.out.println("CLEAR_CONSOLE");
//                System.out.println("LET_CONSOLE");


            }

        } catch (EOFException | IOException e) {
            System.out.println(e);/* nothing to do */
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private static class AddressComparator implements Comparator<ObservableAircraftState> {
        @Override
        public int compare(ObservableAircraftState o1, ObservableAircraftState o2) {
            String s1 = o1.getIcaoAddress()
                    .string();
            String s2 = o2.getIcaoAddress()
                    .string();
            return s1.compareTo( s2 );
        }
    }

    public static void main(String[] args) {
        byte[] bytes = new byte[RawMessage.LENGTH];
        AircraftDatabase aircraftDatabase = new AircraftDatabase(
                AircraftStateManagerTest.class.getResource("/aircraft.zip").getFile());
        AircraftStateManager stateManager = new AircraftStateManager( aircraftDatabase );
        int index = 0;
        System.out.println(
                "OACI      Indicatif      Immat.     Modèle                               Longitude     " + "Latitude "
                        + "     Alt.  " + "     Vit." + "    Dir.\n"
                        + "―――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――" );

        try ( DataInputStream s = new DataInputStream( new BufferedInputStream( new FileInputStream(
                "resources/messages_20230318_0915.bin" ) ) ) ) {

            while ( true ) {
                index++;
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes( bytes, 0, bytes.length );
                assert bytesRead == RawMessage.LENGTH;
                assert bytes != null;

                ByteString message = new ByteString( bytes );
                assert message != null;

                RawMessage rawMessage = RawMessage.of( timeStampNs, bytes );
                assert rawMessage != null;

                Message theMessage = MessageParser.parse( rawMessage );
                assert theMessage != null;

                stateManager.updateWithMessage( theMessage );
                stateManager.purge();
            }
        }

        catch ( IOException e ) {
        }
        finally {
            List<ObservableAircraftState> states = new ArrayList<>( stateManager.states() );
            AddressComparator comparator = new AddressComparator();
            states.sort( comparator );
            try {
                index = 0;
                DecimalFormat df = new DecimalFormat( "#.#####" );
                for ( ObservableAircraftState state : states ) {
                    index++;
                    String aircraftData = state.getIcaoAddress()
                            .string();
                    String callSign = state.getCallSign()
                            .string();
                    String aircraftRegistration = state.getAircraftData() == null ? "" : state.getAircraftData()
                            .registration()
                            .string();
                    String aircraftModel = state.getAircraftData() == null ? "" : state.getAircraftData()
                            .model();
                    double longitude = Units.convertTo( state.getPosition()
                            .longitude(), Units.Angle.DEGREE );
                    double latitude = Units.convertTo( state.getPosition()
                            .latitude(), Units.Angle.DEGREE );
                    double altitude = state.getAltitude();
                    if ( state.getVelocity() == -1 ) {
                        continue;
                    }
                    double velocity = Units.convertTo( state.getVelocity(), Units.Speed.KILOMETER_PER_HOUR );
                    double dir = state.getTrackOrHeading() % ( 2 * Math.PI );
                    char direction;
                    final char[] directions = new char[]{'↑', '↗', '→', '↘', '↓', '↙', '←', '↖'};
                    if ( 5.890 < dir || dir <= 0.393 ) {
                        direction = directions[0];
                    }
                    else if ( 0.393 < dir && dir <= 1.178 ) {
                        direction = directions[1];
                    }
                    else if ( 1.178 < dir && dir <= 1.963 ) {
                        direction = directions[2];
                    }
                    else if ( 1.963 < dir && dir <= 2.749 ) {
                        direction = directions[3];
                    }
                    else if ( 2.749 < dir && dir <= 3.534 ) {
                        direction = directions[4];
                    }
                    else if ( 3.534 < dir && dir <= 4.320 ) {
                        direction = directions[5];
                    }
                    else if ( 4.320 < dir && dir <= 5.105 ) {
                        direction = directions[6];
                    }
                    else if ( 5.105 < dir && dir <= 5.980 ) {
                        direction = directions[7];
                    }
                    else {
                        direction = 'X';
                    }

                    System.out.printf( "%-9s %-14s %-10s %-36s %-13s %-13s %-10s %-8s %s\n", aircraftData, callSign,
                            aircraftRegistration, aircraftModel, df.format( longitude ),
                            df.format( latitude ), Math.round( altitude ), Math.round( velocity ),
                            direction );
                }
            }
            catch ( NullPointerException e ) {
                System.out.println( "e.getMessage() = " + e.getMessage() );
                System.out.println( "index = " + index );
            }
        }
    }





}
