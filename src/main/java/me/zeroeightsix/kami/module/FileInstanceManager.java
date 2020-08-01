package me.zeroeightsix.kami.module;

import me.zeroeightsix.kami.util.Macro;
import me.zeroeightsix.kami.util.Waypoint;
import me.zeroeightsix.kami.util.WaypointInfo;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dominikaaaa
 * Lazy fix used for Java instance of {@link me.zeroeightsix.kami.util.Macro} and {@link MacroManager}
 */
public class FileInstanceManager {
    /*
     * Map of all the macros.
     * KeyCode, Actions
     */
    public static Map<Integer, List<String>> macros = new LinkedHashMap<>();

    /*
     * ArrayList of all Waypoints
     */
    public static ArrayList<WaypointInfo> waypoints = new ArrayList<>();

    /**
     * Super lazy fix for Windows users sometimes saving empty files
     */
    public static void fixEmptyFiles() {
        if (!Waypoint.INSTANCE.getFile().exists()) {
            try {
                FileWriter w = new FileWriter(Waypoint.INSTANCE.getFile());
                w.write("[]");
                w.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!Macro.INSTANCE.getFile().exists()) {
            try {
                FileWriter w = new FileWriter(Macro.INSTANCE.getFile());
                w.write("{}");
                w.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
