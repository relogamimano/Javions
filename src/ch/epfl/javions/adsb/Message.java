package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public interface Message {
    /**
     * Return the timestamp of the message in nanoseconds
     * @return the timestamp of the message in nanoseconds
     */
    long timeStampsNs();

    /**
     * Return the ICAO address of the message's sender
     * @return the ICAO address of the message's sender
     */
    IcaoAddress icaoAddress();
}
