package me.zeroeightsix.kami.module.modules.misc

import baritone.api.pathing.goals.GoalXZ
import me.zeroeightsix.kami.event.events.BaritoneSettingsInitEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.text.MessageDetection
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.kamiblue.event.listener.listener
import kotlin.random.Random

/**
 * TODO: Path finding to stay inside 1 chunk
 * TODO: Render which chunk is selected
 */
@Module.Info(
    name = "AntiAFK",
    category = Module.Category.MISC,
    description = "Prevents being kicked for AFK"
)
object AntiAFK : Module() {
    private val delay = register(Settings.integerBuilder("ActionDelay").withValue(50).withRange(5, 100).withStep(5))
    private val variation = register(Settings.integerBuilder("Variation").withValue(25).withRange(0, 50))
    private val autoReply = register(Settings.b("AutoReply", true))
    private val swing = register(Settings.b("Swing", true))
    private val jump = register(Settings.b("Jump", true))
    private val turn = register(Settings.b("Turn", true))
    private val walk = register(Settings.b("Walk", true))
    private val radius = register(Settings.integerBuilder("Radius").withValue(64).withRange(1, 128))
    private val inputTimeout = register(Settings.integerBuilder("InputTimeout(m)").withValue(0).withRange(0, 15))

    private var startPos: BlockPos? = null
    private var squareStep = 0
    private var baritoneDisconnectOnArrival = false
    private val inputTimer = TickTimer(TimeUnit.MINUTES)
    private val actionTimer = TickTimer(TimeUnit.TICKS)
    private var nextActionDelay = 0

    override fun getHudInfo(): String? {
        return if (inputTimeout.value == 0) null
        else ((System.currentTimeMillis() - inputTimer.time) / 1000L).toString()
    }

    override fun onDisable() {
        startPos = null
        BaritoneUtils.settings?.disconnectOnArrival?.value = baritoneDisconnectOnArrival
        BaritoneUtils.cancelEverything()
    }

    override fun onEnable() {
        inputTimer.reset()
        baritoneDisconnectOnArrival()
    }

    init {
        listener<BaritoneSettingsInitEvent> {
            baritoneDisconnectOnArrival()
        }
    }

    private fun baritoneDisconnectOnArrival() {
        BaritoneUtils.settings?.disconnectOnArrival?.let {
            baritoneDisconnectOnArrival = it.value
            it.value = false
        }
    }

    init {
        listener<PacketEvent.Receive> {
            if (!autoReply.value || it.packet !is SPacketChat) return@listener
            if (MessageDetection.Direct.RECEIVE detect it.packet.chatComponent.unformattedText) {
                sendServerMessage("/r I am currently AFK and using KAMI Blue!")
            }
        }

        listener<InputEvent.MouseInputEvent> {
            if (inputTimeout.value != 0 && isInputting()) {
                inputTimer.reset()
            }
        }

        listener<InputEvent.KeyInputEvent> {
            if (inputTimeout.value != 0 && isInputting()) {
                inputTimer.reset()
            }
        }
    }

    private fun isInputting() =
        mc.gameSettings.keyBindAttack.isKeyDown
            || mc.gameSettings.keyBindUseItem.isKeyDown
            || mc.gameSettings.keyBindJump.isKeyDown
            || mc.gameSettings.keyBindSneak.isKeyDown
            || mc.gameSettings.keyBindForward.isKeyDown
            || mc.gameSettings.keyBindBack.isKeyDown
            || mc.gameSettings.keyBindLeft.isKeyDown
            || mc.gameSettings.keyBindRight.isKeyDown

    init {
        listener<SafeTickEvent> {
            if (inputTimeout.value != 0) {
                if (BaritoneUtils.isActive) {
                    inputTimer.reset()
                } else if (!inputTimer.tick(inputTimeout.value.toLong(), false)) {
                    startPos = null
                    return@listener
                }
            }

            if (actionTimer.tick(nextActionDelay.toLong())) {
                val random = if (variation.value > 0) (0..variation.value).random() else 0
                nextActionDelay = delay.value + random

                when ((getAction())) {
                    Action.SWING -> mc.player.swingArm(EnumHand.MAIN_HAND)
                    Action.JUMP -> mc.player.jump()
                    Action.TURN -> mc.player.rotationYaw = Random.nextDouble(-180.0, 180.0).toFloat()
                }

                if (walk.value && !BaritoneUtils.isActive) {
                    squareWalk()
                }
            }
        }
    }

    private fun getAction(): Action? {
        if (!swing.value && !jump.value && !turn.value) return null
        val action = Action.values().random()
        return if (action.setting.value) action else getAction()
    }

    private fun squareWalk() {
        if (startPos == null) startPos = mc.player.position

        startPos?.let {
            when (squareStep) {
                0 -> baritoneGotoXZ(it.x, it.z + radius.value)
                1 -> baritoneGotoXZ(it.x + radius.value, it.z + radius.value)
                2 -> baritoneGotoXZ(it.x + radius.value, it.z)
                3 -> baritoneGotoXZ(it.x, it.z)
            }
        }

        squareStep = (squareStep + 1) % 4
    }

    private fun baritoneGotoXZ(x: Int, z: Int) {
        BaritoneUtils.primary?.customGoalProcess?.setGoalAndPath(GoalXZ(x, z))
    }

    private enum class Action(val setting: Setting<Boolean>) {
        SWING(swing),
        JUMP(jump),
        TURN(turn)
    }

    init {
        walk.settingListener = Setting.SettingListeners {
            BaritoneUtils.cancelEverything()
        }
    }
}