package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import com.sun.javafx.collections.ObservableSetWrapper;
import javafx.beans.property.SetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.*;

/**
 * Class managing aircraft's current state
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public final class AircraftStateManager {
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> table;
    private final ObservableSet<ObservableAircraftState> observableStates;
    private AircraftDatabase aircraftDatabase;

    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        table = new HashMap<>();
        observableStates = FXCollections.observableSet();

        this.aircraftDatabase = Objects.requireNonNull(aircraftDatabase);
    }
    public Set<ObservableAircraftState> states() {
        return Collections.unmodifiableSet(observableStates);
    }

    public void updateWithMessage(Message message) throws IOException {
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

    public void purge() {
//        Iterator i = observableStates.iterator();
//        while (i.hasNext()) {
//
//        }
//        for (:
//             ) {
//
//        }
        observableStates.clear();
    }

    public void printSet() {    if (this.observableStates.isEmpty())
        return;
        System.out.printf("| %-6s | %-8s | %-6s | %-15s | %-6s | %-7s | %-5s | %-8s |%n", "OACI", "Indicatif", "Immat.", "Modèle", "Longitude", "Latitude", "Alt.", "Vit.");    this.purge();
        for (ObservableAircraftState aircraft : this.observableStates)        System.out.printf("  %-6s   %-8s   %-6s   %-15s   %-6f   %-7f   %-5f   %-8f  %n", aircraft.getIcaoAddress().string(), "temp", aircraft.getCategory(), "temp", aircraft.getPosition().longitude() * 180/Math.PI, aircraft.getPosition().latitude() * 180/Math.PI, aircraft.getAltitude(), aircraft.getVelocity());
    }
}