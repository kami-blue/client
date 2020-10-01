package me.zeroeightsix.kami.util

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import net.minecraft.network.Packet
import net.minecraftforge.fml.client.FMLClientHandler
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * Created on 8/4/2017 by fr1kin
 * This code is licensed under MIT and can be found here:
 * https://github.com/fr1kin/ForgeHax/blob/2011740/src/main/java/com/matt/forgehax/util/PacketHelper.java
 *
 * Updated by Xiaro on 08/18/20
 */
object PacketHelper {
    private val CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(15L, TimeUnit.SECONDS)
            .build<Packet<*>, Boolean>(object : CacheLoader<Packet<*>?, Boolean>() {
                @Throws(Exception::class)
                override fun load(key: Packet<*>?): Boolean {
                    return false
                }
            })

    fun ignore(packet: Packet<*>) {
        CACHE.put(packet, true)
    }

    fun ignoreAndSend(packet: Packet<*>) {
        ignore(packet)
        FMLClientHandler.instance().clientToServerNetworkManager.sendPacket(packet)
    }

    fun isIgnored(packet: Packet<*>): Boolean {
        return try {
            CACHE[packet]
        } catch (e: ExecutionException) {
            false
        }
    }

    fun remove(packet: Packet<*>?) {
        CACHE.invalidate(packet)
    }
}