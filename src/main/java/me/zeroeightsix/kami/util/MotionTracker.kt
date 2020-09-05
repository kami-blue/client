package me.zeroeightsix.kami.util

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.LocalPlayerUpdateEvent
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import java.util.*

class MotionTracker(target: Entity?, var trackLength: Int = 20) {
    var target: Entity? = target
        set(value) {
            if (field != value) {
                motionLog.clear()
                field = value
            }
        }
    private val motionLog = LinkedList<Vec3d>()
    private var skipTicks = 0

    @EventHandler
    private val receiveListener = Listener(EventHook { event: LocalPlayerUpdateEvent ->
        target?.let {
            val motionVec = it.positionVector.subtract(it.prevPosX, it.prevPosY, it.prevPosZ)
            if (motionVec.length() < 0.0001 && skipTicks < 2) { // Only logging every 2 ticks if target is not moving
                skipTicks++
            } else {
                skipTicks = 0
                motionLog.add(motionVec)
            }
            while (motionLog.size > trackLength) {
                motionLog.pollFirst()
            }
        }
    })

    fun calcPositionAhead(ticksAhead: Int): Vec3d? {
        return target?.positionVector?.add(calcAverageMotion().scale(ticksAhead.toDouble()))
    }

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