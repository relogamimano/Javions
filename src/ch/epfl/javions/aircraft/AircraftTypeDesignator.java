package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;

public record AircraftTypeDesignator(String string) {
    public AircraftTypeDesignator {
        if (!Pattern.compile("[A-Z0-9]{2,4}").matcher(string).matches()) {
            throw new IllegalArgumentException();
        }
    }
}
