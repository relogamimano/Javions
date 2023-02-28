package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

public record IcaoAddress(String string) {
    private static Pattern pattern = Pattern.compile("[0-9A-F]{6}");
// TODO: 26.02.23  Notez que la méthode compile effectue un certain travail qu'il est préférable de ne faire qu'une fois au démarrage du programme. Faites donc en sorte que ce soit le cas, en stockant l'objet Pattern correspondant aux adresses OACI dans un attribut statique de l'enregistrement IcaoAddress.
    public IcaoAddress {
        Preconditions.checkArgument(pattern.matcher(string).matches());
        if (string.isEmpty()) {
            throw new NullPointerException();
        }
    }

}

