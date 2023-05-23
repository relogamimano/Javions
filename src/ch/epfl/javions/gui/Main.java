package ch.epfl.javions.gui;


import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import static java.nio.file.Path.of;

public class Main extends Application {
    ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<>();
    public static void main(String[] args) {launch(args);}


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
        InputStream in = System.in;
        List<String> raw = getParameters().getRaw();
        final String str = raw.isEmpty() ? null : raw.get(0);


        Thread messagesThread = new Thread(() -> {
            while(true) {
                try {
//                    Thread.sleep();
                    Supplier<RawMessage> supplier = str != null
                            ? rawMessageSupplier(str)
                            : defautl();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            // lauche anim

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

        var mi = readAllMessages("resources/messages_20230318_0915.bin").iterator();

        // Animation des aéronefs
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    for (int i = 0; i < 10; i += 1) {
//                        RawMessage rawMessage =
                        Message m = MessageParser.parse(mi.next());
//                        m = Objects.requireNonNull();
                        if (m != null) {
                            //slc
                            asm.updateWithMessage(m);
                        }
                        asm.purge();
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();



    }

    /// meth

    private Supplier<RawMessage> rawMessageSupplier(String str) throws IOException {
        List<RawMessage> rr = readAllMessages(str);
        return new Supplier<>() {
            int ix = 0;
            @Override
            public RawMessage get() {
                return rr.get(ix++);
            }

        };
    }


    private Supplier<RawMessage> defautl() throws IOException {
        AdsbDemodulator adsbDemodulator = new AdsbDemodulator(System.in);
        return new Supplier<RawMessage>() {
            @Override
            public RawMessage get() {
                try {
                    return adsbDemodulator.nextMessage();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };


    }
}
