package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.GuiScreenEvent.Displayed
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraft.client.gui.GuiGameOver
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.player.AttackEntityEvent

/**
 * @author polymer
 * @author cookiedragon234
 * Updated by polymer 10 March 2020
 * Updated by dominikaaaa on 12/04/20
 */
@Module.Info(
        name = "AutoEZ",
        category = Module.Category.COMBAT,
        description = "Sends an insult in chat after killing someone"
)
class AutoEZ : Module() {
    @JvmField
	var mode: Setting<Mode> = register(Settings.e("Mode", Mode.ONTOP))
    @JvmField
	var customText: Setting<String> = register(Settings.stringBuilder("CustomText").withValue("unchanged").withConsumer { _: String?, _: String? -> }.build())

    private var focus: EntityPlayer? = null
    private var hasBeenCombat = 0

    enum class Mode {
        GG("gg, \$NAME"),
        ONTOP("KAMI BLUE on top! ez \$NAME"),
        EZD("You just got ez'd \$NAME"),
        EZ_HYPIXEL("E Z Win \$NAME"),
        NAENAE("You just got naenae'd by kami blue plus, \$NAME"), CUSTOM;

        var text: String? = null

        constructor(text: String?) {
            this.text = text
        }

        constructor() // yes
    }

    private fun getText(m: Mode, playerName: String): String {
        return if (m == Mode.CUSTOM) {
            customText.value.replace("\$NAME", playerName)
        } else m.text!!.replace("\$NAME", playerName)
    }

    @EventHandler
    private val livingDeathEventListener = Listener(EventHook { event: AttackEntityEvent ->
        if (event.target is EntityPlayer) {
            focus = event.target as EntityPlayer
            if (event.entityPlayer.uniqueID === mc.player.uniqueID) {
                if (focus!!.health <= 0.0 || focus!!.isDead || !mc.world.playerEntities.contains(focus)) {
                    mc.player.sendChatMessage(getText(mode.value, event.target.name))
                    return@EventHook
                }
                hasBeenCombat = 1000
            }
        }
    })

    @EventHandler
    private val listener = Listener(EventHook { event: Displayed ->
        if (event.screen !is GuiGameOver) return@EventHook
        if (mc.player.health > 0) {
            hasBeenCombat = 0
        }
    })

    override fun onUpdate() {
        if (mc.player == null) return
        if (hasBeenCombat > 0 && (focus!!.health <= 0.0f || focus!!.isDead || !mc.world.playerEntities.contains(focus))) {
            mc.player.sendChatMessage(getText(mode.value, focus!!.name))
            hasBeenCombat = 0
        }
        --hasBeenCombat

        if (customText.value != "unchanged") return
        if (startTime == 0L) startTime = System.currentTimeMillis()
        if (startTime + 5000 <= System.currentTimeMillis()) { // 5 seconds in milliseconds
            if (mode.value == Mode.CUSTOM && customText.value.equals("unchanged", ignoreCase = true) && mc.player != null) {
                MessageSendHelper.sendWarningMessage("$chatName Warning: In order to use the custom " + name + ", please run the &7" + Command.getCommandPrefix() + "autoez&r command to change it, with '&7\$NAME&f' being the username of the killed player")
            }
            startTime = System.currentTimeMillis()
        }
    }

    companion object {
        private var startTime: Long = 0
    }
}