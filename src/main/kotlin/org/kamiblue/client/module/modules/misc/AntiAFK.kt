package org.kamiblue.client.module.modules.misc

import baritone.api.pathing.goals.GoalXZ
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.BaritoneSettingsInitEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.setting.settings.impl.primitive.BooleanSetting
import org.kamiblue.client.util.BaritoneUtils
import org.kamiblue.client.util.MovementUtils.realSpeed
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.text.MessageDetection
import org.kamiblue.client.util.text.MessageSendHelper.sendServerMessage
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener
import kotlin.random.Random

/**
 * TODO: Path finding to stay inside 1 chunk
 * TODO: Render which chunk is selected
 */
internal object AntiAFK : Module(
    name = "AntiAFK",
    category = Category.MISC,
    description = "Prevents being kicked for AFK"
) {
    private val delay by setting("Action Delay", 50, 5..100, 5)
    private val variation by setting("Variation", 25, 0..50, 5)
    private val autoReply by setting("Auto Reply", true)
    private val replyMessage by setting("Reply Message", "I am AFK with KAMI Blue!", { autoReply })
    private val swing = setting("Swing", true)
    private val jump = setting("Jump", true)
    private val turn = setting("Turn", true)
    private val walk = setting("Walk", true)
    private val radius by setting("Radius", 64, 8..128, 8, fineStep = 1)
    private val inputTimeout by setting("Idle Timeout", 0, 0..15, 1, description = "Starts AntiAFK after being idle for longer than specific minutes, 0 to disable")
    private val allowBreak by setting("Allow Breaking Blocks", false, { walk.value })

    private var startPos: BlockPos? = null
    private var squareStep = 0
    private var baritoneAllowBreak = false
    private var baritoneDisconnectOnArrival = false
    private val inputTimer = TickTimer(TimeUnit.MINUTES)
    private val actionTimer = TickTimer(TimeUnit.TICKS)
    private var nextActionDelay = 0

    override fun getHudInfo(): String {
        return if (inputTimeout == 0) ""
        else ((System.currentTimeMillis() - inputTimer.time) / 1000L).toString()
    }

    init {
        onEnable {
            baritoneAllowBreak = BaritoneUtils.settings?.allowBreak?.value ?: true
            if (!allowBreak) BaritoneUtils.settings?.allowBreak?.value = false
            inputTimer.reset()
            baritoneDisconnectOnArrival()
        }

        onDisable {
            startPos = null
            BaritoneUtils.settings?.allowBreak?.value = baritoneAllowBreak
            BaritoneUtils.settings?.disconnectOnArrival?.value = baritoneDisconnectOnArrival
            BaritoneUtils.cancelEverything()
        }

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
            if (!autoReply || it.packet !is SPacketChat) return@listener
            if (MessageDetection.Direct.RECEIVE detect it.packet.chatComponent.unformattedText) {
                sendServerMessage("/r $replyMessage")
            }
        }

        listener<InputEvent.MouseInputEvent> {
            if (inputTimeout > 0 && isClicking()) {
                startPos = null
                inputTimer.reset()
            }
        }

        listener<InputEvent.KeyInputEvent> {
            if (inputTimeout > 0 && isPressing()) {
                startPos = null
                inputTimer.reset()
            }
        }
    }

    private fun isClicking() =
        mc.gameSettings.keyBindAttack.isKeyDown
            || mc.gameSettings.keyBindUseItem.isKeyDown

    private fun isPressing() =
        mc.gameSettings.keyBindJump.isKeyDown
            || mc.gameSettings.keyBindSneak.isKeyDown
            || mc.gameSettings.keyBindForward.isKeyDown
            || mc.gameSettings.keyBindBack.isKeyDown
            || mc.gameSettings.keyBindLeft.isKeyDown
            || mc.gameSettings.keyBindRight.isKeyDown

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeListener

            if (inputTimeout > 0) {
                if ((startPos == null || !BaritoneUtils.isPathing) && player.realSpeed > 0.2) {
                    inputTimer.reset()
                }

                if (!inputTimer.tick(inputTimeout.toLong(), false)) {
                    startPos = null
                    return@safeListener
                }
            }

            if (actionTimer.tick(nextActionDelay.toLong())) {
                val random = if (variation > 0) (0..variation).random() else 0
                nextActionDelay = delay + random

                when ((getAction())) {
                    Action.SWING -> player.swingArm(EnumHand.MAIN_HAND)
                    Action.JUMP -> player.jump()
                    Action.TURN -> player.rotationYaw = Random.nextDouble(-180.0, 180.0).toFloat()
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

    private fun SafeClientEvent.squareWalk() {
        if (startPos == null) startPos = player.position

        startPos?.let {
            when (squareStep) {
                0 -> baritoneGotoXZ(it.x, it.z + radius)
                1 -> baritoneGotoXZ(it.x + radius, it.z + radius)
                2 -> baritoneGotoXZ(it.x + radius, it.z)
                3 -> baritoneGotoXZ(it.x, it.z)
            }
        }

        squareStep = (squareStep + 1) % 4
    }

    private fun baritoneGotoXZ(x: Int, z: Int) {
        BaritoneUtils.primary?.customGoalProcess?.setGoalAndPath(GoalXZ(x, z))
    }

    private enum class Action(val setting: BooleanSetting) {
        SWING(swing),
        JUMP(jump),
        TURN(turn)
    }

    init {
        walk.listeners.add {
            if (isEnabled) {
                BaritoneUtils.cancelEverything()
            }
        }
    }
}
