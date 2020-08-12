// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.util;

import me.zeroeightsix.kami.KamiMod;
import java.io.Reader;
import java.io.InputStreamReader;
import com.google.gson.Gson;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class Donator
{
    public static Donator INSTANCE;
    public DonatorUser[] donatorUsers;
    
    public Donator() {
        Donator.INSTANCE = this;
        try {
            final HttpsURLConnection connection = (HttpsURLConnection)new URL("https://raw.githubusercontent.com/S-B99/KAMI/assets/assets/donators.json").openConnection();
            connection.connect();
            this.donatorUsers = (DonatorUser[])new Gson().fromJson((Reader)new InputStreamReader(connection.getInputStream()), (Class)DonatorUser[].class);
            connection.disconnect();
        }
        catch (Exception e) {
            KamiMod.log.error("Failed to load donators");
        }
    }
    
    public class DonatorUser
    {
        public String uuid;
    }
}
