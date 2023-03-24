package ch.epfl.javions.adsb;

import org.junit.jupiter.api.Test;

public class CprDecoderTest {

    @Test
    public void cpr(){
        System.out.println(CprDecoder.decodePosition(0.62,0.42,0.6200000000000000001,0.420000000000000001,0));
    }
}
