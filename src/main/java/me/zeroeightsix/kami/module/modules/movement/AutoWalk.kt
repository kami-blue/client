package me.zeroeightsix.kami.module.modules.movement

import baritone.api.BaritoneAPI
import baritone.api.pathing.calc.IPath
import baritone.api.pathing.goals.Goal
import baritone.api.pathing.goals.GoalXZ
import baritone.api.pathing.movement.IMovement
import baritone.api.utils.BetterBlockPos
import baritone.utils.PathRenderer
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.astar.Astarpathfinder
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.MathUtils
import me.zeroeightsix.kami.util.math.MathUtils.Cardinal
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.event.InputUpdateEvent
import java.awt.Color

@Module.Info(
        name = "AutoWalk",
        category = Module.Category.MOVEMENT,
        description = "Automatically walks somewhere"
)
object AutoWalk : Module() {
    val mode = register(Settings.e<AutoWalkMode>("Direction", AutoWalkMode.PATHFIND))
    val flydelay = register(Settings.integerBuilder("SableFlightDelay").withRange(0, 400).withValue(0).withVisibility { mode.value == AutoWalkMode.PATHFIND }.build())
    private val drawPath = register(Settings.booleanBuilder("DrawPath").withValue(false).withVisibility { mode.value == AutoWalkMode.PATHFIND }.build())

    enum class AutoWalkMode {
        FORWARD, BACKWARDS, PATHFIND
    }

    private var disableBaritone = false
    private const val border = 30000000
    var direction: String? = null

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
                AutoWalkMode.PATHFIND -> disableBaritone = true

                else -> {
                    KamiMod.log.error("Mode is irregular. Value: " + mode.value)
                }
            }
        }

        listener<ConnectionEvent.Disconnect> {
            disable()
        }

        listener<RenderWorldEvent> {
            if (drawPath.value && mode.value.equals(AutoWalkMode.PATHFIND) && ElytraFlight.isEnabled && Astarpathfinder.path != null) {
                val path: MutableList<BetterBlockPos> = mutableListOf()
                for (i in 0 until Astarpathfinder.path.size) {
                    path.add(BetterBlockPos(Astarpathfinder.path[i].pos.x, mc.player.posY.toInt() - 1, Astarpathfinder.path[i].pos.z))
                }
                PathRenderer.drawPath(object : IPath {
                    override fun positions(): MutableList<BetterBlockPos> { return path }
                    override fun movements(): MutableList<IMovement>? { return null }
                    override fun getGoal(): Goal? { return null }
                    override fun getNumNodesConsidered(): Int { return 0 }
                }, 0, Color(1f, 1f, 0f), false, 0, 0)
            }
        }
    }

    override fun onDisable() {
        if (disableBaritone) BaritoneAPI.getProvider().primaryBaritone.pathingBehavior.cancelEverything()
        ElytraFlight.updatePathfindState()
    }

    public override fun onEnable() {
        if (mc.player == null) {
            disable()
            return
        }

        if (mode.value != AutoWalkMode.PATHFIND) return

        if (ElytraFlight.isEnabled) {
            disableBaritone = true
            onDisable()
            return
        }

        when (MathUtils.getPlayerCardinal(mc.getRenderViewEntity() as? EntityPlayer? ?: mc.player)) {
            Cardinal.POS_Z -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt(), mc.player.posZ.toInt() + border))
            Cardinal.NEG_X_POS_Z -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt() - border, mc.player.posZ.toInt() + border))
            Cardinal.NEG_X -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt() - border, mc.player.posZ.toInt()))
            Cardinal.NEG_X_NEG_Z -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt() - border, mc.player.posZ.toInt() - border))
            Cardinal.NEG_Z -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt(), mc.player.posZ.toInt() - border))
            Cardinal.POS_X_NEG_Z -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt() + border, mc.player.posZ.toInt() - border))
            Cardinal.POS_X -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt() + border, mc.player.posZ.toInt()))
            Cardinal.POS_X_POS_Z -> BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(mc.player.posX.toInt() + border, mc.player.posZ.toInt() + border))
            else -> {
                sendErrorMessage("Could not determine direction. Disabling...")
                disable()
            }
        }

        direction = MathUtils.getPlayerCardinal(mc.getRenderViewEntity() as? EntityPlayer? ?: mc.player).cardinalName
    }

    override fun getHudInfo(): String {
        return if (BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.goal != null && direction != null) {
            direction!!
        } else {
            when (mode.value) {
                AutoWalkMode.PATHFIND -> "NONE"
                AutoWalkMode.FORWARD -> "FORWARD"
                AutoWalkMode.BACKWARDS -> "BACKWARDS"

                else -> {
                    "N/A"
                }
            }
        }
    }


}