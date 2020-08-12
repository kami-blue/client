// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.cape;

import java.util.Arrays;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

public class Capes
{
    public static ArrayList<String> lines;
    private static List<String> users;
    
    public static void getUsersCape() {
        try {
            final URL url = new URL("https://pastebin.com/raw/Lp1VD9Kv");
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Capes.lines.add(line);
            }
            bufferedReader.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static boolean isCapeUser(final String name) {
        return Capes.lines.contains(name);
    }
    
    static {
        Capes.lines = new ArrayList<String>();
        Capes.users = Arrays.asList("samse11", "Kotori_OAO");
    }
}
