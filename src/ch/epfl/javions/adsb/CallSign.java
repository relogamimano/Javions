package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Call sign of the aircraft
 * @param string call string@author:
 * @author : Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)            =
 */
public record CallSign (String string) {
    private static final Pattern pattern = Pattern.compile("[A-Z0-9 ]{0,8}");


    /**
     * Verifies that the call sign is valid [A-Z0-9]{0,8}
     * @param string call sign $
     * @throws IllegalArgumentException if the message is invlaid
     */

    public CallSign{
        Preconditions.checkArgument(pattern.matcher(string).matches());

    }
}
