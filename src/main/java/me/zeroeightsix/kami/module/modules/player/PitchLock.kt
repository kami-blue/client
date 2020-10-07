package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.util.math.MathHelper
import kotlin.math.roundToInt

@Module.Info(
        name = "PitchLock",
        category = Module.Category.PLAYER,
        description = "Locks your camera pitch"
)
object PitchLock : Module() {
    private val auto = register(Settings.b("Auto", true))
    private val pitch = register(Settings.f("Pitch", 180f))
    private val slice = register(Settings.i("Slice", 8))

    override fun onUpdate(event: SafeTickEvent) {
        if (slice.value == 0) return
        if (auto.value) {
            val angle = 360 / slice.value
            var yaw = mc.player.rotationPitch
            yaw = (yaw / angle).roundToInt() * angle.toFloat()
            mc.player.rotationPitch = yaw
            if (mc.player.isRiding) mc.player.getRidingEntity()!!.rotationPitch = yaw
        } else {
            mc.player.rotationPitch = MathHelper.clamp(pitch.value - 180, -180f, 180f)
        }
    }
}