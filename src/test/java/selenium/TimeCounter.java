package selenium;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimeCounter {

    private static final Map<String, Long> STARTS = new ConcurrentHashMap<>();
    private static PrintWriter WRITER;

    static {
        try {
            WRITER = new PrintWriter(Files.newBufferedWriter(Path.of("response-times.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startTime(String id) {
        STARTS.put(id, System.currentTimeMillis());
    }

    public static void stopTime(String id, String extra) {
        long end = System.currentTimeMillis();
        long start = STARTS.get(id);
        WRITER.println(id + "," + extra + "," + (end - start));
    }

    public static void close() {
        WRITER.flush();
        WRITER.close();
    }

}
