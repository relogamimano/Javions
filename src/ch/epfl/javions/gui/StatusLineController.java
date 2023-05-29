package ch.epfl.javions.gui;

import javafx.beans.property.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
/**
 * Status Line Controller display the number of visible aircraft and the number of received messages
 * @author Sofia Henriques Garfo (346298)
 * @author Romeo Maignal (360568)
 */
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

    /**
     * Getter method used to access the pane
     * @return (Pane) pane
     */
    public BorderPane pane() { return pane; }

    /**
     * Getter method used to access the modifiable aircraft count property
     * @return (int) modifiable aircraft count property
     */
    public IntegerProperty aircraftCountProperty() { return aircraftCountProperty; }

    /**
     * Getter methode used to access the modifiable message count property
     * @return (long) modifiable message count property
     */

    public LongProperty messageCountProperty() { return messageCountProperty; }
}
