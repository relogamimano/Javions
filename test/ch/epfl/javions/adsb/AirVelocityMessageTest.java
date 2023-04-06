package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HexFormat;

public class AirVelocityMessageTest {

    @Test
    void messageB() {
        System.out.println(AirborneVelocityMessage.of(RawMessage.of(100, HexFormat.of().parseHex("8D485020994409940838175B284F"))));
    }

    @Test
    public static void main(String[] args) throws IOException {
        String f = "resources/samples_20230304_1442.bin";
        IcaoAddress expectedAddress = new IcaoAddress("4D2228");
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int i = 0;
            while ((m = d.nextMessage()) != null) {
                var velocitymessage = AirborneVelocityMessage.of(m);
                if (velocitymessage != null) {
                    System.out.println(velocitymessage);
                    i++;
                }
            }
            System.out.println(i);


        }
    }

    @Test
    void messageC() {
        RawMessage rm = new RawMessage(0, ByteString.ofHexadecimalString("8DA05F219C06B6AF189400CBC33F"));    System.out.println(AirborneVelocityMessage.of(rm));
    }
}
