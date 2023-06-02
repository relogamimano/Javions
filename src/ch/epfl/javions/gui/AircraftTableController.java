package ch.epfl.javions.gui;
import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS;


/**
 * Class that manages the table with the aircraft's information
 * @author Sofia Henriques Garfo (346298)
 * @author Romeo Maignal (360568)
 * */

public final class AircraftTableController {
    //Optimal dimensions for the columns holding data
    private static final int NUM_PREFERRED_WIDTH = 85;
    private static final int OACI_PREFERRED_WIDTH = 70;

    private static final int REG_PREFERRED_WIDTH = 90;

    private static final int MOD_PREFERRED_WIDTH = 230;

    private static final int TYP_PREFERRED_WIDTH = 50;

    private static final int FRACT_DIGITS_POSITION = 4;

    private static final int FRACT_DIGITS_OTHERS = 0;


    private  final TableView<ObservableAircraftState> table;
    private final ObservableSet<ObservableAircraftState>  states;

    private final ObjectProperty<ObservableAircraftState> selectedA;
    private Consumer<ObservableAircraftState> consumer;

    private static final String TABLE_STYLE = "table.css";

    /**
     * Creates a table, defines the columns and install the listeners and the handlers
     * @param aircraftStates states of the aircraft
     * @param selectedAircraft selected aircraft
     */
    public AircraftTableController(ObservableSet<ObservableAircraftState> aircraftStates,
                                   ObjectProperty<ObservableAircraftState> selectedAircraft){

        table = new TableView<>();
        table.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        table.setTableMenuButtonVisible(true);
        table.getStylesheets().add(TABLE_STYLE);

        states = aircraftStates;
        selectedA = selectedAircraft;


        //Columns set up :
        createTextColumn("OACI", OACI_PREFERRED_WIDTH,
                f -> new ReadOnlyObjectWrapper<>(f.getIcaoAddress().string()));

        createTextColumn("Immatriculation", OACI_PREFERRED_WIDTH,
                f ->  (f.callSignProperty().map(CallSign::string)));

        createTextColumn("Immatriculation", REG_PREFERRED_WIDTH, f -> {
            if(f.getAircraftData() == null ) {
                return new ReadOnlyStringWrapper("");
            } else {
                return new ReadOnlyStringWrapper(f.getAircraftData().registration().string());
            }
        });

        createTextColumn("Modèle", MOD_PREFERRED_WIDTH, f ->
                f.getAircraftData() == null ?
                        new ReadOnlyStringWrapper("") :
                        new ReadOnlyStringWrapper(f.getAircraftData().model()));

        createTextColumn("Type", TYP_PREFERRED_WIDTH, f ->
                f.getAircraftData() == null ?
                        new ReadOnlyStringWrapper("") :
                        new ReadOnlyStringWrapper(f.getAircraftData().typeDesignator().string()));

        createTextColumn("Description", OACI_PREFERRED_WIDTH, f->
                f.getAircraftData() == null ?
                        new ReadOnlyStringWrapper("") :
                        new ReadOnlyObjectWrapper<>(f.getAircraftData().description().string()));
        //Setting a limit for the decimal representation of digits
        NumberFormat nf0 = NumberFormat.getInstance();
        nf0.setMaximumFractionDigits(FRACT_DIGITS_OTHERS);
        nf0.setMinimumFractionDigits(FRACT_DIGITS_OTHERS);

        NumberFormat nf1 = NumberFormat.getInstance();
        nf1.setMaximumFractionDigits(FRACT_DIGITS_POSITION);
        nf1.setMinimumFractionDigits(FRACT_DIGITS_OTHERS);

        createNumericColumn("longitude(°)",
                f -> f.positionProperty().map(GeoPos::longitude), nf1, Units.Angle.DEGREE);
        createNumericColumn("altitude(m)",
                f -> f.positionProperty().map(GeoPos::latitude),nf1, Units.Angle.DEGREE);
        createNumericColumn("Vitesse(km/h)",
                ObservableAircraftState::altitudeProperty, nf0, Units.Length.METER);
        createNumericColumn("Vitesse(km/h)",
                ObservableAircraftState::velocityProperty, nf0, Units.Speed.KILOMETER_PER_HOUR);
        installListeners();
        installHandlers();
    }

