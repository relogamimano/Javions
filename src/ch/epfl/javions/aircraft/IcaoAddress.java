package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

public record IcaoAddress(String string, Pattern pattern) {
//    private static Pattern pattern = new Pattern();
    public IcaoAddress {

        Preconditions.checkArgument(!Pattern.compile("[0-9A-F]{6}").matcher(string).matches());
        if (string == null) {
            throw new NullPointerException();
        }
    }

}
