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
class MotionTracker(targetIn: Entity?, var trackLength: Int = 40) {
    var target: Entity? = targetIn
        set(value) {
            if (field != value) {
                motionLog.clear()
                field = value
            }
        }
    private val motionLog = LinkedList<Vec3d>()

    @EventHandler
    private val receiveListener = Listener(EventHook { event: LocalPlayerUpdateEvent ->
        target?.let {
            motionLog.add(it.positionVector.subtract(it.prevPosX, it.prevPosY, it.prevPosZ))
            while (motionLog.size > trackLength) motionLog.pollFirst()
        }
    })

    /**
     * Calculate the predicted position of the target entity based on [calcAverageMotion]
     *
     * @param [ticksAhead] Amount of prediction ahead
     * @return Predicted position of the target entity
     */
    fun calcPositionAhead(ticksAhead: Int): Vec3d? {
        return target?.let { EntityUtils.getInterpolatedPos(it, KamiTessellator.pTicks()).add(calcAverageMotion().scale(ticksAhead.toDouble())) }
    }

    /**
     * Calculate the average motion of the target entity in [trackLength]
     *
     * @return Average motion vector
     */
    fun calcAverageMotion(): Vec3d {
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

    init {
        KamiMod.EVENT_BUS.subscribe(this)
    }
}