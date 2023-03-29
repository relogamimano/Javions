package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.Objects;
import java.util.regex.Pattern;
/**
 * * @author: Sofia Henriques Garfo (346298)
 *  * @author: Romeo Maignal (360568)
 */
public record AircraftRegistration(String string) {
    private static Pattern pattern  = Pattern.compile("[A-Z0-9 .?/_+-]+");
    public AircraftRegistration {
        Preconditions.checkArgument(pattern.matcher(string).matches());
        Objects.requireNonNull(string);
    }
}