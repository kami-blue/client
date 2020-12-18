package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.util.text.TextFormatting
import org.lwjgl.input.Keyboard

/**
 * Created by 086 on 11/11/2017.
 *
 * TODO: Refactor into appropriate utils
 */
object Wrapper {
    @JvmStatic
    val minecraft: Minecraft
        get() = Minecraft.getMinecraft()

    @JvmStatic
    val player: EntityPlayerSP?
        get() = minecraft.player

    @JvmStatic
    val world: WorldClient?
        get() = minecraft.world

    fun getKey(keyName: String): Int {
        return Keyboard.getKeyIndex(keyName.toUpperCase())
    }

    fun getKeyName(keycode: Int): String {
        return Keyboard.getKeyName(keycode)
    }

    fun sendUnknownKeyError(bind: String) {
        MessageSendHelper.sendErrorMessage("Unknown key [" +
            TextFormatting.GRAY + bind + TextFormatting.RESET +
            "]! left alt is " +
            TextFormatting.GRAY + "lmenu" + TextFormatting.RESET +
            ", left Control is " +
            TextFormatting.GRAY + "lcontrol" + TextFormatting.RESET +
            " and ` is " +
            TextFormatting.GRAY + "grave" + TextFormatting.RESET +
            ". You cannot bind the &7meta&f key."
        )
    }
}