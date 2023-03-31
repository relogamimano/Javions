package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;

public class AircraftStateAccumulator<T extends AircraftStateSetter>  {
    private static final long NANO_TO_SEC = 10_000_000_000L;
    private T stateSetter;
    public AircraftStateAccumulator(T stateSetter) throws NullPointerException {
        Preconditions.checkArgument(stateSetter != null);
        this.stateSetter = stateSetter;
    }
    public T stateSetter() {
        return stateSetter;
    }
    private AirbornePositionMessage lastOddPositionMessage;
    private AirbornePositionMessage lastEvenPositionMessage;
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
                if (lastOddPositionMessage != null
                        && lastEvenPositionMessage != null
                        && Math.abs(lastEvenPositionMessage.timeStampNs() - lastOddPositionMessage.timeStampNs() ) <= NANO_TO_SEC) {
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
