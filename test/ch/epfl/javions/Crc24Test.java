package ch.epfl.javions;

import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.Assert.assertEquals;

public class Crc24Test {
    // for loop used to test different ADS-B messages
    @Test
    void crc24WorkingWithTrivialValues() {
        Crc24 crc24 = new Crc24(Crc24.GENERATOR);
        String[] mS = new String[]{"8D392AE499107FB5C00439",
                "8D4D2286EA428867291C08", "8D3950C69914B232880436", "8D4B17E399893E15C09C21",
                "8D4B18F4231445F2DB63A0", "8D495293F82300020049B8"};
        String[] cS = new String[]{"035DB8", "EE2EC6", "BC63D3", "9FC014", "DEEB82", "111203"};
        for (int i = 0; i < mS.length; i++) {
            int c = Integer.parseInt(cS[i], 16); // == 0x035DB8

            byte[] mAndC = HexFormat.of().parseHex(mS[i] + cS[i]);
            assertEquals(0, crc24.crc(mAndC));

            byte[] mOnly = HexFormat.of().parseHex(mS[i]);
            assertEquals(c, crc24.crc(mOnly));
        }

    }

}
