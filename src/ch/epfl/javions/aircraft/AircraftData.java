package ch.epfl.javions.aircraft;

import java.util.Objects;

/**
 * Collecte les données fixes d'un aéronef
 * @param registration
 *          numéro d'immatriculation
 * @param typeDesignator
 *          indicateur type
 * @param model
 *          modèle
 * @param description
 *          description
 * @param wakeTurbulenceCategory
 *          catégorie de turbulence de sillage
 */
public record AircraftData(AircraftRegistration registration, AircraftTypeDesignator typeDesignator, String model,
                           AircraftDescription description, WakeTurbulenceCategory wakeTurbulenceCategory) {
    /**
     * Vérifie qu'aucune des donnés est vide
     */
    public AircraftData{
        Objects.requireNonNull(registration);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(model);
        Objects.requireNonNull(description);
        Objects.requireNonNull(wakeTurbulenceCategory);

    }

}
