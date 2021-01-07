package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.BaritoneCommandEvent
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.movement.AutoWalk
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import kotlin.math.round

object AutoTunnel : Module(
    name = "AutoTunnel",
    description = "Automatically tunnels forward, at a given size",
    category = Category.MISC
) {
    private val backFill = setting("BackFill", false)
    private val height = setting("Height", 2, 1..10, 1)
    private val width = setting("Width", 1, 1..10, 1)
    private val disableOnDisconnect = setting("DisableOnDisconnect", true)

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
        safeListener<TickEvent.ClientTickEvent> {
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

    override fun getHudInfo(): String {
        return lastDirection.name2.capitalize()
    }

    init {
        with({ if (mc.player != null && isEnabled) sendTunnel() }) {
            height.listeners.add(this)
            width.listeners.add(this)
        }

        backFill.listeners.add { BaritoneUtils.settings?.backfill?.value = backFill.value }
    }
}