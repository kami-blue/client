package me.zeroeightsix.kami.util

import net.minecraft.client.Minecraft
import net.minecraft.network.play.client.CPacketPlayer
import kotlin.math.round

/**
 * @author Xiaro
 */
object CenterPlayer {
    private val mc = Minecraft.getMinecraft()

    /**
     * @return the position x of the nearest block center
     */
    fun getPosX(offsetMultiplier: Float): Double {
        val offset = round(mc.player.posX + 0.5) - 0.5 - mc.player.posX
        return mc.player.posX + offset * offsetMultiplier
    }

    /**
     * @return the position z of the nearest block center
     */
    fun getPosZ(offsetMultiplier: Float): Double {
        val offset = round(mc.player.posZ + 0.5) - 0.5 - mc.player.posZ
        return mc.player.posZ + offset * offsetMultiplier
    }

    /**
     * Move player to the nearest block center
     */
    fun centerPlayer(offsetMultiplier: Float) {
        val posX = getPosX(offsetMultiplier)
        val posZ = getPosZ(offsetMultiplier)
        mc.player.connection.sendPacket(CPacketPlayer.Position(posX, mc.player.posY, posZ, mc.player.onGround))
        mc.player.setPosition(posX, mc.player.posY, posZ)
    }
}