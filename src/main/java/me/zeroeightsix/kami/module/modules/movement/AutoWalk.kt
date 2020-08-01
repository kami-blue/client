package me.zeroeightsix.kami.module.modules.movement

import baritone.api.BaritoneAPI
import baritone.api.pathing.goals.GoalXZ
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.ServerDisconnectedEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.MathsUtils
import me.zeroeightsix.kami.util.MathsUtils.Cardinal
import me.zeroeightsix.kami.util.MessageSendHelper.sendErrorMessage
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

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

    init { mode.settingListener = Setting.SettingListeners { mc.player?.let { if (mode.value == AutoWalkMode.BARITONE && mc.player.isElytraFlying) { sendErrorMessage("$chatName Baritone mode isn't currently compatible with Elytra flying! Choose a different mode if you want to use AutoWalk while Elytra flying"); closeSettings(); disable() } } } }

    @EventHandler
    private val inputUpdateEventListener = Listener(EventHook { event: InputUpdateEvent ->
        if (InventoryUtils.inProgress || !isEnabled) return@EventHook
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
    private val kickListener = Listener(EventHook { event: ServerDisconnectedEvent ->
        if (mode.value == AutoWalkMode.BARITONE && isEnabled) {
            disable()
        }
    })

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

        when (MathsUtils.getPlayerCardinal(mc)) {
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

        direction = MathsUtils.getPlayerCardinal(mc).cardinalName
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

    public override fun onDisable() {
        disableBaritone()
    }

    @EventHandler
    private val clientDisconnect = Listener(EventHook { event: FMLNetworkEvent.ClientDisconnectionFromServerEvent ->
        disableBaritone()
    })

    @EventHandler
    private val serverDisconnect = Listener(EventHook { event: FMLNetworkEvent.ServerDisconnectionFromClientEvent ->
        disableBaritone()
    })

    private fun disableBaritone() {
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