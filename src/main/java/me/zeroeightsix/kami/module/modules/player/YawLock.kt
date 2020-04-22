package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.util.math.MathHelper
import kotlin.math.roundToInt

/**
 * Created by 086 on 16/12/2017.
 */
@Module.Info(
        name = "YawLock",
        category = Module.Category.PLAYER,
        description = "Locks your camera yaw"
)
class YawLock : Module() {
    private val auto = register(Settings.b("Auto", true))
    private val yaw = register(Settings.f("Yaw", 180f))
    private val slice = register(Settings.i("Slice", 8))
    override fun onUpdate() {
        if (slice.value == 0) return
        if (auto.value) {
            val angle = 360 / slice.value
            var yaw = mc.player.rotationYaw
            yaw = (yaw / angle).roundToInt() * angle.toFloat()
            mc.player.rotationYaw = yaw
            if (mc.player.isRiding) mc.player.getRidingEntity()!!.rotationYaw = yaw
        } else {
            mc.player.rotationYaw = MathHelper.clamp(yaw.value - 180, -180f, 180f)
        }
    }
}