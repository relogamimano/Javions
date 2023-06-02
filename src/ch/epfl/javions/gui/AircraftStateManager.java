package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.*;

import static ch.epfl.javions.Units.NANO;
import static ch.epfl.javions.Units.Time.MINUTE;
import static ch.epfl.javions.Units.convertTo;

/**
 * Class managing aircraft's current state
 * @author Sofia Henriques Garfo (346298)
 * @author Romeo Maignal (360568)
 */
public class AircraftStateManager {
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> table;
    private final ObservableSet<ObservableAircraftState> observableStates;
    private final AircraftDatabase aircraftDatabase;
    private long lastTimeStamp = 0;
    private final ObservableSet<ObservableAircraftState> unmodifiableStates;

    /**
     * Aircraft State Manager constructor that builds a manager of aircraft state.
     * It controls their ongoing state and give access to tools to update or remove them.
     * @param aircraftDatabase aircraft data base
     */
    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        this.table = new HashMap<>();
        this.observableStates = FXCollections.observableSet();
        this.unmodifiableStates = FXCollections.unmodifiableObservableSet(observableStates);
        this.aircraftDatabase = Objects.requireNonNull(aircraftDatabase);
    }

    /**
     * Getter method that return an unmodifiable version of the observable aircraft states.
     * @return unmodifiable states set
     */
    public ObservableSet<ObservableAircraftState> states() {
        return unmodifiableStates;
    }

    /**
     * Updating method used bring up to date the state of aircraft, in the set of observables state,
     * corresponding to the message took in argument.
     * @param message message
     * @throws IOException if an input/output file error occurs when calling the update methode
     */
    public void updateWithMessage(Message message) throws IOException {

        if (message != null) {
            lastTimeStamp = message.timeStampNs();
            IcaoAddress address = message.icaoAddress();
            if (table.get(address) == null) {/*check if the aircraft's message doesn't already have a state slot in the table
            IF NOT : the message is used to create a new aircraft state in the table */
                table.put( address,
                        new AircraftStateAccumulator<>(
                                new ObservableAircraftState( address,
                                        aircraftDatabase.get( address ) ) ) );
            }
            table.get(address).update(message);// message updating the table of aircraft states
            //here we check if a state is eligible to become an observable aircraft
            if(table.get(address)
                    .stateSetter()
                    .getPosition() != null) {/*The state need to have a known position */
                observableStates.add(table.get(address).stateSetter());
            }
        }
    }

    /**
     * Updating method used to remove aircraft no longer sending position messages.
     */
    public void purge() {
        Iterator<AircraftStateAccumulator<ObservableAircraftState>> i = table.values().iterator();
        while (i.hasNext()) {
            var a = i.next();
            if ( (lastTimeStamp - a.stateSetter().getTimeStampNs()) > convertTo(MINUTE, NANO)) {
                observableStates.remove(a.stateSetter());//removing of the Accumulator and the State independently
                i.remove();
            }
        }
    }

}
