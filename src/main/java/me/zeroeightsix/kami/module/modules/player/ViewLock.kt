package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import java.util.*
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sign

@Module.Info(
        name = "ViewLock",
        category = Module.Category.PLAYER,
        description = "Locks your camera view"
)
object ViewLock : Module() {

    val yaw = register(Settings.b("Yaw", true))
    val pitch = register(Settings.b("Pitch", true))
    val autoYaw = register(Settings.booleanBuilder("AutoYaw").withValue(true).withVisibility { yaw.value })
    val autoPitch = register(Settings.booleanBuilder("AutoPitch").withValue(true).withVisibility { pitch.value })
    private val threshold = register(Settings.integerBuilder("ChangeThresh").withValue(5).withRange(1, 50).withStep(1).withVisibility { autoPitch.value || autoYaw.value })
    val disableMouseYaw = register(Settings.booleanBuilder("DisableMouseYaw").withValue(true).withVisibility { yaw.value && yaw.value })
    val disableMousePitch = register(Settings.booleanBuilder("DisableMousePitch").withValue(true).withVisibility { pitch.value && pitch.value })
    private val specificYaw = register(Settings.floatBuilder("SpecificYaw").withValue(180.0f).withRange(-180.0f, 180.0f).withStep(1.0f).withVisibility { !autoYaw.value && yaw.value })
    private val specificPitch = register(Settings.floatBuilder("SpecificPitch").withValue(0.0f).withRange(-90.0f, 90.0f).withStep(1.0f).withVisibility { !autoPitch.value && pitch.value })
    private val yawSlice = register(Settings.integerBuilder("YawSlice").withValue(8).withRange(2, 32).withStep(1).withVisibility { autoYaw.value && yaw.value })
    private val pitchSlice = register(Settings.integerBuilder("PitchSlice").withValue(5).withRange(2, 32).withStep(1).withVisibility { autoPitch.value && pitch.value })

    private var yawSnap = 0
    private var pitchSnap = 0
    private val deltaXQueue = LinkedList<Pair<Int, Long>>()
    private val deltaYQueue = LinkedList<Pair<Int, Long>>()
    private var pitchSliceAngle = 1.0f
    private var yawSliceAngle = 1.0f

    override fun onEnable() {
        yawSliceAngle = 360.0f / yawSlice.value
        pitchSliceAngle = 180.0f / (pitchSlice.value - 1)
        if ((autoYaw.value || autoPitch.value) && mc.player != null) snapToNext()
    }

    init {
        listener<SafeTickEvent> {
            if (autoYaw.value || autoPitch.value) {
                snapToSlice()
            }
            if (yaw.value && !autoYaw.value) {
                mc.player.rotationYaw = specificYaw.value
            }
            if (pitch.value && !autoPitch.value) {
                mc.player.rotationPitch = specificPitch.value
            }
        }
    }

    private fun snapToNext() {
        yawSnap = round(mc.player.rotationYaw / yawSliceAngle).toInt()
        pitchSnap = round((mc.player.rotationPitch + 90) / pitchSliceAngle).toInt()
        snapToSlice()
    }

    private fun changeDirection(yawChange: Int, pitchChange: Int) {
        yawSnap = Math.floorMod(yawSnap + yawChange, yawSlice.value)
        pitchSnap = (pitchSnap + pitchChange).coerceIn(0, pitchSlice.value - 1)
        snapToSlice()
    }

    private fun snapToSlice() {
        if (yaw.value && autoYaw.value) {
            mc.player.rotationYaw = (yawSnap * yawSliceAngle).coerceIn(0f, 360f)
            mc.player.ridingEntity?.let { it.rotationYaw = mc.player.rotationYaw }
        }
        if (pitch.value && autoPitch.value) {
            mc.player.rotationPitch = (pitchSnap * pitchSliceAngle - 90).coerceIn(-90f, 90f)
        }
    }


    fun handleDeltaX(deltaX: Int) {
        val currenttime = System.currentTimeMillis()
        deltaXQueue.add(Pair(deltaX, currenttime))
        val sum = deltaXQueue.sumBy { it.first }
        if (abs(sum) > threshold.value * 100) {
            deltaXQueue.clear()
            changeDirection(sign(sum.toDouble()).toInt(), 0)
            return
        }
        while (deltaXQueue.peek().second < currenttime - 500) {
            deltaXQueue.remove()
        }
    }

    fun handleDeltaY(deltaY: Int) {
        val currenttime = System.currentTimeMillis()
        deltaYQueue.add(Pair(deltaY, currenttime))
        val sum = deltaYQueue.sumBy { it.first }
        if (abs(sum) > threshold.value * 100) {
            deltaYQueue.clear()
            changeDirection(0, -sign(sum.toDouble()).toInt())
            return
        }
        while (deltaYQueue.peek().second < currenttime - 500) {
            deltaYQueue.remove()
        }
    }

    init {
        yawSlice.settingListener = Setting.SettingListeners {
            yawSliceAngle = 360.0f / yawSlice.value
            if (isEnabled && autoYaw.value) snapToNext()
        }

        pitchSlice.settingListener = Setting.SettingListeners {
            pitchSliceAngle = 180.0f / (pitchSlice.value - 1)
            if (isEnabled && autoPitch.value) snapToNext()
        }

        autoPitch.settingListener = Setting.SettingListeners { if (isEnabled && autoPitch.value) snapToNext() }
        autoYaw.settingListener = Setting.SettingListeners { if (isEnabled && autoYaw.value) snapToNext() }
    }
}