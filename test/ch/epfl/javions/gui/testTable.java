package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import static javafx.application.Application.launch;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class testTable extends Application {
    public static void main(String[] args) { launch(args); }

    static List<RawMessage> readAllMessages(String fileName) throws IOException {
        List<RawMessage> messages = new ArrayList<>();
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream("resources/messages_20230318_0915.bin")))){
            byte[] bytes = new byte[RawMessage.LENGTH];
            int bytesRead = -1;
            while (bytesRead != 0) {
                long timeStampNs = s.readLong();
                bytesRead = s.readNBytes(bytes, 0, bytes.length);
                messages.add(RawMessage.of(timeStampNs, bytes));
            }
        } catch (Exception e) { /* nothing to do */ }
        return messages;
    }

    @Override
    public void start(Stage primaryStage) throws URISyntaxException, IOException {
        Path tileCache = Path.of("tile-cache");
        TileManager tm =
                new TileManager(tileCache, "tile.openstreetmap.org");
        MapParameters mp =
                new MapParameters(17, 17_389_327, 11_867_430);
        BaseMapController bmc = new BaseMapController(tm, mp);

        URL dbUrl = getClass().getResource("/aircraft.zip");
        assert dbUrl != null;
        String f = Path.of(dbUrl.toURI()).toString();
        var db = new AircraftDatabase(f);

        AircraftStateManager asm = new AircraftStateManager(db);



        ObjectProperty<ObservableAircraftState> sap =
                new SimpleObjectProperty<>();
        AircraftTableController atc =
                new AircraftTableController( asm.states(), sap);

        var root = new StackPane(bmc.pane(), atc.pane());
        primaryStage.setScene(new Scene(root));
        primaryStage.show();


        var mi = readAllMessages("resources/messages_20230318_0915.bin")
                .iterator();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    for (int i = 0; i < 10; i += 1) {
                        Message m = MessageParser.parse(mi.next());
                        if (m != null) asm.updateWithMessage(m);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();
    }


}