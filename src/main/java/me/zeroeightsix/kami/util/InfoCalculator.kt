package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.util.LagCompensator.tickRate
import me.zeroeightsix.kami.util.math.MathUtils.round
import kotlin.math.hypot

/**
 * @author l1ving
 * Created by l1ving on 18/01/20
 * Updated by l1ving on 06/02/20
 * Updated by Xiaro on 11/09/20
 *
 *
 * Speed:
 * @author l1ving
 * Created by l1ving on 18/01/20
 * Credit to Seppuku for the following calculation I made more efficient and got inspiration from
 * final String bps = "BPS: " + df.format((MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ) / tickRate));
 *
 *
 * Durability:
 * @author TBM
 * Created by TBM on 8/12/19
 *
 *
 * TPS:
 * @author 086
 */
object InfoCalculator {
    private val mc = Wrapper.minecraft

    /**
     * @return Server type
     */
    @JvmStatic
    fun getServerType() = if (mc.isIntegratedServerRunning) "Singleplayer" else mc.currentServerData?.serverIP
            ?: "MainMenu"

    /**
     * @return Ping
     */
    @JvmStatic
    fun ping() = mc.player?.let { mc.connection?.getPlayerInfo(it.uniqueID)?.responseTime ?: 1 } ?: -1


    /**
     * @return Speed
     */
    @JvmStatic
    fun speed(useUnitKmH: Boolean): Double {
        val tps = 1000.0 / mc.timer.tickLength
        val multiply = if (useUnitKmH) 3.6 else 1.0 // convert mps to kmh
        return hypot(mc.player.posX - mc.player.prevPosX, mc.player.posZ - mc.player.prevPosZ) * multiply * tps
    }

    /**
     * @return Durability
     */
    @JvmStatic
    fun dura() = with(mc.player.heldItemMainhand) { maxDamage - getItemDamage() }

    /**
     * @return Memory usage
     */
    @JvmStatic
    fun memory() = (Runtime.getRuntime().freeMemory() / 1000000L)

    /**
     * @return Ticks per second
     */
    @JvmStatic
    fun tps(places: Int) = round(tickRate, places)

    /**
     * @return Dimension
     */
    @JvmStatic
    fun dimension() = when (mc.player?.dimension) {
        -1 -> "Nether"
        0 -> "Overworld"
        1 -> "End"
        else -> "No Dimension"
    }
}