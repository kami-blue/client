package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.BaritoneCommandEvent
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.movement.AutoWalk
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.util.EnumFacing
import org.kamiblue.event.listener.listener
import kotlin.math.round

@Module.Info(
        name = "AutoTunnel",
        description = "Automatically tunnels forward, at a given size",
        category = Module.Category.MISC
)
object AutoTunnel : Module() {
    private val backFill = register(Settings.b("BackFill", false))
    private val height = register(Settings.integerBuilder("Height").withValue(2).withRange(2, 10).withStep(1))
    private val width = register(Settings.integerBuilder("Width").withValue(1).withRange(1, 10).withStep(1))
    private val disableOnDisconnect = register(Settings.b("DisableOnDisconnect", true))

    private var lastDirection = EnumFacing.NORTH

    override fun isActive(): Boolean {
        return isEnabled
                && (BaritoneUtils.isPathing
                || BaritoneUtils.primary?.builderProcess?.isActive == true)
    }

    override fun onDisable() {
        if (mc.player != null) BaritoneUtils.cancelEverything()
    }

    init {
        listener<SafeTickEvent> {
            if (!isActive()) sendTunnel()
        }

        listener<BaritoneCommandEvent> {
            if (it.command.contains("cancel")) {
                disable()
            }
        }

        listener<ConnectionEvent.Disconnect> {
            if (disableOnDisconnect.value) disable()
        }
    }

    private fun sendTunnel() {
        mc.player?.let {
            if (AutoWalk.isEnabled) AutoWalk.disable()
            BaritoneUtils.cancelEverything()
            val normalizedYaw = RotationUtils.normalizeAngle(it.rotationYaw)
            it.rotationYaw = round(normalizedYaw / 90.0f) * 90.0f
            it.rotationPitch = 0.0f
            lastDirection = it.horizontalFacing

            MessageSendHelper.sendBaritoneCommand("tunnel", height.value.toString(), width.value.toString(), "100")
        }
    }

    override fun getHudInfo(): String? {
        return lastDirection.name2.capitalize()
    }

    init {
        with(Setting.SettingListeners { if (isEnabled) sendTunnel() }) {
            height.settingListener = this
            width.settingListener = this
        }

        backFill.settingListener = Setting.SettingListeners { BaritoneUtils.settings?.backfill?.value = backFill.value }
    }
}