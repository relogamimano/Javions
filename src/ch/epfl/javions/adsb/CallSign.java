package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @param string
 *          Indicatif de l'aéronef
 */
public record CallSign (String string) {
    private static Pattern pattern = Pattern.compile("[A-Z0-9 ]{0,8}");


    public CallSign{
        Preconditions.checkArgument(pattern.matcher(string).matches());

    }
}
