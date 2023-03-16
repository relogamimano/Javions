package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;
/**
 * * @author: Sofia Henriques Garfo (346298)
 *  * @author: Romeo Maignal (360568)
 */
public record IcaoAddress(String string) {
    private static Pattern pattern = Pattern.compile("[0-9A-F]{6}");
    public IcaoAddress {
        Preconditions.checkArgument(pattern.matcher(string).matches());
        if (string.isEmpty()) {
            throw new NullPointerException();
        }
    }

}

