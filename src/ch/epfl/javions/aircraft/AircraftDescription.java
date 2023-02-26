package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;

public record AircraftDescription(String string) {
    public AircraftDescription {
        if (!Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]").matcher(string).matches()) {
            throw new IllegalArgumentException();
        }
    }
}
