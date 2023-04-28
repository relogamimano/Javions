package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.*;

/**
 * Class managing aircraft's current state
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public class AircraftStateManager {
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> table;
    private final ObservableSet<ObservableAircraftState> observableStates;
    private final AircraftDatabase aircraftDatabase;
    private long timeStamp = 0;

    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        table = new HashMap<>();
        observableStates = FXCollections.observableSet();

        this.aircraftDatabase = Objects.requireNonNull(aircraftDatabase);
    }
    public Set<ObservableAircraftState> states() {
        return Collections.unmodifiableSet(observableStates);
    }

    public void updateWithMessage(Message message) throws IOException {

        if (message != null) {
            this.timeStamp = message.timeStampNs();
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
            if ( ( i.next().stateSetter().getTimeStampNs() - timeStamp ) > (60 * 10e-9) ) {
                i.remove();
                observableStates.remove(i.next().stateSetter());
            }
        }

    }

}
