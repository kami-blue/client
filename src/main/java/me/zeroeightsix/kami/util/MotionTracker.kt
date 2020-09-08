package me.zeroeightsix.kami.util

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.LocalPlayerUpdateEvent
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import java.util.*

/**
 * @author Xiaro
 *
 * Tracking the motion of an Entity tick by tick
 *
 * Created by Xiaro on 04/09/20
 */
class MotionTracker(targetIn: Entity?, private val trackLength: Int = 20) {
    var target: Entity? = targetIn
        set(value) {
            if (value != field) {
                reset()
                field = value
            }
        }
    private val motionLog = LinkedList<Vec3d>()
    private var prevMotion = Vec3d(0.0, 0.0, 0.0)
    private var motion = Vec3d(0.0, 0.0, 0.0)

    @EventHandler
    private val onUpdateListener = Listener(EventHook { event: LocalPlayerUpdateEvent ->
        target?.let {
            motionLog.add(it.positionVector.subtract(it.prevPosX, it.prevPosY, it.prevPosZ))
            while (motionLog.size > trackLength) motionLog.pollFirst()
            prevMotion = motion
            motion = calcAverageMotion()
        }
    })

    /**
     * Calculate the average motion of the target entity in [trackLength]
     *
     * @return Average motion vector
     */
    private fun calcAverageMotion(): Vec3d {
        var sumX = 0.0
        var sumY = 0.0
        var sumZ = 0.0
        for (motion in motionLog) {
            sumX += motion.x
            sumY += motion.y
            sumZ += motion.z
        }
        return Vec3d(sumX, sumY, sumZ).scale(1.0 / motionLog.size)
    }

    /**
     * Calculate the predicted position of the target entity based on [calcAverageMotion]
     *
     * @param [ticksAhead] Amount of prediction ahead
     * @param [interpolation] Whether to return interpolated position or not, default value is false (no interpolation)
     * @return Predicted position of the target entity
     */
    fun calcPositionAhead(ticksAhead: Int, interpolation: Boolean = false): Vec3d? {
        return Wrapper.world?.let { world ->
            target?.let {
                val partialTicks = if (interpolation) KamiTessellator.pTicks() else 1f
                val startingPos = EntityUtils.getInterpolatedPos(it, partialTicks)
                val averageMotion = prevMotion.add(motion.subtract(prevMotion).scale(partialTicks.toDouble()))
                var movedTicks = 0
                for (ticks in 0..ticksAhead) {
                    if (world.collidesWithAnyBlock(it.boundingBox.offset(averageMotion.scale(ticks.toDouble())))) break
                    movedTicks = ticks
                }
                startingPos.add(calcAverageMotion().scale(movedTicks.toDouble()))
            }
        }
    }

    /**
     * Reset motion tracker
     */
    fun reset() {
        motionLog.clear()
        prevMotion = Vec3d(0.0, 0.0, 0.0)
        motion = Vec3d(0.0, 0.0, 0.0)
    }

    init {
        KamiMod.EVENT_BUS.subscribe(this)
    }
}