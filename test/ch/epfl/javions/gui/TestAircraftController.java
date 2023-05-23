package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Path.of;

public final class TestAircraftController extends Application {




    public static void main(String[] args) { launch(args); }

    static List<RawMessage> readAllMessages(String fileName)
            throws IOException {
        List<RawMessage> list = new ArrayList<>();
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(fileName)))) {
            byte[] bytes = new byte[RawMessage.LENGTH];

            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                RawMessage rawMessage = new RawMessage(timeStampNs, new ByteString(bytes));
                list.add(rawMessage);
            }
        } catch (EOFException e) {
            return list;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {


        Path tileCache = of("tile-cache");
        TileManager tm = new TileManager(tileCache, "tile.openstreetmap.org");
        MapParameters mp =
//                new MapParameters(17, 17_389_327, 11_867_430);
//                new MapParameters(17, 17_393_500, 11_863_700);
                new MapParameters(10, 135400, 92550);
        BaseMapController bmc = new BaseMapController(tm, mp);

        // Création de la base de données
        URL dbUrl = getClass().getResource("/aircraft.zip");
        assert dbUrl != null;
        String f = of(dbUrl.toURI()).toString();
        var db = new AircraftDatabase(f);

        AircraftStateManager asm = new AircraftStateManager(db);
        ObjectProperty<ObservableAircraftState> sap = new SimpleObjectProperty<>();

        AircraftController ac = new AircraftController(mp, asm.states(), sap);
//        for () {
//
//            n.setOnMouseClicked(event -> {
//                String id = n.getId();
//                asm.states().
//                    });
//        }

        StackPane root = new StackPane(bmc.pane(), ac.pane());

        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        var mi = readAllMessages("resources/messages_20230318_0915.bin").iterator();

        // Animation des aéronefs
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    for (int i = 0; i < 6; i += 1) {
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
