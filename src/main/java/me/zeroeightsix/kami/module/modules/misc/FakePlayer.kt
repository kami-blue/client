package me.zeroeightsix.kami.module.modules.misc

import com.mojang.authlib.GameProfile
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.GuiScreenEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.GuiGameOver
import org.kamiblue.event.listener.listener
import java.util.*

@Module.Info(
    name = "FakePlayer",
    description = "Spawns a client sided fake player",
    category = Module.Category.MISC
)
object FakePlayer : Module() {
    private val copyInventory = register(Settings.b("CopyInventory", false))
    val playerName = register(Settings.stringBuilder("PlayerName").withValue("Player").withVisibility { false })

    private var fakePlayer: EntityOtherPlayerMP? = null
    private const val ENTITY_ID = -7170400

    init {
        listener<ConnectionEvent.Disconnect> {
            disable()
        }

        listener<GuiScreenEvent.Displayed> {
            if (it.screen is GuiGameOver) disable()
        }
    }

    override fun onEnable() {
        if (mc.world == null || mc.player == null) {
            disable()
            return
        }

        if (playerName.value == "Player") {
            MessageSendHelper.sendChatMessage("You can use &7'${Command.commandPrefix.value}fp <name>'&r to set a custom name")
        }

        fakePlayer = EntityOtherPlayerMP(mc.world, GameProfile(UUID.randomUUID(), playerName.value)).apply {
            copyLocationAndAnglesFrom(mc.player)
            rotationYawHead = mc.player.rotationYawHead
            if (copyInventory.value) inventory.copyInventory(mc.player.inventory)
        }.also {
            mc.world.addEntityToWorld(ENTITY_ID, it)
        }
    }

    override fun onDisable() {
        mc.addScheduledTask {
            if (mc.world == null || mc.player == null) return@addScheduledTask
            fakePlayer?.setDead()
            mc.world?.removeEntityFromWorld(-911)
        }
    }
}