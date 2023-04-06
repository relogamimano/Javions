package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;
/**
 * Aircraft type designator
 * * @author: Sofia Henriques Garfo (346298)
 *  * @author: Romeo Maignal (360568)
 */
public record AircraftTypeDesignator(String string) {
    private static final Pattern pattern = Pattern.compile("[A-Z0-9]{2,4}");
    /**
     * Verifies that the aircraft designator is valid : [A-Z0-9]{2,4}
     * @param string aircraft type designator
     * @throws IllegalArgumentException if aircraft type designator not valid
     */
    public AircraftTypeDesignator {
        Preconditions.checkArgument(pattern.matcher(string).matches() || string.isEmpty()) ;
    }

}
