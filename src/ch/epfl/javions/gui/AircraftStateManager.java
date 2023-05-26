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

    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        this.table = new HashMap<>();
        this.observableStates = FXCollections.observableSet();
        this.unmodifiableStates = FXCollections.unmodifiableObservableSet(observableStates);
        this.aircraftDatabase = Objects.requireNonNull(aircraftDatabase);
    }
    public ObservableSet<ObservableAircraftState> states() {
        return unmodifiableStates;
    }

    public void updateWithMessage(Message message) throws IOException {
        if (message != null) {
            lastTimeStamp = message.timeStampNs();
            IcaoAddress address = message.icaoAddress();
            if (table.get(address) == null) {
                table.put( address,
                        new AircraftStateAccumulator<>(
                                new ObservableAircraftState( address,
                                        aircraftDatabase.get( address ) ) ) );
            }
            table.get(address).update(message);
            if(table.get(address)
                    .stateSetter()
                    .getPosition() != null) {
                observableStates.add(table.get(address).stateSetter());
            }

        }

    }

    public void purge() {
        Iterator<AircraftStateAccumulator<ObservableAircraftState>> i = table.values().iterator();
        while (i.hasNext()) {
            var a = i.next();
            if ( (lastTimeStamp - a.stateSetter().getTimeStampNs()) > convertTo(MINUTE, NANO)) {
                System.out.println(a.stateSetter().getIcaoAddress());
                observableStates.remove(a.stateSetter());
                i.remove();
            }
        }
    }

}
