package me.zeroeightsix.kami.util.bewwawho;

import com.google.gson.Gson;
import me.zeroeightsix.kami.KamiMod;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStreamReader;
import java.net.URL;

/***
 * @author S-B99
 */
public class RichPresence {

    public static RichPresence INSTANCE;
    public CustomUser[] customUsers;

    public class CustomUser {
        public String uuid;
        public String type;
    }

    public RichPresence() {
        INSTANCE = this;
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(KamiMod.DONATORS_JSON).openConnection();
            connection.connect();
            this.customUsers = new Gson().fromJson(new InputStreamReader(connection.getInputStream()), CustomUser[].class);
            connection.disconnect();
        } catch (Exception e) {
            KamiMod.log.error("Failed to load donators");
            e.printStackTrace();
        }
    }
}
