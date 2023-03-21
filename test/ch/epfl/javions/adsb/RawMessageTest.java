package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;

import static ch.epfl.javions.Bits.extractUInt;
import static ch.epfl.javions.adsb.RawMessage.size;
import static ch.epfl.javions.adsb.RawMessage.typeCode;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RawMessageTest {
    //<editor-fold desc="Test messages">
    private static final List<String> VALID_MESSAGES_DF17 = List.of(
            "8D392AE499107FB5C00439035DB8",
            "8D392AE89B00009570AC00DDDBE5",
            "8D44095358BF06C19B95072CF116",
            "8D3E1A079908F2326804455F8C87",
            "8D4B1A5FF82300060049B8C4C7BD",
            "8D3C66B69989A323B83042873FC2",
            "8D4CA8159908B237D8043F8F5B24",
            "8D4D22ABEA4A5864013C080E446F",
            "8D34660B587FC616532EAE3ADF20",
            "8D34520499092FACD8043D0F9517",
            "8D49514658A582D9C97D2AD683AF",
            "8D4B161299005E0B682A069F59BF",
            "8D4B194B5883C2F491954A04B015",
            "8D440824584B8665D772391F973E",
            "8D4A370399115691707427925380",
            "8D02A19558B505063ECC6C520E68",
            "8D3E386C593B26195748051D6C1B",
            "8D34608358B982D5B3532EE0620C",
            "8D4A370399110092B070174F9E26",
            "8D40650B58B1D2CA3D9B7051174A",
            "8D4D218B58B982D17D78AE6605CD",
            "8D3E410959DD02BB036E59C9EE6D",
            "8D300164EA0DC877733C08DEAF57",
            "8D44028CEA253864013C0837B06C",
            "8D46316E99080409D8440D980DF6",
            "8D3C658258C901E8DD5AEB8A7D9D",
            "8D502D58F8210006004AB82E0AFD",
            "8D02009558613634BB4AFE919334",
            "8D502D679909C59730043B427EDD",
            "8D406D279909A7A2D0044A0352BB",
            "8D46A5E7990D3B23D804416C113B",
            "8D4B1803990C0A1F607014EA7359",
            "8D0A008A990C1039B8043E622D37",
            "8D4B1A2B990D6899B0603983BC87",
            "8D46316E581F52F9FF85ED2ECAB9",
            "8D3CDDE658BF01F71B4F5B0AEE01",
            "8D495146F82300020049B835F646",
            "8D4B1A299908CD2B58403657A39E",
            "8D484C1B58C38651A368FDC9C621",
            "8D3001645829A62F894C39E192E8",
            "8D3999E4585F722A4937E82700DB",
            "8D3CDD21E11F3C0000000001E352",
            "8D3C648E58B9865FB382A5E4F976",
            "8D43EA7022349394660820320B1C",
            "8D4B1A3E99085CAC90AC255BB186",
            "8DE48F63583BA5FA2547BD8F3973",
            "8D3E386C595382619B418B6FB79C",
            "8D451DBA9900E235200400C4D4DC",
            "8D3909089910690080140C97401A",
            "8D4B194B99096525F0083E325632",
            "8D40650B99152C24D0483A94E46F");

    private static final List<String> VALID_MESSAGES_DF_OTHER = List.of(
            "8F4B1A3E990C13ACB0942207AEB2",
            "8F40644458BF02864575F1855853",
            "8F39332299117DA368043A76A283",
            "8E3858BCE10A8000000000C5BA29",
            "8E39212E583C25FB1F3682CDD213",
            "8F50020EEA1D68747F7C08930110",
            "8F3946E599154123E0083B900E92",
            "8F440C31F82300060049B8FA4906",
            "8F3950D09914CE2C2804362840DE",
            "8F4B1A1DE109BE00000000E55259",
            "8F40644458BF128F9F713BCA7AC3",
            "8E49C33C99044B8CE0000D79582A",
            "8F44033359598617ED75A7593527",
            "8F39D56999106E0DD0940F3CCCDF",
            "8F4B1A34EA0FB864013C08B1E2A7",
            "8F393322200464B4CC71A001A30D",
            "8E49C33C99044B8C00000D5CD8FC",
            "8E3D287F99102D0E48100EA43FCB",
            "8F4B1A2B587DA2A2E33F36B30239",
            "8F44003EEA485864013C08A98FB1",
            "8F4B1A1E584332D7BF7925390F47",
            "8F50020E9908680C60F40D57665F",
            "8F4B1A34580F564AC9590CE84CD7",
            "8E39212E99106581680811A39195",
            "8F44003EEA485864013C08A98FB1",
            "8E384EDDE10FBB00000000E4BB37",
            "8F44033399150086D85017056400",
            "8F39B9A35915F653A55D7703502E",
            "8F40644499092FAA98043F984F9C",
            "8F3944ED99114DA168402F368381",
            "8F394C1799117DA6200C37BA59FB",
            "8F506CA3998D382028043A254461",
            "8F44003E990D3226B0543CAAF896",
            "8F392AE8902B62E7E36D96984086",
            "8F4409AAEA447860015C00C3232D",
            "8E383F3C8113C663CF6C35034C2F",
            "8F4B1A3E998909A8708431B609F0",
            "8E39212E581E828CBB3734EDA0BB",
            "8F39B9A3593982E8BF64B8547E0D",
            "8F4B1A2BEA3CA864013C08F4DE28",
            "8E39212E99106B01000C12D68447",
            "8F4B1A2B990CA59FB0401BAEB922",
            "8E39212EF8000006004AB0047AB0",
            "8F8964A8582396303B4C9B2D1289",
            "8E383F3C99004502400000EF2C00",
            "8F4B1A2B2315A4F6C90620530928",
            "8F440333EA0BD870015C0033EC5F",
            "8F02009523481379CF11E09869BC",
            "8F4B1A2B584786520B59338782B7",
            "8E3858BCE10A8000000000C5BA29",
            "8F3CDDE6EA11A874875C08CA2E79");

    private static final List<String> VALID_MESSAGES = Stream
            .concat(VALID_MESSAGES_DF17.stream(), VALID_MESSAGES_DF_OTHER.stream())
            .toList();
    //</editor-fold>

    @BeforeAll
    static void preventOutput() {
        if (System.getProperty("ch.epfl.cs108.quiet") != null) {
            System.setOut(new PrintStream(OutputStream.nullOutputStream()));
            System.setErr(new PrintStream(OutputStream.nullOutputStream()));
        }
    }

    @Test
    void rawMessageConstructorThrowsIfTimeStampIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new RawMessage(-1, new ByteString(new byte[14])));
    }

    @Test
    void rawMessageConstructorThrowsIfMessageSizeIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new RawMessage(0, new ByteString(new byte[0])));
        assertThrows(IllegalArgumentException.class, () -> new RawMessage(0, new ByteString(new byte[13])));
        assertThrows(IllegalArgumentException.class, () -> new RawMessage(0, new ByteString(new byte[15])));
    }

    @Test
    void rawMessageSizeOnlyReturns14ForDF17() {
        for (var ca = 0; ca < 1 << 3; ca += 1) {
            for (var df = 0; df < 1 << 5; df += 1) {
                var byte0 = (df << 3) | ca;
                var expectedSize = df == 17 ? 14 : 0;
                Assertions.assertEquals(expectedSize, RawMessage.size((byte) byte0));
            }
        }
    }

    @Test
    void rawMessageStaticTypeCodeReturnsTypeCode() {
        for (var tc = 0; tc < 1 << 5; tc += 1) {
            var payload = ~0L;
            payload = (payload << 5) | (tc + 1);
            payload = (payload << 51) - 1;
            Assertions.assertEquals(tc, RawMessage.typeCode(payload));
        }
    }

    @Test
    void rawMessageOfReturnsRawMessageForValidMessages() {
        for (var message : VALID_MESSAGES) {
            var messageBytes = HexFormat.of().parseHex(message);
            var rawMessage = RawMessage.of(100, messageBytes);
            assertNotNull(rawMessage);
        }
    }

    @Test
    void rawMessageOfReturnsNullForMessagesWithInvalidCrc() {
        var bitToFlip = 0;
        for (var message : VALID_MESSAGES) {
            var messageBytes = HexFormat.of().parseHex(message);
            var byteToFlip = bitToFlip / Byte.SIZE;
            messageBytes[byteToFlip] ^= 1 << (bitToFlip % Byte.SIZE);
            var rawMessage = RawMessage.of(100, messageBytes);
            assertNull(rawMessage);
            bitToFlip += 1;
        }
    }

    @Test
    void rawMessageDownlinkFormatReturnsDownlinkFormat() {
        for (var message : VALID_MESSAGES) {
            var messageBytes = HexFormat.of().parseHex(message);
            var expectedDf = Byte.toUnsignedInt(messageBytes[0]) >> 3;
            var rawMessage = RawMessage.of(100, messageBytes);
            assertNotNull(rawMessage);
            Assertions.assertEquals(expectedDf, rawMessage.downLinkFormat());
        }
    }

    @Test
    void rawMessageIcaoAddressReturnsIcaoAddress() {
        for (var message : VALID_MESSAGES) {
            var messageBytes = HexFormat.of().parseHex(message);
            var icaoAddress = (Byte.toUnsignedInt(messageBytes[1]) << 16)
                    | (Byte.toUnsignedInt(messageBytes[2]) << 8)
                    | Byte.toUnsignedInt(messageBytes[3]);
            var icaoAddressString = "%06X".formatted(icaoAddress);
            var expectedIcaoAddress = new IcaoAddress(icaoAddressString);
            var rawMessage = RawMessage.of(100, messageBytes);
            assertNotNull(rawMessage);
            Assertions.assertEquals(expectedIcaoAddress, rawMessage.icaoAddress());
        }
    }

    @Test
    void rawMessagePayloadReturnsPayload() {
        for (var message : VALID_MESSAGES) {
            var messageBytes = HexFormat.of().parseHex(message);
            var expectedPayload = 0L;
            for (var i = 4; i < 11; i += 1)
                expectedPayload = (expectedPayload << Byte.SIZE) | Byte.toUnsignedLong(messageBytes[i]);
            var rawMessage = RawMessage.of(100, messageBytes);
            assertNotNull(rawMessage);
            Assertions.assertEquals(expectedPayload, rawMessage.payload());
        }
    }

    @Test
    void rawMessageTypeCodeReturnsTypeCode() {
        for (var message : VALID_MESSAGES) {
            var messageBytes = HexFormat.of().parseHex(message);
            var expectedTypeCode = Byte.toUnsignedInt(messageBytes[4]) >> 3;
            var rawMessage = RawMessage.of(100, messageBytes);
            assertNotNull(rawMessage);
            Assertions.assertEquals(expectedTypeCode, rawMessage.typeCode());
        }
    }
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

}
