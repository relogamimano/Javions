package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;


public final class AdsbDemodulator {
    private final PowerWindow powerWindow;
    private static final int STANDARD_WINDOW_SIZE = 1200;
    private static final int ADS_B_BYTE_SIZE = 14;
    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        powerWindow = new PowerWindow(samplesStream, STANDARD_WINDOW_SIZE);
    }


    public RawMessage nextMessage() throws IOException {

        byte[] bytes;

        int previousPSum;
        int actualPSum = 0;
        int nextPSum = 0;
        int actualVSum;

        while (powerWindow.isFull()) {


            powerWindow.advance();

            previousPSum = actualPSum;
            actualPSum = nextPSum;
            actualVSum = vSum();
            nextPSum = pSum();

            if( previousPSum < actualPSum && actualPSum > nextPSum && (actualPSum >= (2 * actualVSum))){

                bytes = new byte[ADS_B_BYTE_SIZE];
                for (int j = 0; j < ADS_B_BYTE_SIZE; j++) {
                    int tempByte = 0;
                    for (int k = 0; k < Byte.SIZE; k++) {
                        tempByte = (tempByte << 1);
                        tempByte |= (powerWindow.get(80 * (j + 1) + 10 * k) < powerWindow.get(85 + 80 * j + 10 * k) ? 0 : 1);
                    }
                    bytes[j] = (byte)tempByte;
                }
                if (RawMessage.size(bytes[0]) != 0) {

                    RawMessage m = RawMessage.of(powerWindow.position() * 100, bytes);
                     if(m != null) {
                         powerWindow.advanceBy(STANDARD_WINDOW_SIZE);
                         return m;
                     }

                }
            }
        }
        return null;
    }

    private int pSum() {
        return powerWindow.get(1) + powerWindow.get(11) + powerWindow.get(36) + powerWindow.get(46);
    }
    private int vSum()  {
        return powerWindow.get(5) + powerWindow.get(15) + powerWindow.get(20) + powerWindow.get(25) + powerWindow.get(30) + powerWindow.get(40);
    }
}
