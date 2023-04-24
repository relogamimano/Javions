package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.Objects;
import java.util.regex.Pattern;
/**
 * Aicraft ICAO address
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public record IcaoAddress(String string) {
    private static final Pattern pattern = Pattern.compile("[0-9A-F]{6}");

/**
 * Verifies that the ICAO address is valid : [0-9A-F]{6}
 * @param string aircraft type designator
 * @throws IllegalArgumentException if the address isn't valid
 * @throws NullPointerException if the string is null
 **/
    public IcaoAddress {
        Preconditions.checkArgument(pattern.matcher(string).matches());
        Objects.requireNonNull(string);
    }

}

