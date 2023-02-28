package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

public record AircraftDescription(String string) {
    private static Pattern pattern = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");;
    public AircraftDescription {
        Preconditions.checkArgument(pattern.matcher(string).matches() || string.isEmpty());
    }
}
