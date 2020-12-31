package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.movement.Strafe
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.combat.SurroundUtils
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.VectorUtils
import me.zeroeightsix.kami.util.math.VectorUtils.distanceTo
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.math.VectorUtils.toVec3d
import net.minecraft.util.math.BlockPos
import org.kamiblue.commons.extension.ceilToInt
import org.kamiblue.commons.extension.toRadian
import org.kamiblue.event.listener.listener
import kotlin.math.*

@Module.Info(
        name = "HoleSnap",
        description = "Move you into the hole nearby",
        category = Module.Category.COMBAT
)
object HoleSnap : Module() {
    private val disableStrafe = register(Settings.b("DisableStrafe", true))
    private val range = register(Settings.floatBuilder("Range").withValue(2.5f).withRange(0f, 5f))

    override fun onEnable() {
        if (mc.player == null) disable()
    }

    init {
        listener<SafeTickEvent> {
            if (SurroundUtils.checkHole(mc.player) != SurroundUtils.HoleType.NONE) {
                disable()
                return@listener
            }
            findHole()?.toVec3d()?.let {
                if (disableStrafe.value) Strafe.disable()
                if (mc.player.onGround) {
                    val yawRad = RotationUtils.getRotationTo(mc.player.positionVector, it).x.toDouble().toRadian()
                    val speed = min(0.25, mc.player.positionVector.distanceTo(it) / 4.0)
                    mc.player.motionX = -sin(yawRad) * speed
                    mc.player.motionZ = cos(yawRad) * speed
                }
            }
        }
    }

    private fun findHole(): BlockPos? {
        var closestHole = Pair(69.69, BlockPos.ORIGIN)
        val playerPos = mc.player.positionVector.toBlockPos()
        val ceilRange = (range.value).ceilToInt()
        val posList = VectorUtils.getBlockPositionsInArea(playerPos.add(ceilRange, -1, ceilRange), playerPos.add(-ceilRange, -1, -ceilRange))
        for (posXZ in posList) {
            val dist = mc.player.distanceTo(posXZ)
            if (dist > range.value || dist > closestHole.first) continue
            for (posY in 0..5) {
                val pos = posXZ.add(0, -posY, 0)
                if (!mc.world.isAirBlock(pos.up())) break
                if (SurroundUtils.checkHole(pos) == SurroundUtils.HoleType.NONE) continue
                closestHole = dist to pos
            }
        }
        return if (closestHole.second != BlockPos.ORIGIN) closestHole.second else null
    }
}