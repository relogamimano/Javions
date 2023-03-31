package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MessageParserTest {

    public static void main(String[] args) throws IOException {
        String f = "resources/samples_20230304_1442.bin";
        IcaoAddress expectedAddress = new IcaoAddress("4D2228");
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int i = 0;
            while ((m = d.nextMessage()) != null) {
                var message = MessageParser.parse(m);
                if (message != null) {
                    System.out.println(message);

                    i++;
                }
            }
            System.out.println(i);


        }
    }
}
