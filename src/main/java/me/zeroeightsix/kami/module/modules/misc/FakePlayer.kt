package me.zeroeightsix.kami.module.modules.misc

import com.mojang.authlib.GameProfile
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.GuiEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.formatValue
import me.zeroeightsix.kami.util.threads.onMainThreadSafe
import me.zeroeightsix.kami.util.threads.runSafeR
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.GuiGameOver
import org.kamiblue.event.listener.listener
import java.util.*

object FakePlayer : Module(
    name = "FakePlayer",
    description = "Spawns a client sided fake player",
    category = Category.MISC
) {
    private val copyInventory by setting("CopyInventory", false)
    val playerName by setting("PlayerName", "Player")

    private var fakePlayer: EntityOtherPlayerMP? = null
    private const val ENTITY_ID = -7170400

    init {
        listener<ConnectionEvent.Disconnect> {
            disable()
        }

        listener<GuiEvent.Displayed> {
            if (it.screen is GuiGameOver) disable()
        }

        onEnable {
            runSafeR {
                if (playerName == "Player") {
                    MessageSendHelper.sendChatMessage("You can use " +
                        formatValue("${CommandManager.prefix}set FakePlayer PlayerName <name>") +
                        " to set a custom name")
                }

                fakePlayer = EntityOtherPlayerMP(Companion.mc.world, GameProfile(UUID.randomUUID(), playerName)).apply {
                    copyLocationAndAnglesFrom(Companion.mc.player)
                    rotationYawHead = Companion.mc.player.rotationYawHead
                    if (copyInventory) inventory.copyInventory(Companion.mc.player.inventory)
                }.also {
                    Companion.mc.world.addEntityToWorld(ENTITY_ID, it)
                }
            } ?: disable()
        }

        onDisable {
            onMainThreadSafe {
                fakePlayer?.setDead()
                world.removeEntityFromWorld(-911)
            }
        }
    }
}