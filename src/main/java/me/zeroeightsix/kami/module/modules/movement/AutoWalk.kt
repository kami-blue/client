package me.zeroeightsix.kami.module.modules.movement

import baritone.api.BaritoneAPI
import baritone.api.pathing.goals.GoalXZ
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.ServerDisconnectedEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MathsUtils
import me.zeroeightsix.kami.util.MathsUtils.Cardinal
import net.minecraftforge.client.event.InputUpdateEvent

/**
 * Created by 086 on 16/12/2017.
 * Greatly modified by Dewy on the 10th of May, 2020.
 */
@Module.Info(
        name = "AutoWalk",
        category = Module.Category.MOVEMENT,
        description = "Automatically walks somewhere"
)
class AutoWalk : Module() {
    @JvmField
    var mode: Setting<AutoWalkMode> = register(Settings.e("Direction", AutoWalkMode.BARITONE))
    private var disableBaritone = false
    private var border = 30000000

    @EventHandler
    private val inputUpdateEventListener = Listener(EventHook { event: InputUpdateEvent ->
        when (mode.value) {
            AutoWalkMode.FORWARD -> {
                disableBaritone = false
                event.movementInput.moveForward = 1f
            }
            AutoWalkMode.BACKWARDS -> {
                disableBaritone = false
                event.movementInput.moveForward = -1f
            }
            AutoWalkMode.BARITONE -> disableBaritone = true
        }
    })

    @EventHandler
    var kickListener = Listener(EventHook { event: ServerDisconnectedEvent? ->
        if (mode.value == AutoWalkMode.BARITONE && isEnabled) {
            disable()
        }
    })

    public override fun onEnable() {
        if (mode.value != AutoWalkMode.BARITONE) return

        if (mc.player == null) { disable(); return }

        when (MathsUtils.getPlayerCardinal(mc)!!) {
            Cardinal.POS_Z -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt(), mc.player.posZ.toInt() + border))
            Cardinal.NEG_X_POS_Z -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt() - border, mc.player.posZ.toInt() + border))
            Cardinal.NEG_X -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt() - border, mc.player.posZ.toInt()))
            Cardinal.NEG_X_NEG_Z -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt() - border, mc.player.posZ.toInt() - border))
            Cardinal.NEG_Z -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt(), mc.player.posZ.toInt() - border))
            Cardinal.POS_X_NEG_Z -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt() + border, mc.player.posZ.toInt() - border))
            Cardinal.POS_X -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt() + border, mc.player.posZ.toInt()))
            Cardinal.POS_X_POS_Z -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt() + border, mc.player.posZ.toInt() + border))
        }

        direction = MathsUtils.getPlayerCardinal(mc)!!.cardinalName
    }

    override fun getHudInfo(): String {
        return if (BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.goal != null) {
            direction!!
        } else {
            when (mode.value) {
                AutoWalkMode.BARITONE -> "NONE"
                AutoWalkMode.FORWARD -> "FORWARD"
                AutoWalkMode.BACKWARDS -> "BACKWARDS"
            }
        }
    }

    public override fun onDisable() {
        if (disableBaritone) {
            BaritoneAPI.getProvider().primaryBaritone.pathingBehavior.cancelEverything()
        }
    }

    enum class AutoWalkMode {
        FORWARD, BACKWARDS, BARITONE
    }

    companion object {
        @JvmField
        var direction: String? = null
    }
}