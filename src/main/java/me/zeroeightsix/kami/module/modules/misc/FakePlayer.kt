package me.zeroeightsix.kami.module.modules.misc

import com.mojang.authlib.GameProfile
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.entity.EntityOtherPlayerMP
import java.util.*

@Module.Info(
        name = "FakePlayer",
        description = "Spawns a client sided fake player",
        category = Module.Category.MISC
)
object FakePlayer : Module() {
    private val copyInventory = setting("CopyInventory", false)
    val playerName = setting("PlayerName", "Player", { false })

    init {
        listener<ConnectionEvent.Disconnect> {
            disable()
        }
    }

    override fun onEnable() {
        if (mc.world == null || mc.player == null) {
            disable()
            return
        }
        if (playerName.value == "Player") MessageSendHelper.sendChatMessage("You can use &7'${Command.commandPrefix.value}fp <name>'&r to set a custom name")
        EntityOtherPlayerMP(mc.world, GameProfile(UUID.randomUUID(), playerName.value)).apply {
            copyLocationAndAnglesFrom(mc.player)
            rotationYawHead = mc.player.rotationYawHead
            if (copyInventory.value) inventory.copyInventory(mc.player.inventory)
        }.also {
            mc.world.addEntityToWorld(-911, it)
        }
    }

    override fun onDisable() {
        if (mc.world == null || mc.player == null) return
        mc.world.removeEntityFromWorld(-911)
    }
}