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
    private final TableView<ObservableAircraftState> table;
    private final ObservableSet<ObservableAircraftState>  states;

    private final ObjectProperty<ObservableAircraftState> selectedA;
    private Consumer<ObservableAircraftState> consumer;
    // TODO: final? static? pr tt


    public AircraftTableController(ObservableSet<ObservableAircraftState> aircraftStates,
                                   ObjectProperty<ObservableAircraftState> selectedAircraft){

        table = new TableView<>();
        table.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        table.setTableMenuButtonVisible(true);
        table.getStyleClass().add("table.ccs");

        states = aircraftStates;
        selectedA = selectedAircraft;



        createTextColumn("oaci",70, f -> new ReadOnlyObjectWrapper<>(f.getIcaoAddress().string()));


        createTextColumn("callSign",70, f ->  (f.callSignProperty().map(CallSign::string)));



        createTextColumn("model", 230,  f ->
                f.getAircraftData() == null ?
                        new ReadOnlyStringWrapper("") : new ReadOnlyStringWrapper(f.getAircraftData().model()));



        createTextColumn("type", 50, f ->
                f.getAircraftData() == null ?
                        new ReadOnlyStringWrapper("") : new ReadOnlyStringWrapper(f.getAircraftData().typeDesignator().string()));


        createTextColumn("description", 70, f->
                f.getAircraftData() == null ?
                        new ReadOnlyStringWrapper("") : new ReadOnlyObjectWrapper<>(f.getAircraftData().description().string()));

        NumberFormat nf0 = NumberFormat.getInstance();
        nf0.setMaximumFractionDigits(0);
        nf0.setMinimumFractionDigits(0);

        NumberFormat nf1 = NumberFormat.getInstance();
        nf1.setMaximumFractionDigits(4);
        nf1.setMinimumFractionDigits(0);



        createNumericColumn("altitude", ObservableAircraftState::altitudeProperty, nf0, Units.Length.METER);
        createNumericColumn("latitude", f -> f.positionProperty().map(GeoPos::latitude),nf1, Units.Angle.DEGREE);
        createNumericColumn("longitude", f -> f.positionProperty().map(GeoPos::longitude), nf1, Units.Angle.DEGREE);
        createNumericColumn("velocity",ObservableAircraftState::velocityProperty, nf0, Units.Speed.KILOMETER_PER_HOUR);

        installListeners();
        installHandlers();

    }

    private  void createTextColumn(String title, int width,
                                   Function<ObservableAircraftState, ObservableValue<String>> function){

        TableColumn<ObservableAircraftState,String> column= new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory( f -> function.apply(f.getValue()));
        table.getColumns().add(column);
    }

    private  void createNumericColumn(String title,
                                      Function<ObservableAircraftState, ObservableValue<Number>> function,
                                      NumberFormat numberFormat, double unit){

        TableColumn<ObservableAircraftState,String> column= new TableColumn<>(title);
        column.getStyleClass().add("numeric");
        column.setPrefWidth(85);
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

    private void installHandlers(){
        table.setOnMouseClicked( c -> {
            if (c.getButton().equals(MouseButton.PRIMARY) && c.getClickCount() == 2 && consumer!=null);{
                consumer.accept(table.getSelectionModel().getSelectedItem());
            }
        });
    }
}

