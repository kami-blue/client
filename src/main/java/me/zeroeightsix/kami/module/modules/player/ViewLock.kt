package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

@Module.Info(
        name = "ViewLock",
        category = Module.Category.PLAYER,
        description = "Locks your camera view"
)
object ViewLock : Module() {

    private val yaw = register(Settings.b("Yaw", true))
    private val pitch = register(Settings.b("Pitch", true))
    private val autoYaw = register(Settings.booleanBuilder("AutoYaw").withValue(true).withVisibility { yaw.value })
    private val autoPitch = register(Settings.booleanBuilder("AutoPitch").withValue(true).withVisibility { pitch.value })
    private val disableMouseYaw = register(Settings.booleanBuilder("DisableMouseYaw").withValue(true).withVisibility { yaw.value && yaw.value })
    private val disableMousePitch = register(Settings.booleanBuilder("DisableMousePitch").withValue(true).withVisibility { pitch.value && pitch.value })
    private val specificYaw = register(Settings.floatBuilder("SpecificYaw").withValue(180.0f).withRange(-180.0f, 180.0f).withStep(1.0f).withVisibility { !autoYaw.value && yaw.value })
    private val specificPitch = register(Settings.floatBuilder("SpecificPitch").withValue(0.0f).withRange(-90.0f, 90.0f).withStep(1.0f).withVisibility { !autoPitch.value && pitch.value })
    private val yawSlice = register(Settings.integerBuilder("YawSlice").withValue(8).withRange(2, 32).withStep(1).withVisibility { autoYaw.value && yaw.value })
    private val pitchSlice = register(Settings.integerBuilder("PitchSlice").withValue(5).withRange(2, 32).withStep(1).withVisibility { autoPitch.value && pitch.value })

    private var yawSnap = 0
    private var pitchSnap = 0
    private val deltaXQueue = LinkedList<Pair<Float, Long>>()
    private val deltaYQueue = LinkedList<Pair<Float, Long>>()
    private var pitchSliceAngle = 1.0f
    private var yawSliceAngle = 1.0f

    override fun onEnable() {
        yawSliceAngle = 360.0f / yawSlice.value
        pitchSliceAngle = 180.0f / (pitchSlice.value - 1)
        if (autoYaw.value || autoPitch.value) snapToNext()
    }

    init {
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

    private fun handleDelta(delta: Float, list: LinkedList<Pair<Float, Long>>, slice: Float): Int {
        val currentTime = System.currentTimeMillis()
        list.add(Pair(delta * 0.15f, currentTime))

        val sum = list.sumByDouble { it.first.toDouble() }.toFloat()
        return if (abs(sum) > slice) {
            list.clear()
            sign(sum).toInt()
        } else {
            while (list.peek().second < currentTime - 500) {
                list.remove()
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