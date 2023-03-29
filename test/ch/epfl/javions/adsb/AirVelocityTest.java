package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AirVelocityTest {

        @Test
        public static void main(String[] args) throws IOException {
            String f = "resources/samples_20230304_1442.bin";
            IcaoAddress expectedAddress = new IcaoAddress("4D2228");
            try (InputStream s = new FileInputStream(f)) {
                AdsbDemodulator d = new AdsbDemodulator(s);
                RawMessage m;
                while((m=d.nextMessage())!=null){
                    var velocitymessage=AirborneVelocityMessage.of(m) ;
                    if(velocitymessage!=null){
                        System.out.println(velocitymessage);
                    }
                }


            }}}
