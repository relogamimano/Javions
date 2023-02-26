package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

public record AircraftRegistration(String string) {
    private static Pattern pattern;
    public AircraftRegistration {
        pattern = Pattern.compile("[A-Z0-9 .?/_+-]+");
        Preconditions.checkArgument(pattern.matcher(string).matches());
        if (string.isEmpty()) {
            throw new NullPointerException();
        }
    }
}