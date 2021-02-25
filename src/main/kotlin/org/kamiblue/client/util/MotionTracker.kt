package org.kamiblue.client.util

import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.KamiEventBus
import org.kamiblue.client.util.graphics.KamiTessellator
import org.kamiblue.client.util.threads.safeListener
import java.util.*
import kotlin.collections.ArrayDeque

/**
 * Tracking the motion of an Entity tick by tick
 */
class MotionTracker(targetIn: Entity?, private val trackLength: Int = 20) {
    var target: Entity? = targetIn
        set(value) {
            if (value != field) {
                reset()
                field = value
            }
        }
    private val motionLog = ArrayDeque<Vec3d>()
    private var prevMotion = Vec3d(0.0, 0.0, 0.0)
    private var motion = Vec3d(0.0, 0.0, 0.0)
    private val lockObject = Any()

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeListener
            synchronized(lockObject) {
                target?.let { target ->
                    motionLog.add(calcActualMotion(target))
                    while (motionLog.size > trackLength) motionLog.removeFirstOrNull()
                    prevMotion = motion
                    motion = calcAverageMotion()
                }
            }
        }
    }

    /**
     * Calculate the actual motion of given entity
     *
     * @param entity The entity for motion calculation
     * @return Actual motion vector
     */
    private fun calcActualMotion(entity: Entity): Vec3d {
        return entity.positionVector.subtract(entity.prevPosX, entity.prevPosY, entity.prevPosZ)
    }

    /**
     * Calculate the average motion of the target entity in [trackLength]
     *
     * @return Average motion vector
     */
    private fun calcAverageMotion(): Vec3d {
        var sumX = 0.0
        var sumY = 0.0
        var sumZ = 0.0
        synchronized(lockObject) {
            for (motion in motionLog) {
                sumX += motion.x
                sumY += motion.y
                sumZ += motion.z
            }
            return Vec3d(sumX, sumY, sumZ).scale(1.0 / motionLog.size)
        }
    }

    fun getPositionAndBBAhead(ticksAhead: Int, interpolation: Boolean = false): Pair<Vec3d, AxisAlignedBB>? {
        return target?.let { entity ->
            calcPositionAhead(ticksAhead, interpolation)?.let {
                val halfWidth = entity.width / 2.0
                val height = entity.height.toDouble()

                it to AxisAlignedBB(it.x - halfWidth, it.y, it.z - halfWidth, it.x + halfWidth, it.y + height, it.z + halfWidth)
            }
        }
    }

    /**
     * Calculate the predicted position of the target entity based on [calcAverageMotion]
     *
     * @param [ticksAhead] Amount of prediction ahead
     * @param [interpolation] Whether to return interpolated position or not, default value is false (no interpolation)
     * @return Predicted position of the target entity
     */
    fun calcPositionAhead(ticksAhead: Int, interpolation: Boolean = false): Vec3d? {
        return target?.let { target ->
            calcMovedVectorAhead(ticksAhead, interpolation)?.let {
                val partialTicks = if (interpolation) KamiTessellator.pTicks() else 1f
                EntityUtils.getInterpolatedPos(target, partialTicks).add(it)
            }
        }
    }

    /**
     * Calculate the predicted moved vector of the target entity based on [calcAverageMotion]
     *
     * @param [ticksAhead] Amount of prediction ahead
     * @param [interpolation] Whether to return interpolated position or not, default value is false (no interpolation)
     * @return Predicted moved vector of the target entity
     */
    fun calcMovedVectorAhead(ticksAhead: Int, interpolation: Boolean = false): Vec3d? {
        return Wrapper.world?.let { world ->
            target?.let {
                val partialTicks = if (interpolation) KamiTessellator.pTicks() else 1f
                val averageMotion = prevMotion.add(motion.subtract(prevMotion).scale(partialTicks.toDouble()))
                var movedVec = Vec3d(0.0, 0.0, 0.0)
                for (ticks in 0..ticksAhead) {
                    movedVec = if (canMove(world, it.entityBoundingBox, movedVec.add(averageMotion))) { // Attempt to move with full motion
                        movedVec.add(averageMotion)
                    } else if (canMove(world, it.entityBoundingBox, movedVec.add(averageMotion.x, 0.0, averageMotion.z))) { // Attempt to move horizontally
                        movedVec.add(averageMotion.x, 0.0, averageMotion.z)
                    } else break
                }
                movedVec
            }
        }
    }

    private fun canMove(world: World, bbox: AxisAlignedBB, offset: Vec3d): Boolean {
        return !world.collidesWithAnyBlock(bbox.offset(offset))
    }

    /**
     * Reset motion tracker
     */
    fun reset() {
        synchronized(lockObject) {
            motionLog.clear()
            prevMotion = Vec3d(0.0, 0.0, 0.0)
            motion = Vec3d(0.0, 0.0, 0.0)
        }
    }

    init {
        KamiEventBus.subscribe(this)
    }
}