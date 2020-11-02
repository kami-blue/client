package me.zeroeightsix.kami.module.modules.movement

import baritone.api.pathing.goals.GoalXZ
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.MathUtils
import me.zeroeightsix.kami.util.math.MathUtils.Cardinal
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.event.InputUpdateEvent

@Module.Info(
        name = "AutoWalk",
        category = Module.Category.MOVEMENT,
        description = "Automatically walks somewhere"
)
object AutoWalk : Module() {
    val mode = setting("Direction", AutoWalkMode.BARITONE)

    enum class AutoWalkMode {
        FORWARD, BACKWARDS, BARITONE
    }

    private var disableBaritone = false
    private const val border = 30000000
    var direction: String? = null
    private var toggleTimer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)

    init {
        listener<InputUpdateEvent> {
            when (mode.value) {
                AutoWalkMode.FORWARD -> {
                    disableBaritone = false
                    it.movementInput.moveForward = 1f
                }
                AutoWalkMode.BACKWARDS -> {
                    disableBaritone = false
                    it.movementInput.moveForward = -1f
                }
                AutoWalkMode.BARITONE -> disableBaritone = true

                else -> {
                    KamiMod.log.error("Mode is irregular. Value: " + mode.value)
                }
            }
        }

        listener<ConnectionEvent.Disconnect> {
            disable()
        }

        listener<SafeTickEvent> {
            if (mode.value == AutoWalkMode.BARITONE && !BaritoneUtils.isCustomGoalActive && toggleTimer.tick(3L, false)) {
                disable()
            }
        }
    }

    override fun onDisable() {
        if (disableBaritone) BaritoneUtils.cancelEverything()
    }

    public override fun onEnable() {
        if (mc.player == null) {
            disable()
            return
        }

        if (mode.value != AutoWalkMode.BARITONE) return

        if (mc.player.isElytraFlying) {
            sendErrorMessage("$chatName Baritone mode isn't currently compatible with Elytra flying! Choose a different mode if you want to use AutoWalk while Elytra flying")
            disable()
            return
        }

        when (MathUtils.getPlayerCardinal(mc.getRenderViewEntity() as? EntityPlayer? ?: mc.player)) {
            Cardinal.POS_Z -> BaritoneUtils.primary?.customGoalProcess!!.setGoalAndPath(GoalXZ(mc.player.posX.toInt(), mc.player.posZ.toInt() + border))
            Cardinal.NEG_X_POS_Z -> BaritoneUtils.primary?.customGoalProcess!!.setGoalAndPath(GoalXZ(mc.player.posX.toInt() - border, mc.player.posZ.toInt() + border))
            Cardinal.NEG_X -> BaritoneUtils.primary?.customGoalProcess!!.setGoalAndPath(GoalXZ(mc.player.posX.toInt() - border, mc.player.posZ.toInt()))
            Cardinal.NEG_X_NEG_Z -> BaritoneUtils.primary?.customGoalProcess!!.setGoalAndPath(GoalXZ(mc.player.posX.toInt() - border, mc.player.posZ.toInt() - border))
            Cardinal.NEG_Z -> BaritoneUtils.primary?.customGoalProcess!!.setGoalAndPath(GoalXZ(mc.player.posX.toInt(), mc.player.posZ.toInt() - border))
            Cardinal.POS_X_NEG_Z -> BaritoneUtils.primary?.customGoalProcess!!.setGoalAndPath(GoalXZ(mc.player.posX.toInt() + border, mc.player.posZ.toInt() - border))
            Cardinal.POS_X -> BaritoneUtils.primary?.customGoalProcess!!.setGoalAndPath(GoalXZ(mc.player.posX.toInt() + border, mc.player.posZ.toInt()))
            Cardinal.POS_X_POS_Z -> BaritoneUtils.primary?.customGoalProcess!!.setGoalAndPath(GoalXZ(mc.player.posX.toInt() + border, mc.player.posZ.toInt() + border))
            else -> {
                sendErrorMessage("Could not determine direction. Disabling...")
                disable()
            }
        }

        direction = MathUtils.getPlayerCardinal(mc.getRenderViewEntity() as? EntityPlayer? ?: mc.player).cardinalName
        toggleTimer.reset()
    }

    override fun getHudInfo(): String {
        return if (BaritoneUtils.primary?.customGoalProcess!!.goal != null && direction != null) {
            direction!!
        } else {
            when (mode.value) {
                AutoWalkMode.BARITONE -> "NONE"
                AutoWalkMode.FORWARD -> "FORWARD"
                AutoWalkMode.BACKWARDS -> "BACKWARDS"

                else -> {
                    "N/A"
                }
            }
        }
    }

    init {
        mode.listeners.add {
            mc.player?.let {
                if (mode.value == AutoWalkMode.BARITONE && mc.player.isElytraFlying) {
                    sendErrorMessage("$chatName Baritone mode isn't currently compatible with Elytra flying! Choose a different mode if you want to use AutoWalk while Elytra flying"); disable()
                }
            }
        }
    }
}