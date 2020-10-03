package me.zeroeightsix.kami.module.modules.misc

import baritone.api.BaritoneAPI
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.movement.AutoWalk
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.math.MathUtils.CardinalMain
import me.zeroeightsix.kami.util.math.MathUtils.getPlayerMainCardinal
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.entity.player.EntityPlayer

@Module.Info(
        name = "AutoTunnel",
        description = "Automatically tunnels forward, at a given size",
        category = Module.Category.MISC
)
object AutoTunnel : Module() {
    private val backfill = register(Settings.b("Backfill", false))
    private val height = register(Settings.integerBuilder("Height").withRange(1, 10).withValue(2).build())
    private val width = register(Settings.integerBuilder("Width").withRange(1, 10).withValue(1).build())

    private var lastCommand = arrayOf("")
    private var startingDirection = CardinalMain.POS_X

    override fun onEnable() {
        if (mc.player == null) {
            disable()
            return
        }
        if (AutoWalk.isEnabled) AutoWalk.disable()

        startingDirection = getPlayerMainCardinal(mc.getRenderViewEntity() as? EntityPlayer? ?: mc.player)
        sendTunnel()
    }

    private fun sendTunnel() {
        val current: Array<String> = if (height.value == 2 && width.value == 1) {
            arrayOf("tunnel")
        } else {
            arrayOf("tunnel", height.value.toString(), width.value.toString(), "1000000")
        }

        if (!current.contentEquals(lastCommand)) {
            lastCommand = current
            when (startingDirection) {
                CardinalMain.POS_X -> {
                    mc.player.rotationYaw = -90.0f; mc.player.rotationPitch = 0.0f
                }
                CardinalMain.NEG_X -> {
                    mc.player.rotationYaw = 90.0f; mc.player.rotationPitch = 0.0f
                }
                CardinalMain.POS_Z -> {
                    mc.player.rotationYaw = 0.0f; mc.player.rotationYaw = 0.0f
                }
                CardinalMain.NEG_Z -> {
                    mc.player.rotationYaw = 180.0f; mc.player.rotationYaw = 0.0f
                }
                else -> return
            }
            MessageSendHelper.sendBaritoneCommand(*current)
        }
    }

    override fun onDisable() {
        mc.player?.let {
            BaritoneAPI.getProvider().primaryBaritone.pathingBehavior.cancelEverything()
        }
        lastCommand = arrayOf("")
    }

    override fun getHudInfo(): String? {
        return startingDirection.cardinalName
    }

    @EventHandler
    private val disconnectListener = Listener(EventHook { event: ConnectionEvent.Disconnect ->
        BaritoneAPI.getProvider().primaryBaritone.pathingBehavior.cancelEverything()
    })

    init {
        height.settingListener = Setting.SettingListeners { if (mc.player != null && isEnabled) sendTunnel() }
        width.settingListener = Setting.SettingListeners { if (mc.player != null && isEnabled) sendTunnel() }
        backfill.settingListener = Setting.SettingListeners { if (mc.player != null) BaritoneAPI.getSettings().backfill.value = backfill.value }
    }
}