    //create table's text columns
    private  void createTextColumn(String title, int width,
                                   Function<ObservableAircraftState, ObservableValue<String>> function){
        TableColumn<ObservableAircraftState,String> column= new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory( f -> function.apply(f.getValue()));
        table.getColumns().add(column);
    }

    // Creates a numeric column for the table.
    private  void createNumericColumn(String title,
                                      Function<ObservableAircraftState, ObservableValue<Number>> function,
                                      NumberFormat numberFormat, double unit){
        // Create a new TableColumn with the specified title
        TableColumn<ObservableAircraftState,String> column= new TableColumn<>(title);
        column.setPrefWidth(NUM_PREFERRED_WIDTH);
        column.getStyleClass().add("numeric");

        // Set the cell value factory to map the numeric value to a formatted string value
        column.setCellValueFactory( f -> function.apply(f.getValue()).map(n ->
                numberFormat.format(Units.convertTo(n.doubleValue(),unit))));

        // Add the column to the table
        table.getColumns().add(column);

        // Set the comparator for the column to handle sorting of numeric values
        column.setComparator((s1,s2) -> {
            if ( s1.isEmpty() || s2.isEmpty()){
                // Handle empty cells by sorting them to the bottom
                return s2.compareTo(s1);
            } else {
                Double d1 = null;
                Double d2 = null;
                try {
                    // Parse the numeric values from the formatted strings
                    d1 = numberFormat.parse(s1).doubleValue();
                    d2 = numberFormat.parse(s2).doubleValue();
                    // Compare the numeric values
                    return Double.compare(d1, d2);
                } catch (ParseException ignored) {
                }
                return Double.compare(d1, d2);
            }
        });
    }

    private void installListeners(){
        //listener that updates the table when the aircraft states change
        states.addListener((SetChangeListener<ObservableAircraftState>) c -> {
            if (c.wasAdded()) {
                table.getItems().add(c.getElementAdded()); // if another aircraft state is added to the
                // list then add it to the table
            } else if (c.wasRemoved()){
                table.getItems().remove(c.getElementRemoved()); // if an aircraft state is removed then remove it
            }                                                   // from the table

            table.sort();
        });

        //listener on selected aircraft
        selectedA.addListener((o, oV, nV) -> {
            if (!table.getSelectionModel().isSelected(nV.getCategory())) { /* if the selected aircraft isn't the on */
                table.scrollTo(nV);                                        /* selected on the table then scroll to it*/

            }
            table.getSelectionModel().select(nV); /* selectioner l'avion dans la table */
        });

        // if an aircraft is selected on the table set it as selected aircraft
        table.getSelectionModel().selectedItemProperty().addListener((o, oV, nV) -> selectedA.set(nV));

    }


    /**
     * Returns the highest node of the scene graph
     * @return  highest node
     */
    public TableView pane(){
        return table;
    }

    /**
     * When an aircraft is selected calls it's accept method
     * @param aircraftState selected aircraft
     **/
    public void setOnDoubleClick(Consumer<ObservableAircraftState> aircraftState){
        consumer = aircraftState;
    }

    private void installHandlers() {
        table.setOnMouseClicked(c -> { //accepts the consummer when a doubleclick on the table occurs
            if (c.getButton().equals(MouseButton.PRIMARY) && c.getClickCount() == 2 && consumer != null &&
                    table.getSelectionModel().getSelectedItem() != null) {
                consumer.accept(table.getSelectionModel().getSelectedItem());
            }
        });
    }
}