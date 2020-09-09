package me.zeroeightsix.kami.module.modules.misc

import baritone.api.BaritoneAPI
import baritone.api.pathing.goals.GoalXZ
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent
import kotlin.math.max
import kotlin.random.Random

/**
 * Created by 086 on 16/12/2017.
 * Updated by dominikaaaa on 21/04/20
 * Updated by Xiaro on 09/09/20
 *
 * TODO: Path finding to stay inside 1 chunk
 * TODO: Render which chunk is selected
 */
@Module.Info(
        name = "AntiAFK",
        category = Module.Category.MISC,
        description = "Prevents being kicked for AFK"
)
class AntiAFK : Module() {
    private val delay = register(Settings.integerBuilder("ActionDelay").withValue(50).withRange(0, 100).build())
    private val variation = register(Settings.integerBuilder("Variation").withValue(25).withRange(0, 50))
    private val squareWalk = register(Settings.b("SquareWalk", true))
    private val radius = register(Settings.integerBuilder("Radius").withMinimum(1).withValue(64).build())
    private val inputTimeout = register(Settings.integerBuilder("InputTimeout(m)").withValue(0).withRange(0, 15).build())

    private var startPos = BlockPos(114514, -696969, 404)
    private var nextActionTick = 0
    private var squareStep = 0
    private var baritoneDisconnectOnArrival = false
    private val inputTimer = TimerUtils.TickTimer(TimerUtils.TimeUnit.MINUTES)

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (!autoReply.value || event.packet !is SPacketChat) return@EventHook
        val text = event.packet.chatComponent.unformattedText
        if (text.contains("whispers: ") && !text.contains(mc.player.name)) {
            sendServerMessage("/r I am currently AFK and using KAMI Blue!")
        }
    })

    @EventHandler
    private val mouseInputListener = Listener(EventHook<InputEvent.MouseInputEvent> { event: InputEvent.MouseInputEvent? ->
        if (inputTimeout.value != 0 && isInputting()) {
            inputTimer.reset()
        }
    })

    @EventHandler
    private val keyInputListener = Listener(EventHook<KeyInputEvent> { event: KeyInputEvent? ->
        if (inputTimeout.value != 0 && isInputting()) {
            inputTimer.reset()
        }
    })

    private fun isInputting(): Boolean {
        return (mc.gameSettings.keyBindAttack.isKeyDown
                || mc.gameSettings.keyBindUseItem.isKeyDown
                || mc.gameSettings.keyBindJump.isKeyDown
                || mc.gameSettings.keyBindSneak.isKeyDown
                || mc.gameSettings.keyBindForward.isKeyDown
                || mc.gameSettings.keyBindBack.isKeyDown
                || mc.gameSettings.keyBindLeft.isKeyDown
                || mc.gameSettings.keyBindRight.isKeyDown)
    }

    override fun getHudInfo(): String? {
        return ((System.currentTimeMillis() - inputTimer.time) / 1000L).toString()
    }

    override fun onEnable() {
        with(BaritoneAPI.getSettings().disconnectOnArrival) {
            baritoneDisconnectOnArrival = value
            value = false
        }
    }

    override fun onDisable() {
        startPos = BlockPos(114514, -696969, 404)
        BaritoneAPI.getSettings().disconnectOnArrival.value = baritoneDisconnectOnArrival
        baritoneCancel()
    }

    override fun onUpdate() {
        if (inputTimeout.value != 0) {
            if (isBaritoneActive) inputTimer.reset()
            if (!inputTimer.tick(inputTimeout.value.toLong(), false)) {
                startPos = BlockPos(114514, -696969, 404)
                return
            }
        }

        if (mc.player.ticksExisted >= nextActionTick) {
            val random = if (variation.value > 0) (0..variation.value).random() else 0
            nextActionTick = mc.player.ticksExisted + max(delay.value, 1) + random

            when ((getAction())) {
                Action.SWING -> mc.player.swingArm(EnumHand.MAIN_HAND)
                Action.JUMP -> mc.player.jump()
                Action.TURN -> mc.player.rotationYaw = Random.nextDouble(-180.0, 180.0).toFloat()
            }

            if (squareWalk.value && !isBaritoneActive) {
                if (startPos == BlockPos(114514, -696969, 404)) startPos = mc.player.position
                when (squareStep) {
                    0 -> baritoneGotoXZ(startPos.x, startPos.z + radius.value)
                    1 -> baritoneGotoXZ(startPos.x + radius.value, startPos.z + radius.value)
                    2 -> baritoneGotoXZ(startPos.x + radius.value, startPos.z)
                    3 -> baritoneGotoXZ(startPos.x, startPos.z)
                }
                squareStep = (squareStep + 1) % 4
            }
        }
    }

    private fun getAction(): Action? {
        if (!swing.value && !jump.value && !turn.value) return null
        val action = Action.values().random()
        return if (action.setting.value) action else getAction()
    }

    enum class Action(val setting: Setting<Boolean>) {
        SWING(swing),
        JUMP(jump),
        TURN(turn)
    }

    private val isBaritoneActive: Boolean
        get() = BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.isActive

    private fun baritoneGotoXZ(x: Int, z: Int) {
        BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(x, z))
    }

    private fun baritoneCancel() {
        if (squareWalk.value && isBaritoneActive) BaritoneAPI.getProvider().primaryBaritone.pathingBehavior.cancelEverything()
    }

    companion object {
        val autoReply: Setting<Boolean> = (Settings.b("AutoReply", true))
        var swing: Setting<Boolean> = (Settings.b("Swing", true))
        var jump: Setting<Boolean> = (Settings.b("Jump", true))
        var turn: Setting<Boolean> = (Settings.b("Turn", true))
    }

    init {
        registerAll(autoReply, swing, jump, turn)

        squareWalk.settingListener = Setting.SettingListeners {
            if (startPos == BlockPos(114514, -696969, 404)) baritoneCancel()
        }
    }
}