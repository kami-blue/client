package org.kamiblue.client.util

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.WorldClient
import org.kamiblue.client.KamiMod
import org.kamiblue.client.event.events.ShutdownEvent
import org.kamiblue.client.util.ConfigUtils.saveAll

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

    @JvmStatic
    fun saveAndShutdown() {
        if (!KamiMod.ready) return

        ShutdownEvent.post()
        saveAll()
    }
}