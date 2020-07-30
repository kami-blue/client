package me.zeroeightsix.kami.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created on 8/4/2017 by fr1kin
 * This code is licensed under MIT and can be found here:
 * https://github.com/fr1kin/ForgeHax/blob/2011740/src/main/java/com/matt/forgehax/util/PacketHelper.java
 */
public class PacketHelper {

    private static final LoadingCache<Packet, Boolean> CACHE =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(15L, TimeUnit.SECONDS)
                    .build(new CacheLoader<Packet, Boolean>() {
                        @Override
                        public Boolean load(Packet key) throws Exception {
                            return false;
                        }
                    });

    public static void ignore(Packet packet) {
        CACHE.put(packet, true);
    }

    public static void ignoreAndSend(Packet packet) {
        ignore(packet);
        FMLClientHandler.instance().getClientToServerNetworkManager().sendPacket(packet);
    }

    public static boolean isIgnored(Packet packet) {
        try {
            return CACHE.get(packet);
        } catch (ExecutionException e) {
            return false;
        }
    }

    public static void remove(Packet packet) {
        CACHE.invalidate(packet);
    }
}
