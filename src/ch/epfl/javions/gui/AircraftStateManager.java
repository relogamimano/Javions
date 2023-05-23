package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
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
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public class AircraftStateManager {
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> table;
    private final ObservableSet<ObservableAircraftState> observableStates;
    private final AircraftDatabase aircraftDatabase;
    ObservableSet<ObservableAircraftState> states;
    private long lastTimeStamp = 0;

    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        table = new HashMap<>();
        observableStates = FXCollections.observableSet();
        states = FXCollections.unmodifiableObservableSet(observableStates);
        this.aircraftDatabase = Objects.requireNonNull(aircraftDatabase);
    }
    public ObservableSet<ObservableAircraftState> states() {
        return states;
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
        Iterator<IcaoAddress> i = table.keySet().iterator();

//        while (i.hasNext()) {
//            var ii = i.next();
//            if ( (timeStamp - table.get(ii).stateSetter().getTimeStampNs() ) > convertTo(MINUTE, NANO)) {
////                var ii = i.next();
//                table.remove(ii);
//                System.out.println("witness");
////                observableStates.remove(table.get(ii).stateSetter());
//            }3586568700
        //     66877933000

        observableStates.removeIf(state -> {
            var t            = lastTimeStamp;
            var tt = state.getTimeStampNs();
            var v = convertTo(MINUTE, NANO);
            if (( lastTimeStamp - state.getTimeStampNs() ) > convertTo(MINUTE, NANO)) {
                var ii = 0;
            }
            return ( lastTimeStamp - state.getTimeStampNs() ) > convertTo(MINUTE, NANO);
        });



//        }

    }

}
