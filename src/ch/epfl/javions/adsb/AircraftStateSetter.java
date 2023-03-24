package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

/**
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public interface AircraftStateSetter {
    /**
     * Change the timestamp of the last message received by from the airplane to the given value in argument
     * @param timeStampsNs the timestamp of the last message
     */
    void setLastMessageTimeStampsNs(long timeStampsNs);

    /**
     * Change the category of the airplane to the given value passed as an argument that follows
     * @param category the category of the airplane
     */
    void setCategory(int category);

    /**
     * Change the call sign of the airplane to the given value
     * @param callsSign the call sign of the airplane
     */
    void setCallsSign(CallSign callsSign);

    /**
     * Modify the geographic position of the airplane to the given value
     * @param position the geographic position of the airplane
     */
    void setPosition(GeoPos position);

    /**
     * Modify the altitude data of the airplane to the given value passed as an argument
     * @param altitude the altitude data of the airplane
     */
    void setAltitude(double altitude);

    /**
     * Modify the airplane's velocity information to the given value passed as an argument
     * @param velocity the airplane's velocity
     */
    void setVelocity(double velocity);

    /**
     * Change the direction of the airplane to the given value
     * @param trackOrHeading the direction of the airplane
     */
    void setTrackOrHeading(double trackOrHeading);

}
