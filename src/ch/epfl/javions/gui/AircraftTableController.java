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

    private static final int NUM_PREFERED_WIDTH = 85;
    private static final int OACI_PREFERED_WIDTH = 70;

    private static final int REG_PREFERED_WIDTH = 90;

    private static final int MOD_PREFERED_WIDTH = 230;

    private static final int TYP_PREFERED_WIDTH = 50;

    private static final int FRACT_DIGITS_POSITION = 4;

    private static final int FRACT_DIGITS_OTHERS = 0;


    private  final TableView<ObservableAircraftState> table;
    private final ObservableSet<ObservableAircraftState>  states;

    private final ObjectProperty<ObservableAircraftState> selectedA;
    private Consumer<ObservableAircraftState> consumer;

    private static  final String OACI = "OACI";

    private static  final String CALLSIGN = "Indicatif";
    private static  final String REGISTRATION = "Immatriculation";
    private static  final String MODEL = "Modèle";
    private static  final String TYPE = "Type";
    private static  final String DESCRIPTION = "Description";
    private static  final String LONGITUDE = "longitude(°)";
    private static  final String LATITUDE = "latitude(°)";
    private static  final String ALTITUDE = "altitude(m)";
    private static  final String VELOCITY = "Vitesse(km/h)";
    private static final String NUMERIC = "numeric";
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



        createTextColumn(OACI,OACI_PREFERED_WIDTH,
                f -> new ReadOnlyObjectWrapper<>(f.getIcaoAddress().string()));


        createTextColumn(CALLSIGN,OACI_PREFERED_WIDTH,
                f ->  (f.callSignProperty().map(CallSign::string)));


        createTextColumn(REGISTRATION,REG_PREFERED_WIDTH, f -> {
            if(f.getAircraftData() == null ) {
                return new ReadOnlyStringWrapper("");
            } else {
                return new ReadOnlyStringWrapper(f.getAircraftData().registration().string());
            }
        });

        createTextColumn(MODEL, MOD_PREFERED_WIDTH,  f ->
                f.getAircraftData() == null ?
                        new ReadOnlyStringWrapper("") :
                        new ReadOnlyStringWrapper(f.getAircraftData().model()));



        createTextColumn(TYPE, TYP_PREFERED_WIDTH, f ->
                f.getAircraftData() == null ?
                        new ReadOnlyStringWrapper("") :
                        new ReadOnlyStringWrapper(f.getAircraftData().typeDesignator().string()));


        createTextColumn(DESCRIPTION, OACI_PREFERED_WIDTH, f->
                f.getAircraftData() == null ?
                        new ReadOnlyStringWrapper("") :
                        new ReadOnlyObjectWrapper<>(f.getAircraftData().description().string()));

        NumberFormat nf0 = NumberFormat.getInstance();
        nf0.setMaximumFractionDigits(FRACT_DIGITS_OTHERS);
        nf0.setMinimumFractionDigits(FRACT_DIGITS_OTHERS);

        NumberFormat nf1 = NumberFormat.getInstance();
        nf1.setMaximumFractionDigits(FRACT_DIGITS_POSITION);
        nf1.setMinimumFractionDigits(FRACT_DIGITS_OTHERS);

        createNumericColumn(LONGITUDE,
                f -> f.positionProperty().map(GeoPos::longitude), nf1, Units.Angle.DEGREE);
        createNumericColumn(LATITUDE,
                f -> f.positionProperty().map(GeoPos::latitude),nf1, Units.Angle.DEGREE);
        createNumericColumn(ALTITUDE,
                ObservableAircraftState::altitudeProperty, nf0, Units.Length.METER);
        createNumericColumn(VELOCITY,
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

    // create tables numeric columns
    private  void createNumericColumn(String title,
                                      Function<ObservableAircraftState, ObservableValue<Number>> function,
                                      NumberFormat numberFormat, double unit){

        TableColumn<ObservableAircraftState,String> column= new TableColumn<>(title);
        column.setPrefWidth(NUM_PREFERED_WIDTH);
        column.getStyleClass().add(NUMERIC);
        column.setCellValueFactory( f -> function.apply(f.getValue()).map(n ->
                numberFormat.format(Units.convertTo(n.doubleValue(),unit))));
        table.getColumns().add(column);
        column.setComparator((s1,s2) -> { // TODO: Comment mieux ecrire le try/catch
            if ( s1.isEmpty() || s2.isEmpty()){

                return s2.compareTo(s1);
            } else {
                Double d1 = null;
                Double d2 = null;
                try {
                    d1 = numberFormat.parse(s1).doubleValue();
                    d2 = numberFormat.parse(s2).doubleValue();
                    return Double.compare(d1, d2);
                } catch (ParseException ignored) {
                }
                return Double.compare(d1, d2);
            }
        });
    }



    private void installListeners(){
        states.addListener((SetChangeListener<ObservableAircraftState>) c -> {
            if (c.wasAdded()) {
                table.getItems().add(c.getElementAdded());
            } else if (c.wasRemoved()){
                table.getItems().remove(c.getElementRemoved());
            }

            table.sort();
        });



        selectedA.addListener((o, oV, nV) -> {
            if (!table.getSelectionModel().isSelected(nV.getCategory())) {
                table.scrollTo(nV);

            }
            table.getSelectionModel().select(nV);
        });


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
