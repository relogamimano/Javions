package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

public record AircraftTypeDesignator(String string) {
    private static Pattern pattern;
    public AircraftTypeDesignator {
        pattern = Pattern.compile("[A-Z0-9]{2,4}");
        Preconditions.checkArgument(pattern.matcher(string).matches());
    }
}
