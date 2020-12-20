package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.mixin.extension.tickLength
import me.zeroeightsix.kami.mixin.extension.timer
import org.kamiblue.commons.utils.MathUtils.round
import kotlin.math.hypot

object InfoCalculator {
    private val mc = Wrapper.minecraft

    fun getServerType() = if (mc.isIntegratedServerRunning) "Singleplayer" else mc.currentServerData?.serverIP
        ?: "Main Menu"

    @JvmStatic
    fun ping() = mc.player?.let { mc.connection?.getPlayerInfo(it.uniqueID)?.responseTime ?: 1 } ?: -1

    fun speed(useUnitKmH: Boolean): Double {
        val tps = 1000.0 / mc.timer.tickLength
        val multiply = if (useUnitKmH) 3.6 else 1.0 // convert mps to kmh
        return hypot(mc.player.posX - mc.player.prevPosX, mc.player.posZ - mc.player.prevPosZ) * multiply * tps
    }

    fun heldItemDurability() = with(mc.player.heldItemMainhand) { maxDamage - itemDamage }

    fun memory() = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L

    @JvmStatic
    fun tps(places: Int) = round(TpsCalculator.tickRate, places)

    fun dimension() = when (mc.player?.dimension) {
        -1 -> "Nether"
        0 -> "Overworld"
        1 -> "End"
        else -> "No Dimension"
    }
}
