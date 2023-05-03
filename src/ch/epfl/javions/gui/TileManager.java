package ch.epfl.javions.gui;

import javafx.scene.image.Image;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;

import static ch.epfl.javions.Preconditions.*;

public final class TileManager {
    private static final int MAX_CAPACITY = 100;
    private static final float LOAD_FACTOR = 0.75f;
    private static final boolean LRU_ORDER = true;
    private final Path discCachePath;
    private final String serverAddress;
    private final LinkedHashMap<TileId, Image> memoryCache = new LinkedHashMap<>(MAX_CAPACITY, LOAD_FACTOR, LRU_ORDER);

    public record TileId(int zoom, int x, int y) {
        public static boolean isValid(int zoom, int x, int y) {
            return ( 6 <= zoom && zoom <= 19 )
                    && ( 0 <= x && x <= ( 1 << ( zoom + 1 ) ) )
                    && ( 0 <= y && y <= ( 1 << ( zoom + 1 ) ) );
        }
    }

    public TileManager(Path filePath, String serverAddress) {
        this.discCachePath = filePath;
        this.serverAddress = serverAddress;
    }

    public Image imageForTileAt(TileId tileId) throws IOException {
        checkArgument(TileId.isValid(tileId.zoom(), tileId.x(), tileId.y()));

        Path localPath = Path.of(tileId.zoom + "/" + tileId.x + "/" + tileId.y + ".png") ;
        Path globalPath = Path.of(discCachePath + "/" + localPath);

        //check if image is already stored in memory cache
        if (memoryCache.containsKey(tileId)){// TODO: 02.05.23 contains ?
            System.out.println("tile was extract from the memory cache");

            return memoryCache.get(tileId);
        } else {
            //if not, check in the disc cache if it contains the image
            if (Files.exists(globalPath)) {
                //if so, put it in the memory cache et return it
                FileInputStream fileIn = new FileInputStream(globalPath.toString());
                Image image = new Image(fileIn);
                Iterator<TileId> i = memoryCache.keySet().iterator();
                if (memoryCache.size() == MAX_CAPACITY) {
                    memoryCache.remove(i.next());
                    System.out.println("tile was removed from memory cache");
                }
                memoryCache.put(tileId, image);// TODO: 02.05.23 put ?
                System.out.println("tile was extracted from the disc cache");

                return image;
            } else {
                //if not, get it from the server, put it in the memory and disc cache, and return it
                URL u = new URL("https://" + serverAddress + "/" + localPath);
                URLConnection c = u.openConnection();
                c.setRequestProperty("User-Agent", "Javions");
                try (InputStream i = c.getInputStream()) {
                    Files.createDirectories( Path.of(globalPath.getParent() + "/"));
                    FileOutputStream fileOut = new FileOutputStream(globalPath.toString());
                    fileOut.write(i.readAllBytes());
                    System.out.println("tile was extracted from the server");
                    return new Image(new ByteArrayInputStream( i.readAllBytes() ) );
                }
            }
        }
    }
}
