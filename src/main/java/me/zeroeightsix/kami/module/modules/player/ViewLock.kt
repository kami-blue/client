package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import java.util.*
import kotlin.math.*

@Module.Info(
        name = "ViewLock",
        category = Module.Category.PLAYER,
        description = "Locks your camera view"
)
object ViewLock : Module() {

    private val deltaXQueue: Queue<Pair<Int, Long>> = LinkedList<Pair<Int, Long>>(listOf())
    private val deltaYQueue: Queue<Pair<Int, Long>> = LinkedList<Pair<Int, Long>>(listOf())
    private var pitchSliceAngle = 1.0f
    private var yawSliceAngle = 1.0f
    val yaw = register(Settings.b("Yaw", true))
    val pitch = register(Settings.b("Pitch", true))
    val autoYaw = register(Settings.booleanBuilder("AutoYaw").withValue(true).withVisibility { yaw.value })
    val autoPitch = register(Settings.booleanBuilder("AutoPitch").withValue(true).withVisibility { pitch.value })
    private val threshold = register(Settings.integerBuilder("ChangeThresh").withValue(5).withRange(1, 50).withStep(1).withVisibility { autoPitch.value || autoYaw.value })
    val disableMouseYaw = register(Settings.booleanBuilder("DisableMouseYaw").withValue(true).withVisibility { yaw.value })
    val disableMousePitch = register(Settings.booleanBuilder("DisableMousePitch").withValue(true).withVisibility { pitch.value })
    private val specifcYaw = register(Settings.floatBuilder("SpecificYaw").withValue(180.0f).withRange(-180.0f, 180.0f).withStep(1.0f).withVisibility { !yaw.value })
    private val specificPitch = register(Settings.floatBuilder("SpecificPitch").withValue(0.0f).withRange(-90.0f, 90.0f).withStep(1.0f).withVisibility { !pitch.value })
    private val yawSlice = register(Settings.integerBuilder("YawSlice").withValue(8).withRange(2, 32).withStep(1).withVisibility { autoYaw.value })
    private val pitchSlice = register(Settings.integerBuilder("PitchSlice").withValue(5).withRange(2, 32).withStep(1).withVisibility { autoPitch.value })

    private var yawSanp = 0
    private var pitchSnap = 0


    override fun onEnable() {
        yawSliceAngle = 360.0f / yawSlice.value
        pitchSliceAngle = 180.0f / (pitchSlice.value - 1)
        if ((autoYaw.value || autoPitch.value) && mc.player != null)
            snapToNext()
    }

    init {
        listener<SafeTickEvent> {
            if (autoYaw.value || autoPitch.value) {
                snapToSlice()

            } else {
                if (yaw.value) mc.player.rotationYaw = specifcYaw.value
                if (pitch.value) mc.player.rotationPitch = specificPitch.value
            }
        }
        yawSlice.settingListener = Setting.SettingListeners {
            if (isEnabled && autoYaw.value) snapToNext()
            yawSliceAngle = 360.0f / yawSlice.value
        }
        pitchSlice.settingListener = Setting.SettingListeners {
            if (isEnabled && autoPitch.value) snapToNext()
            pitchSliceAngle = 180.0f / (pitchSlice.value - 1)
        }
        autoPitch.settingListener = Setting.SettingListeners { if (isEnabled && autoPitch.value) snapToNext() }
        autoYaw.settingListener = Setting.SettingListeners { if (isEnabled && autoYaw.value) snapToNext() }
    }

    private fun snapToNext() {
        yawSanp = round(mc.player.rotationYaw / yawSliceAngle).toInt()
        pitchSnap = round((mc.player.rotationPitch + 90) / pitchSliceAngle).toInt()
        snapToSlice()
    }

    private fun changeDirection(yawChange: Int, pitchChange: Int) {
        yawSanp = Math.floorMod(yawSanp + yawChange, yawSlice.value)
        pitchSnap = min(max(pitchSnap + pitchChange, 0), pitchSlice.value - 1)
        snapToSlice()
    }

    private fun snapToSlice() {
        if (yaw.value && autoYaw.value) {
            mc.player.rotationYaw = yawSanp * yawSliceAngle
            mc.player.ridingEntity?.let { it.rotationYaw = mc.player.rotationYaw }
        }
        if (pitch.value && autoPitch.value) mc.player.rotationPitch = pitchSnap * pitchSliceAngle - 90
    }


    fun handleDeltaX(deltaX: Int) {
        val currenttime = System.currentTimeMillis()
        deltaXQueue.add(Pair(deltaX, currenttime))
        val sum = deltaXQueue.sumBy { it.first }
        println(deltaXQueue.size)
        if (abs(sum) > threshold.value * 100) {
            deltaXQueue.clear()
            changeDirection(sign(sum.toDouble()).toInt(), 0)
            return
        }
        while (deltaXQueue.peek().second < currenttime - 500)
            deltaXQueue.remove()
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
        while (deltaYQueue.peek().second < currenttime - 500)
            deltaYQueue.remove()
    }
}