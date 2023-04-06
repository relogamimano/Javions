package ch.epfl.javions.aircraft;

import java.util.Objects;

/**
 * Fixed data for a plane
 * @param registration registration number
 * @param typeDesignator type indicator
 * @param model aircraft model
 * @param description aircraft description
 * @param wakeTurbulenceCategory turbulence caregory
 */
/**
 * * @author: Sofia Henriques Garfo (346298)
 *  * @author: Romeo Maignal (360568)
 */
public record AircraftData(AircraftRegistration registration, AircraftTypeDesignator typeDesignator, String model,
                           AircraftDescription description, WakeTurbulenceCategory wakeTurbulenceCategory) {
    /**
     * Verifies that none of the parameters are empty
     */
    public AircraftData{
        Objects.requireNonNull(registration);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(model);
        Objects.requireNonNull(description);
        Objects.requireNonNull(wakeTurbulenceCategory);

    }

}
