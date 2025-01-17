package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;

/**
 * Collects ADS-B messages of one aircraft in order to have its state over time
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public class AircraftStateAccumulator<T extends AircraftStateSetter>  {
    private static final long TIMESTAMP_DIFF = 10;
    private final T stateSetter;

    /**
     * Returns an aircraft state accumulator associated with the given mutable state, or throws NullPointerException if null.
     *
     * @param stateSetter state setter
     * @throws NullPointerException if the state is null
     */
    public AircraftStateAccumulator(T stateSetter) throws NullPointerException {
        if(stateSetter == null) throw new NullPointerException();
        this.stateSetter = stateSetter;
    }

    /**
     * Getter that return the state setter, of an Airplane, given as parameter in the constructor
     * @return airplane's state setter
     */
    public T stateSetter() {
        return stateSetter;
    }
    private AirbornePositionMessage lastOddPositionMessage;
    private AirbornePositionMessage lastEvenPositionMessage;

    /**
     * Methode that keep up to date the state setter depending on what kind of ADSB-Message as been given in parameter
     * @param message ADSB-Message
     */
    public void update(Message message) {
        stateSetter.setLastMessageTimeStampNs(message.timeStampNs());
        switch (message) {
            case AircraftIdentificationMessage aim -> {
                stateSetter.setCallSign(aim.callSign());
                stateSetter.setCategory(aim.category());
            }
            case AirbornePositionMessage aim -> {
                stateSetter.setAltitude(aim.altitude());
                if ((aim.parity() == 0)) {
                    lastEvenPositionMessage = aim;
                } else {
                    lastOddPositionMessage = aim;
                }
                if (lastOddPositionMessage != null && lastEvenPositionMessage != null
                        && Math.abs(lastEvenPositionMessage.timeStampNs() - lastOddPositionMessage.timeStampNs() )
                        <= Units.convertTo(TIMESTAMP_DIFF, Units.NANO)) {
                    GeoPos m = CprDecoder.decodePosition(lastEvenPositionMessage.x(),
                            lastEvenPositionMessage.y(), lastOddPositionMessage.x(),
                            lastOddPositionMessage.y(), aim.parity());
                    if (m != null) {
                        stateSetter.setPosition(m);
                    }
                }
            }
            case AirborneVelocityMessage aim -> {
                stateSetter.setVelocity(aim.speed());
                stateSetter.setTrackOrHeading(aim.trackOrHeading());
            }
            default -> System.out.println("Autre type de message.");

        }
    }
}
