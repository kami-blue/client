package me.zeroeightsix.kami.module.modules.movement

import baritone.api.pathing.goals.GoalXZ
import me.zeroeightsix.kami.event.events.BaritoneCommandEvent
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.player.LagNotifier
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.math.Direction
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.util.MovementInputFromOptions
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.extension.floorToInt
import org.kamiblue.event.listener.listener

@Module.Info(
        name = "AutoWalk",
        category = Module.Category.MOVEMENT,
        description = "Automatically walks somewhere"
)
object AutoWalk : Module() {
    val mode = register(Settings.e<AutoWalkMode>("Direction", AutoWalkMode.BARITONE))
    private val disableOnDisconnect = register(Settings.b("DisableOnDisconnect", true))

    enum class AutoWalkMode {
        FORWARD, BACKWARDS, BARITONE
    }

    private const val border = 30000000
    private val messageTimer = TickTimer(TimeUnit.SECONDS)
    var direction = Direction.NORTH; private set

    override fun isActive(): Boolean {
        return isEnabled && (mode.value != AutoWalkMode.BARITONE || BaritoneUtils.isActive)
    }

    override fun getHudInfo(): String {
        return if (BaritoneUtils.isActive) {
            direction.displayName
        } else {
            mode.value.name
        }
    }

    override fun onDisable() {
        if (mc.player != null && mode.value == AutoWalkMode.BARITONE) BaritoneUtils.cancelEverything()
    }

    init {
        listener<BaritoneCommandEvent> {
            if (it.command.contains("cancel")) {
                disable()
            }
        }

        listener<ConnectionEvent.Disconnect> {
            if (disableOnDisconnect.value) disable()
        }

        listener<InputUpdateEvent>(6969) {
            if (LagNotifier.paused && LagNotifier.pauseAutoWalk.value) return@listener

            if (it.movementInput !is MovementInputFromOptions) return@listener

            when (mode.value) {
                AutoWalkMode.FORWARD -> {
                    it.movementInput.moveForward = 1.0f
                }
                AutoWalkMode.BACKWARDS -> {
                    it.movementInput.moveForward = -1.0f
                }
                else -> {
                    // this is fine, Java meme
                }
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (mode.value == AutoWalkMode.BARITONE && !checkBaritoneElytra() && !isActive()) {
                startPathing()
            }
        }
    }

    private fun startPathing() {
        mc.player?.let {
            if (!mc.world.isChunkGeneratedAt(it.chunkCoordX, it.chunkCoordZ)) return

            direction = Direction.fromEntity(it)
            val x = it.posX.floorToInt() + direction.directionVec.x * border
            val z = it.posZ.floorToInt() + direction.directionVec.z * border

            BaritoneUtils.cancelEverything()
            BaritoneUtils.primary?.customGoalProcess?.setGoalAndPath(GoalXZ(x, z))
        }
    }

    private fun checkBaritoneElytra() = mc.player?.let {
        if (it.isElytraFlying && messageTimer.tick(10L)) {
            MessageSendHelper.sendErrorMessage("$chatName Baritone mode isn't currently compatible with Elytra flying!" +
                    " Choose a different mode if you want to use AutoWalk while Elytra flying")
        }
        it.isElytraFlying
    } ?: true

    init {
        mode.settingListener = Setting.SettingListeners {
            if (mc.player == null || isDisabled) return@SettingListeners
            if (mode.value == AutoWalkMode.BARITONE) {
                if (!checkBaritoneElytra()) startPathing()
            } else {
                BaritoneUtils.cancelEverything()
            }
        }
    }
}
