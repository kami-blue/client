// 
// Decompiled by Procyon v0.5.36
// 

package the_fireplace.ias.tools;

import java.net.URLConnection;
import java.io.IOException;
import java.net.URL;

public class HttpTools
{
    public static boolean ping(final String url) {
        try {
            final URLConnection con = new URL(url).openConnection();
            con.connect();
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }
}
