package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;
/**
 * * @author: Sofia Henriques Garfo (346298)
 *  * @author: Romeo Maignal (360568)
 */
public record AircraftDescription(String string) {
    private static Pattern pattern = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");

    /**
     * Verifies that the aircraft description is valid : [ABDGHLPRSTV-][0123468][EJPT-]
     * @param string aircraft description
     * @throws IllegalArgumentException if aircraft description not valid
     */
    public AircraftDescription {
        Preconditions.checkArgument(pattern.matcher(string).matches() || string.isEmpty());
    }
}
