package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

internal object ViewLock : Module(
    name = "ViewLock",
    category = Category.PLAYER,
    description = "Locks your camera view"
) {

    private val yaw = setting("Yaw", true)
    private val pitch = setting("Pitch", true)
    private val autoYaw = setting("AutoYaw", true, { yaw.value })
    private val autoPitch = setting("AutoPitch", true, { pitch.value })
    private val disableMouseYaw = setting("DisableMouseYaw", true, { yaw.value && yaw.value })
    private val disableMousePitch = setting("DisableMousePitch", true, { pitch.value && pitch.value })
    private val specificYaw = setting("SpecificYaw", 180.0f, -180.0f..180.0f, 1.0f, { !autoYaw.value && yaw.value })
    private val specificPitch = setting("SpecificPitch", 0.0f, -90.0f..90.0f, 1.0f, { !autoPitch.value && pitch.value })
    private val yawSlice = setting("YawSlice", 8, 2..32, 1, { autoYaw.value && yaw.value })
    private val pitchSlice = setting("PitchSlice", 5, 2..32, 1, { autoPitch.value && pitch.value })

    private var yawSnap = 0
    private var pitchSnap = 0
    private val deltaXQueue = ArrayDeque<Pair<Float, Long>>()
    private val deltaYQueue = ArrayDeque<Pair<Float, Long>>()
    private var pitchSliceAngle = 1.0f
    private var yawSliceAngle = 1.0f

    init {
        onEnable {
            yawSliceAngle = 360.0f / yawSlice.value
            pitchSliceAngle = 180.0f / (pitchSlice.value - 1)
            if (autoYaw.value || autoPitch.value) snapToNext()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeListener
            if (autoYaw.value || autoPitch.value) {
                snapToSlice()
            }
            if (yaw.value && !autoYaw.value) {
                player.rotationYaw = specificYaw.value
            }
            if (pitch.value && !autoPitch.value) {
                player.rotationPitch = specificPitch.value
            }
        }
    }

    fun handleTurn(deltaX: Float, deltaY: Float): Vec2f {
        val yawChange = if (yaw.value && autoYaw.value) handleDelta(deltaX, deltaXQueue, yawSliceAngle) else 0
        val pitchChange = if (pitch.value && autoPitch.value) handleDelta(-deltaY, deltaYQueue, pitchSliceAngle) else 0
        changeDirection(yawChange, pitchChange)

        return Vec2f(
            if (yaw.value && disableMouseYaw.value) 0.0f else deltaX,
            if (pitch.value && disableMousePitch.value) 0.0f else deltaY
        )
    }

    private fun handleDelta(delta: Float, list: ArrayDeque<Pair<Float, Long>>, slice: Float): Int {
        val currentTime = System.currentTimeMillis()
        list.add(Pair(delta * 0.15f, currentTime))

        val sum = list.sumByDouble { it.first.toDouble() }.toFloat()
        return if (abs(sum) > slice) {
            list.clear()
            sign(sum).toInt()
        } else {
            while (list.first().second < currentTime - 500) {
                list.removeFirstOrNull()
            }
            0
        }
    }


    private fun changeDirection(yawChange: Int, pitchChange: Int) {
        yawSnap = Math.floorMod(yawSnap + yawChange, yawSlice.value)
        pitchSnap = (pitchSnap + pitchChange).coerceIn(0, pitchSlice.value - 1)
        snapToSlice()
    }

    private fun snapToNext() {
        mc.player?.let {
            yawSnap = (it.rotationYaw / yawSliceAngle).roundToInt()
            pitchSnap = ((it.rotationPitch + 90) / pitchSliceAngle).roundToInt()
            snapToSlice()
        }
    }

    private fun snapToSlice() {
        mc.player?.let { player ->
            if (yaw.value && autoYaw.value) {
                player.rotationYaw = (yawSnap * yawSliceAngle).coerceIn(0f, 360f)
                player.ridingEntity?.let { it.rotationYaw = player.rotationYaw }
            }
            if (pitch.value && autoPitch.value) {
                player.rotationPitch = (pitchSnap * pitchSliceAngle - 90).coerceIn(-90f, 90f)
            }
        }
    }

    init {
        yawSlice.listeners.add {
            yawSliceAngle = 360.0f / yawSlice.value
            if (isEnabled && autoYaw.value) snapToNext()
        }

        pitchSlice.listeners.add {
            pitchSliceAngle = 180.0f / (pitchSlice.value - 1)
            if (isEnabled && autoPitch.value) snapToNext()
        }

        with({ _: Boolean, it: Boolean -> if (isEnabled && it) snapToNext() }) {
            autoPitch.valueListeners.add(this)
            autoYaw.valueListeners.add(this)
        }
    }
}