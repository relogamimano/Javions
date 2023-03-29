package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;

public final class AdsbDemodulator {
    private final PowerWindow powerWindow;
    private static final int STANDARD_WINDOW_SIZE = 1200;
    private static final int ADS_B_BYTE_SIZE = 14;
    private static final int SIGNAL_MULTIPLICATOR = 10;
    private static final double[] PREAMBLE_PEAK_PULSES = new double[]{0.0, 1.0, 3.5, 4.5};// in nanoseconds
    private static final double[] PREAMBLE_VALLEY_PULSES = new double[]{0.5, 1.5, 2.0, 2.5, 3.0, 4.0};// in nanoseconds
    private static final double PREAMBLE_LENGTH = 8.0;// in nanoseconds
    private static final double PULSE_LENGTH = 0.5;// in nanoseconds
    private static final int SIGNAL_START = (int) (PREAMBLE_LENGTH * SIGNAL_MULTIPLICATOR);
    private static final int SIGNAL_END = (int) ((PREAMBLE_LENGTH + PULSE_LENGTH) * SIGNAL_MULTIPLICATOR);

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
            actualVSum = valleySum();
            nextPSum = peaksSum();

            if (previousPSum < actualPSum && actualPSum > nextPSum && (actualPSum >= (2 * actualVSum))) {

                bytes = new byte[ADS_B_BYTE_SIZE];
                for (int j = 0; j < ADS_B_BYTE_SIZE; j++) {
                    int tempByte = 0;
                    for (int k = 0; k < Byte.SIZE; k++) {
                        tempByte = (tempByte << 1);
                        int lowerSample = powerWindow.get(SIGNAL_START * (j + 1) + 10 * k);
                        int upperSample = powerWindow.get(SIGNAL_END + SIGNAL_START * j + SIGNAL_MULTIPLICATOR * k);
                        tempByte |= lowerSample < upperSample ? 0 : 1;
                    }
                    bytes[j] = (byte) tempByte;
                }
                if (RawMessage.size(bytes[0]) != 0) {

                    RawMessage m = RawMessage.of(powerWindow.position() * 100, bytes);
                    if (m != null) {
                        powerWindow.advanceBy(STANDARD_WINDOW_SIZE);
                        return m;
                    }

                }
            }
        }
        return null;
    }

    private int peaksSum() {
        int pSum = 0;
        for (double peakPulse : PREAMBLE_PEAK_PULSES) {
            pSum += powerWindow.get((int) (peakPulse * SIGNAL_MULTIPLICATOR) + 1);
        }
        return pSum;
    }

    private int valleySum() {
        int vSum = 0;
        for (double valleyPulse : PREAMBLE_VALLEY_PULSES) {
            vSum += powerWindow.get((int) (valleyPulse * SIGNAL_MULTIPLICATOR));
        }
        return vSum;
    }
}

