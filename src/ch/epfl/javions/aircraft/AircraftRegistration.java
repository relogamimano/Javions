package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;

public record AircraftRegistration(String string) {
    public AircraftRegistration {
        if (!Pattern.compile("[A-Z0-9 .?/_+-]+").matcher(string).matches()) {
            throw new IllegalArgumentException();
        }
        if (string == null) {
            throw new NullPointerException();
        }
    }
}
