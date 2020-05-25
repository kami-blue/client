package me.zeroeightsix.kami.module.modules.misc

import baritone.api.BaritoneAPI
import me.zeroeightsix.kami.KamiMod.MODULE_MANAGER
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.movement.AutoWalk
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MathsUtils.CardinalMain
import me.zeroeightsix.kami.util.MathsUtils.getPlayerMainCardinal
import me.zeroeightsix.kami.util.MessageSendHelper

/**
 * @author dominikaaaa
 * Updated by pNoName on 25/05/20
 */
@Module.Info(
        name = "AutoTunnel",
        description = "Automatically tunnels forward, at a given size",
        category = Module.Category.MISC
)
class AutoTunnel : Module() {
    private var height = register(Settings.integerBuilder("Height").withRange(1, 10).withValue(2).build())
    private var width = register(Settings.integerBuilder("Width").withRange(1, 10).withValue(1).build())

    private var lastCommand = arrayOf("")
    private var startingDirection = CardinalMain.POS_X

    init {
        height.settingListener = Setting.SettingListeners { if (mc.player != null && isEnabled) sendTunnel() }
        width.settingListener = Setting.SettingListeners { if (mc.player != null && isEnabled) sendTunnel() }
    }

    override fun onEnable() {
        if (mc.player == null) {
            disable()
            return
        }
        if (MODULE_MANAGER.isModuleEnabled(AutoWalk::class.java)) {
            MODULE_MANAGER.getModuleT(AutoWalk::class.java).disable()
        }

        startingDirection = getPlayerMainCardinal(mc)
        sendTunnel()
    }

    private fun sendTunnel() {
        var current = arrayOf("")
        if (height.value == 2 && width.value == 1) {
            current = arrayOf("tunnel")
        }
        else {
            current = arrayOf("tunnel", height.value.toString(), width.value.toString(), "1000000")
        }

        if (!current.contentEquals(lastCommand)) {
            lastCommand = current
            when (startingDirection) {
                CardinalMain.POS_X -> { mc.player.rotationYaw = -90.0f; mc.player.rotationPitch = 0.0f }
                CardinalMain.NEG_X -> { mc.player.rotationYaw = 90.0f; mc.player.rotationPitch = 0.0f }
                CardinalMain.POS_Z -> { mc.player.rotationYaw = 0.0f; mc.player.rotationYaw = 0.0f }
                CardinalMain.NEG_Z -> { mc.player.rotationYaw = 180.0f; mc.player.rotationYaw = 0.0f }
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
}