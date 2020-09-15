package me.zeroeightsix.kami.module.modules.misc

import baritone.api.BaritoneAPI
import baritone.api.pathing.goals.GoalXZ
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.math.MathUtils.reverseNumber
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.util.EnumHand
import java.util.*

@Module.Info(
        name = "AntiAFK",
        category = Module.Category.MISC,
        description = "Prevents being kicked for AFK"
)
object AntiAFK : Module() {
    private val frequency = register(Settings.integerBuilder("ActionFrequency").withMinimum(1).withMaximum(100).withValue(40).build())
    val autoReply = register(Settings.b("AutoReply", true))
    private val swing = register(Settings.b("Swing", true))
    private val jump = register(Settings.b("Jump", true))
    private val squareWalk = register(Settings.b("SquareWalk", true))
    private val radius = register(Settings.integerBuilder("Radius").withMinimum(1).withValue(64).build())
    private val turn = register(Settings.b("Turn", true))

    private val random = Random()
    private val squareStartCoords = intArrayOf(0, 0)
    private var squareStep = 0
    private var baritoneDisconnectOnArrival = false

    public override fun onEnable() {
        if (mc.player == null) return

        baritoneDisconnectOnArrival = BaritoneAPI.getSettings().disconnectOnArrival.value
        BaritoneAPI.getSettings().disconnectOnArrival.value = false
        squareStartCoords[0] = mc.player.posX.toInt()
        squareStartCoords[1] = mc.player.posZ.toInt()
    }

    public override fun onDisable() {
        if (mc.player == null) return

        BaritoneAPI.getSettings().disconnectOnArrival.value = baritoneDisconnectOnArrival
        if (isBaritoneActive) BaritoneAPI.getProvider().primaryBaritone.pathingBehavior.cancelEverything()
    }

    override fun onUpdate() {
        if (mc.playerController.getIsHittingBlock()) return
        if (swing.value && mc.player.ticksExisted % (0.5 * getFrequency()) == 0.0) {
            Objects.requireNonNull(mc.connection)!!.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
        }
        if (squareWalk.value && mc.player.ticksExisted % getFrequency() == 0f && !isBaritoneActive) {
            val r = clamp(radius.value)
            when (squareStep) {
                0 -> baritoneGotoXZ(squareStartCoords[0], squareStartCoords[1] + r)
                1 -> baritoneGotoXZ(squareStartCoords[0] + r, squareStartCoords[1] + r)
                2 -> baritoneGotoXZ(squareStartCoords[0] + r, squareStartCoords[1])
                3 -> baritoneGotoXZ(squareStartCoords[0], squareStartCoords[1])
            }
            squareStep = (squareStep + 1) % 4
        }
        if (jump.value && mc.player.ticksExisted % (2 * getFrequency()) == 0f) {
            mc.player.jump()
        }
        if (turn.value && mc.player.ticksExisted % (0.375 * getFrequency()) == 0.0) {
            mc.player.rotationYaw = random.nextInt(360) - makeNegRandom(180).toFloat()
        }
    }

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (autoReply.value && event.packet is SPacketChat && event.packet.getChatComponent().unformattedText.contains("whispers: ") && !event.packet.getChatComponent().unformattedText.contains(mc.player.name)) {
            sendServerMessage("/r I am currently AFK and using KAMI Blue!")
        }
    })

    private val isBaritoneActive: Boolean
        get() = BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.isActive

    private fun baritoneGotoXZ(x: Int, z: Int) {
        BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(GoalXZ(x, z))
    }

    private fun clamp(`val`: Int): Int {
        return if (`val` < 0) 0 else `val`
    }

    private fun getFrequency(): Float {
        return reverseNumber(frequency.value, 1, 100).toFloat()
    }

    private fun makeNegRandom(input: Int): Int {
        val rand = if (random.nextBoolean()) 1 else 0
        return if (rand == 0) {
            -input
        } else {
            input
        }
    }
}