package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;
import org.junit.jupiter.api.Test;

import static ch.epfl.javions.Bits.extractUInt;
import static ch.epfl.javions.adsb.RawMessage.size;
import static ch.epfl.javions.adsb.RawMessage.typeCode;
import static org.junit.Assert.assertEquals;

public class RawMessageTest {
    RawMessage raw1 =
            new RawMessage(8096200, ByteString.ofHexadecimalString("8D4B17E5F8210002004BB8B1F1AC"));

    @Test
    void sizeWorksOnCorrectDf(){
        assertEquals(raw1.LENGTH, size((byte) 0x8d ));
    }

    @Test
    void icaoadressWorks(){
        assertEquals(new IcaoAddress("4B17E5"),raw1.icaoAddress());
    }

    @Test
    void downLinkFormatWorks(){
        assertEquals(17, raw1.downLinkFormat());
    }

    @Test
    void payloadWorks(){ // 69842078141533112
        long expected = 69842078141533112L;
        assertEquals(expected,raw1.payload());
    }


    @Test
    void staticTypeCodeWorks(){ //11111
        long expected = 31;
        assertEquals(expected, typeCode(69842078141533112L) );

    }

    @Test
    void typeCodeWorks(){
        long expected = 31;
        assertEquals(expected, raw1.typeCode());
    }

    //RawMessage[timeStampNs=8096200, bytes=8D4B17E5F8210002004BB8B1F1AC]
    //RawMessage[timeStampNs=75898000, bytes=8D49529958B302E6E15FA352306B]
    //RawMessage[timeStampNs=100775400, bytes=8D39D300990CE72C70089058AD77]
    //RawMessage[timeStampNs=116538700, bytes=8D4241A9601B32DA4367C4C3965E]
    //RawMessage[timeStampNs=129268900, bytes=8D4B1A00EA0DC89E8F7C0857D5F5]
}
