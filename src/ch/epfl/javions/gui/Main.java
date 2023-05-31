package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.SocketImpl;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import static ch.epfl.javions.Units.*;

/**
 * Main class for launching the program
 *
 * @author Sofia Henriques Garfo (346298)
 * @author Romeo Maignal (360568)
 */
public class Main extends Application {
    private int aircraftCount = 0;
    private long messageCount = 0;
    private static final int STARTING_ZOOM = 8;
    private static final int STARTING_MIN_X = 33530;
    private static final int STARTING_MIN_Y = 23070;
    private static final int STARTING_MIN_WIDTH = 800;
    private static final int STARTING_MIN_HEIGHT = 600;
    private static final String TITLE = "Javions";
    private final ConcurrentLinkedQueue<RawMessage> queue = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        launch(args);
    }

    static List<RawMessage> readAllMessages(String fileName) throws IOException {
        List<RawMessage> list = new ArrayList<>();
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(fileName)))) {
            byte[] bytes = new byte[RawMessage.LENGTH];
            //infinite loop broken by the assert key word when messages can no longer be read
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

    /**
     * Recursive method used to launch the program by calling itself at its end.
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages.
     * @throws Exception if the reading of the input stream faces issues
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        long bootTime = System.nanoTime();
        List<String> raw = getParameters().getRaw();
        final String str = raw.isEmpty() ? null : raw.get(0);
        Supplier<RawMessage> supplier = (str == null)
                ? airSpySupplier()
                : defaultMessageSupplier(str);
        //Thread class used to delegate the message processing to another computing entity of the machine
        Thread messagesThread = new Thread(() -> {
            while (true) {
                long elapsedTime = System.nanoTime() - bootTime;
                RawMessage rm = supplier.get();
                if (Objects.isNull(rm)) continue;
                //Condition makes the process (of placing an aircraft on map) wait for the right moment according to its real time chronology
                if (rm.timeStampNs() > elapsedTime) {
                    try {
                        Thread.sleep((long) convertFrom(rm.timeStampNs() - elapsedTime, NANO / MILLI));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                queue.add(rm);
            }
        });
        messagesThread.setDaemon(true);
        messagesThread.start();

        //Set up of the graphic user interface
        //Data
        Path tileCache = Path.of("tile-cache");
        TileManager tm = new TileManager(tileCache, "tile.openstreetmap.org");
        MapParameters mp = new MapParameters(STARTING_ZOOM, STARTING_MIN_X, STARTING_MIN_Y);
        URL u = getClass().getResource("/aircraft.zip");
        assert u != null;
        Path p = Path.of(u.toURI());
        AircraftDatabase db = new AircraftDatabase(p.toString());

        AircraftStateManager asm = new AircraftStateManager(db);
        ObjectProperty<ObservableAircraftState> sap = new SimpleObjectProperty<>();

        //Controllers
        BaseMapController bmc = new BaseMapController(tm, mp);
        AircraftController ac = new AircraftController(mp, asm.states(), sap);
        AircraftTableController atc = new AircraftTableController(asm.states(), sap);
        StatusLineController slc = new StatusLineController();
        slc.aircraftCountProperty().bind(
                Bindings.createIntegerBinding(() -> asm.states().size(),
                        asm.states()));

        //View
        StackPane aircraftView = new StackPane(bmc.pane(), ac.pane());
        BorderPane aircraftConsole = new BorderPane();
        aircraftConsole.setCenter(atc.pane());
        aircraftConsole.setTop(slc.pane());
        SplitPane splitPane = new SplitPane(aircraftView, aircraftConsole);
        splitPane.setOrientation(Orientation.VERTICAL);

        //Stage settings
        primaryStage.setScene(new Scene(splitPane));
        primaryStage.setTitle(TITLE);
        primaryStage.setMinWidth(STARTING_MIN_WIDTH);
        primaryStage.setMinHeight(STARTING_MIN_HEIGHT);
        primaryStage.show();

        atc.setOnDoubleClick((state) -> {
            bmc.centerOn(state.positionProperty().getValue());
        });

        // Aircraft animation
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    while (!queue.isEmpty()) {
                        RawMessage rawMessage = queue.poll();
                        Message m = Objects.isNull(rawMessage) ? null : MessageParser.parse(rawMessage);
                        if (m != null) {
                            //message flow
                            slc.messageCountProperty().set(
                                    slc.messageCountProperty().get() + 1L);
                            asm.updateWithMessage(m);
                        }
                        asm.purge();//check on the right of the aircraft to remain on screen
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();
    }

    // Supplier methode represents a supplier of results from the raw messages.
    // Its sole purpose is to supply (or provide) values dynamically upon request.
    private Supplier<RawMessage> defaultMessageSupplier(String str) throws IOException {
        List<RawMessage> l = readAllMessages(str);
        Iterator<RawMessage> it = l.iterator();
        return it::next;
    }

    // Supplier methode represents a supplier of results from the AirSpy2 machine that does not accept any arguments.
    // Its sole purpose is to supply (or provide) values dynamically upon request.
    private Supplier<RawMessage> airSpySupplier() throws IOException {
        AdsbDemodulator adsbDemodulator = new AdsbDemodulator(System.in);
        return () -> {
            try {

                return adsbDemodulator.nextMessage();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
