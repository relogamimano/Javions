package ch.epfl.javions.gui;


import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.nio.file.Path.of;

public class Main extends Application {
    ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<>();
    public static void main(String[] args) {launch(args);}

    @Override
    public void start(Stage primaryStage) throws Exception {
        InputStream i = System.in;

        Thread messagesThread = new Thread(() -> {



        });
        messagesThread.setDaemon(true);
        messagesThread.start();



        Path tileCache = of("tile-cache");
        TileManager tm = new TileManager(tileCache, "tile.openstreetmap.org");
        MapParameters mp = new MapParameters(8, 33530, 23070);
        URL u = getClass().getResource("/aircraft.zip");
        assert u != null;
        Path p = Path.of(u.toURI());
        AircraftDatabase db = new AircraftDatabase(p.toString());

        AircraftStateManager asm = new AircraftStateManager(db);
        ObjectProperty<ObservableAircraftState> sap = new SimpleObjectProperty<>();
        BaseMapController bmc = new BaseMapController(tm, mp);
        AircraftController ac = new AircraftController(mp, asm.states(), sap);

        StackPane aircraftView = new StackPane(bmc.pane(), ac.pane());
        BorderPane aircraftConsole = new BorderPane(/*table, ligne d'etats*/);


        SplitPane splitPane = new SplitPane(aircraftView, aircraftConsole);
        primaryStage.setTitle("Javions");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();

//        var mi = readAllMessages("resources/messages_20230318_0915.bin").iterator();
//
//        // Animation des aéronefs
//        new AnimationTimer() {
//            @Override
//            public void handle(long now) {
//                try {
//                    for (int i = 0; i < 6; i += 1) {
//                        Message m = MessageParser.parse(mi.next());
//                        if (m != null) {
//                            asm.updateWithMessage(m);
//
//                        }
//                    }
//                } catch (IOException e) {
//                    throw new UncheckedIOException(e);
//                }
//            }
//        }.start();



    }
}
