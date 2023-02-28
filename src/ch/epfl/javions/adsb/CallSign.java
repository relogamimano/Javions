package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

public record CallSign (String string) {
    private static Pattern pattern;

    public CallSign{
        pattern = Pattern.compile("[A-Z0-9 ]{0,8}");
        Preconditions.checkArgument(pattern.matcher(string).matches());

    }
}
