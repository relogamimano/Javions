package ch.epfl.javions.gui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

/**
 * Status Line Controller display the number of visible aircraft and the number of received messages
 *
 * @author Sofia Henriques Garfo (346298)
 * @author Romeo Maignal (360568)
 */
public final class StatusLineController {
    private final BorderPane pane;
    private final IntegerProperty aircraftCountProperty;
    private final LongProperty messageCountProperty;
    private static final String STATUS_LINE_CSS_FILE = "status.css";

    public StatusLineController() {
        this.pane = new BorderPane();
        aircraftCountProperty = new SimpleIntegerProperty();
        messageCountProperty = new SimpleLongProperty();
        pane.getStylesheets().add(STATUS_LINE_CSS_FILE);
        pane.setLeft(leftTxt());
        pane.setRight(rightTxt());
    }

    private Text leftTxt() {
        Text leftTxt = new Text();
        leftTxt.textProperty().bind(
                aircraftCountProperty.map(m -> {
                    return "Messages reçus : " + m.intValue();
                }));
        return leftTxt;
    }

    private Text rightTxt() {
        Text rightTxt = new Text();
        rightTxt.textProperty().bind(
                messageCountProperty.map(m -> {
                    return "Aéronefs visibles : " + m.longValue();
                }));
        return rightTxt;
    }

    /**
     * Getter method used to access the pane
     *
     * @return (Pane) pane
     */
    public BorderPane pane() {
        return pane;
    }

    /**
     * Getter method used to access the modifiable aircraft count property
     *
     * @return (int) modifiable aircraft count property
     */
    public IntegerProperty aircraftCountProperty() {
        return aircraftCountProperty;
    }

    /**
     * Getter methode used to access the modifiable message count property
     *
     * @return (long) modifiable message count property
     */
    public LongProperty messageCountProperty() {
        return messageCountProperty;
    }
}
