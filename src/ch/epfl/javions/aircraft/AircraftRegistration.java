package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.Objects;
import java.util.regex.Pattern;
/**
 * Aircraft registration 
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public record AircraftRegistration(String string) {
    private static final Pattern pattern  = Pattern.compile("[A-Z0-9 .?/_+-]+");

    /**
     * Verifies that the Aircraft registritation is not null and valid [A-Z0-9 .?/_+-]+
     * @param string aircraft registration
     * @throws IllegalArgumentException if string not valid
     * @throws NullPointerException if string is null
     */
    public AircraftRegistration {
        Preconditions.checkArgument(pattern.matcher(string).matches());
        Objects.requireNonNull(string);
    }
}