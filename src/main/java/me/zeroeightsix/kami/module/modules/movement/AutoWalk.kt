package me.zeroeightsix.kami.module.modules.movement

import baritone.api.BaritoneAPI
import baritone.api.pathing.goals.GoalXZ
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.math.MathUtils
import me.zeroeightsix.kami.util.math.MathUtils.Cardinal
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage
import net.minecraftforge.client.event.InputUpdateEvent

@Module.Info(
        name = "AutoWalk",
        category = Module.Category.MOVEMENT,
        description = "Automatically walks somewhere"
)
object AutoWalk : Module() {
    val mode = register(Settings.e<AutoWalkMode>("Direction", AutoWalkMode.BARITONE))

    enum class AutoWalkMode {
        FORWARD, BACKWARDS, BARITONE
    }

    private var disableBaritone = false
    private const val border = 30000000
    var direction: String? = null

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

            else -> {
                KamiMod.log.error("Mode is irregular. Value: " + mode.value)
            }
        }
    })

    @EventHandler
    private val disconnectListener = Listener(EventHook { event: ConnectionEvent.Disconnect ->
        disable()
    })

    override fun onDisable() {
        if (disableBaritone) BaritoneAPI.getProvider().primaryBaritone.pathingBehavior.cancelEverything()
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

        when (MathUtils.getPlayerCardinal(mc)) {
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

        direction = MathUtils.getPlayerCardinal(mc).cardinalName
    }

    override fun getHudInfo(): String {
        return if (BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.goal != null && direction != null) {
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
        mode.settingListener = Setting.SettingListeners {
            mc.player?.let {
                if (mode.value == AutoWalkMode.BARITONE && mc.player.isElytraFlying) {
                    sendErrorMessage("$chatName Baritone mode isn't currently compatible with Elytra flying! Choose a different mode if you want to use AutoWalk while Elytra flying"); disable()
                }
            }
        }
    }
}