package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

public record AircraftRegistration(String string) {
    private static Pattern pattern  = Pattern.compile("[A-Z0-9 .?/_+-]+");
    public AircraftRegistration {
        Preconditions.checkArgument(pattern.matcher(string).matches());
        if (string.isEmpty()) {
            throw new NullPointerException();
        }
    }
}