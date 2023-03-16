package ch.epfl.javions.demodulation;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;

public final class AdsbDemodulator {
    private final InputStream sampleStream;
    private PowerWindow powerWindow;
    private
    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        this.sampleStream = samplesStream;
        powerWindow = new PowerWindow(samplesStream, 1200);
    }

    public RawMessage nextMessage() throws IOException {

        byte[] bytes = new byte[120];
        int previousPSum = pSum(powerWindow, 1, -1);
        int actualPSum = pSum(powerWindow, 1, 0);
        int actualVSum =  vSum(powerWindow, 1);
        int nextPSum = pSum(powerWindow, 1, 1);
        for (int i = 1; i < powerWindow.size() - 1; i++) {
            if (powerWindow.get(i) == -1) {
                return null;
            }
//            previousPSum = pSum(powerWindow, i, -1);
//            actualPSum = pSum(powerWindow, i, 0);
//            actualVSum = vSum(powerWindow, i);
//            nextPSum = pSum(powerWindow, i, 1);

            if( previousPSum < actualPSum
                    && actualPSum > nextPSum
                    && actualPSum >= 2 * actualVSum ) {
                for (int j = 0; j < bytes.length; j++) {
                    bytes[j] = (byte) (powerWindow.get(80 + 10 * i) < powerWindow.get(85 + 10 * i) ? 0 : 1);
                }
            }
            previousPSum = actualPSum;
            actualPSum = nextPSum;
            nextPSum = pSum(powerWindow, i, 1);
            powerWindow.advance();
        }
        ByteString byteString = new ByteString(bytes);
        long DF = byteString.bytesInRange(0, 0) & 0x11111;
        if (DF != (long) 17) {
            // TODO: 16.03.23 Reccurence relation usefull ? Legitimate ?
            return nextMessage();
        }
        long ICAO = byteString.bytesInRange(1, 3);
        long ME = byteString.bytesInRange(4, 10);
        long CRC = byteString.bytesInRange(11, 13);
        Crc24 crc24 = new Crc24(Crc24.GENERATOR);
        if (crc24.crc(bytes) == CRC) {
            return new RawMessage(Timestamp, byteString);
        }
        return null;
    }

    private int pSum(PowerWindow pw, int i, int shift) throws IllegalArgumentException {
        if(shift >= -1 && shift <= 1) {throw new IllegalArgumentException("SHIFT OUTSIDE OF POSSIBLES VALUES = {-1, 0, 1");}
        return pw.get(i + shift) + pw.get(i + shift + 10) + pw.get(i + shift + 35) + pw.get(i + shift + 45);
    }
    private int vSum(PowerWindow pw, int i)  {
        return pw.get(i + 5) + pw.get(i + 15) + pw.get(i + 20) + pw.get(i + 25) + pw.get(i + 30) + pw.get(i + 40);
    }
}
