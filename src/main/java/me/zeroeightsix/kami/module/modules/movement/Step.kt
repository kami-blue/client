package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.Bind
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard

/**
 * The packet mode code is licensed under MIT and can be found here:
 * https://github.com/fr1kin/ForgeHax/blob/2011740/src/main/java/com/matt/forgehax/mods/StepMod.java
 */
@Module.Info(
        name = "Step",
        description = "Changes the vanilla behavior for stepping up blocks",
        category = Module.Category.MOVEMENT,
        modulePriority = 200
)
object Step : Module() {
    private val mode: Setting<Mode> = register(Settings.e("Mode", Mode.PACKET))
    private val upStep = register(Settings.b("UpStep", true))
    private val downStep = register(Settings.b("DownStep", false))
    private val entityStep = register(Settings.booleanBuilder("Entities").withValue(true))
    private val height = register(Settings.floatBuilder("Height").withRange(0.25f, 2.0f).withValue(1.0f).withStep(0.25f))
    private val downSpeed = register(Settings.floatBuilder("DownSpeed").withRange(0.0f, 1.0f).withValue(0.2f).withStep(0.05f))
    private val bindUpStep = register(Settings.custom("BindUpStep", Bind.none(), BindConverter()))
    private val bindDownStep = register(Settings.custom("BindDownStep", Bind.none(), BindConverter()))

    private const val defaultHeight = 0.6f
    private val shouldRunStep get() = !mc.player.isElytraFlying && !mc.player.capabilities.isFlying && !mc.player.isOnLadder && !mc.player.isInWater && !mc.player.isInLava

    private val ignoredPackets = HashSet<CPacketPlayer>()
    private var lastCollidedTick = 0
    private var onGroundTick = 0

    @Suppress("UNUSED")
    private enum class Mode {
        VANILLA, PACKET
    }

    override fun onDisable() {
        mc.player?.let {
            it.stepHeight = defaultHeight
            it.ridingEntity?.stepHeight = 1.0f
        }
        ignoredPackets.clear()
    }

    override fun onToggle() {
        BaritoneUtils.settings?.assumeStep?.value = isEnabled
    }

    init {
        listener<InputEvent.KeyInputEvent> {
            if (bindUpStep.value.isDown(Keyboard.getEventKey())) {
                upStep.value = !upStep.value
                MessageSendHelper.sendChatMessage(upStep.toggleMsg())
            }

            if (bindDownStep.value.isDown(Keyboard.getEventKey())) {
                downStep.value = !downStep.value
                MessageSendHelper.sendChatMessage(downStep.toggleMsg())
            }
        }
    }

    private fun Setting<Boolean>.toggleMsg() = "$chatName Turned ${this.name} ${if (this.value) "&aon" else "&coff"}&f!"

    init {
        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.START || !shouldRunStep) return@listener
            setStepHeight()
            if (downStep.value && mc.player.motionY <= 0.0 && mc.player.ticksExisted - onGroundTick <= 3) downStep()
            if (mc.player.collidedHorizontally) lastCollidedTick = mc.player.ticksExisted
            if (mc.player.onGround) onGroundTick = mc.player.ticksExisted
        }
    }

    private fun setStepHeight() {
        mc.player.stepHeight = if (upStep.value && mc.player.onGround && mc.player.collidedHorizontally) height.value else defaultHeight
        mc.player.ridingEntity?.let {
           it.stepHeight = if (entityStep.value && it.collidedHorizontally) height.value else 1.0f
        }
    }

    private fun downStep() {
        // Down step doesn't work for edge lower than 1 blocks anyways
        val belowBB = mc.player.boundingBox.expand(0.0, -1.05, 0.0)
        if (mc.world.collidesWithAnyBlock(belowBB)) mc.player.motionY -= downSpeed.value
    }

    init {
        listener<PacketEvent.Send> { event ->
            if (mc.player == null || !upStep.value || mode.value != Mode.PACKET || !shouldRunStep) return@listener
            if (event.packet !is CPacketPlayer || event.packet !is CPacketPlayer.Position && event.packet !is CPacketPlayer.PositionRotation) return@listener
            if (ignoredPackets.remove(event.packet)) return@listener

            val prevPos = PlayerPacketManager.prevServerSidePosition
            if (mc.player.ticksExisted - lastCollidedTick <= 5) getStepArray(event.packet.y - prevPos.y)?.let {
                for (posY in it) {
                    val packet = CPacketPlayer.Position(prevPos.x, prevPos.y + posY, prevPos.z, true)
                    ignoredPackets.add(packet)
                    mc.connection?.sendPacket(packet)
                }
            }
        }
    }

    private fun getStepArray(diff: Double) = when {
        height.value >= diff && diff in 0.6..1.0 -> stepOne
        height.value >= diff && diff in 1.0..1.5 -> stepOneHalf
        height.value >= diff && diff in 1.5..2.0 -> stepTwo
        else -> null
    }

    private val stepOne = doubleArrayOf(0.41999, 0.75320)
    private val stepOneHalf = doubleArrayOf(0.41999, 0.75320, 1.00133, 1.16611, 1.24919, 1.17079)
    private val stepTwo = doubleArrayOf(0.42, 0.78, 0.63, 0.51, 0.90, 1.21, 1.45, 1.43)
}