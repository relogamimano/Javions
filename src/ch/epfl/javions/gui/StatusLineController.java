package ch.epfl.javions.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.*;
import javafx.collections.ObservableSet;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public final class StatusLineController {
    private final BorderPane pane;
    private final IntegerProperty aircraftCountProperty;
    private final LongProperty messageCountProperty;
    public StatusLineController() {
        this.pane = new BorderPane();
        aircraftCountProperty = new SimpleIntegerProperty();
        messageCountProperty = new SimpleLongProperty();
        pane.getStylesheets().add("status.css");
        pane.setLeft(leftTxt());
        pane.setRight(rightTxt());
    }
    private Text leftTxt() {
        String leftRef = "Aéronefs visibles : ";
        Text leftTxt = new Text();
        leftTxt.textProperty().bind(
                aircraftCountProperty.map(m -> { return leftRef + m.intValue(); } ));
        return leftTxt;
    }
    private Text rightTxt() {
        String rightRef = "Messages reçus : ";
        Text rightTxt = new Text();
        rightTxt.textProperty().bind(
                messageCountProperty.map(m -> { return rightRef + m.longValue(); } ));
        return rightTxt;
    }

    public BorderPane pane() { return pane;}

    public IntegerProperty aircraftCountProperty() { return aircraftCountProperty; }

    public LongProperty messageCountProperty() { return messageCountProperty; }
}
