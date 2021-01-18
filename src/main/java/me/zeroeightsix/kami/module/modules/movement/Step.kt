package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.mixin.extension.y
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.settings.impl.primitive.BooleanSetting
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.Bind
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.runSafe
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard

/**
 * The packet mode code is licensed under MIT and can be found here:
 * https://github.com/fr1kin/ForgeHax/blob/2011740/src/main/java/com/matt/forgehax/mods/StepMod.java
 */
internal object Step : Module(
    name = "Step",
    description = "Changes the vanilla behavior for stepping up blocks",
    category = Category.MOVEMENT,
    modulePriority = 200
) {
    private val mode by setting("Mode", Mode.PACKET)
    private val upStep = setting("UpStep", true)
    private val downStep = setting("DownStep", false)
    private val entityStep by setting("Entities", true)
    private val height by setting("Height", 1.0f, 0.25f..2.0f, 0.25f)
    private val downSpeed by setting("DownSpeed", 0.2f, 0.0f..1.0f, 0.05f)
    private val bindUpStep by setting("BindUpStep", Bind())
    private val bindDownStep by setting("BindDownStep", Bind())

    private const val defaultHeight = 0.6f
    private val SafeClientEvent.shouldRunStep get() = !player.isElytraFlying && !player.capabilities.isFlying && !player.isOnLadder && !player.isInWater && !player.isInLava

    private val ignoredPackets = HashSet<CPacketPlayer>()
    private var lastCollidedTick = 0
    private var onGroundTick = 0

    @Suppress("UNUSED")
    private enum class Mode {
        VANILLA, PACKET
    }

    init {
        onDisable {
            runSafe {
                player.apply {
                    stepHeight = defaultHeight
                    ridingEntity?.stepHeight = 1.0f
                }
            }
            ignoredPackets.clear()
        }

        onToggle {
            BaritoneUtils.settings?.assumeStep?.value = isEnabled
        }

        listener<InputEvent.KeyInputEvent> {
            val key = Keyboard.getEventKey()

            if (bindUpStep.isDown(key)) {
                upStep.value = !upStep.value
                MessageSendHelper.sendChatMessage(upStep.toggleMsg())
            }

            if (bindDownStep.isDown(key)) {
                downStep.value = !downStep.value
                MessageSendHelper.sendChatMessage(downStep.toggleMsg())
            }
        }
    }

    private fun BooleanSetting.toggleMsg() = "$chatName Turned ${this.name} ${if (this.value) "&aon" else "&coff"}&f!"

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START || !shouldRunStep) return@safeListener
            setStepHeight()
            if (downStep.value && player.motionY <= 0.0 && player.ticksExisted - onGroundTick <= 3) downStep()
            if (player.collidedHorizontally) lastCollidedTick = player.ticksExisted
            if (player.onGround) onGroundTick = player.ticksExisted
        }
    }

    private fun SafeClientEvent.setStepHeight() {
        player.stepHeight = if (upStep.value && player.onGround && player.collidedHorizontally) height else defaultHeight
        player.ridingEntity?.let {
            it.stepHeight = if (entityStep && it.collidedHorizontally) height else 1.0f
        }
    }

    private fun SafeClientEvent.downStep() {
        // Down step doesn't work for edge lower than 1 blocks anyways
        val belowBB = player.entityBoundingBox.expand(0.0, -1.05, 0.0)
        if (world.collidesWithAnyBlock(belowBB)) player.motionY -= downSpeed
    }

    init {
        safeListener<PacketEvent.Send> { event ->
            if (!upStep.value || mode != Mode.PACKET || !shouldRunStep) return@safeListener
            if (event.packet !is CPacketPlayer || event.packet !is CPacketPlayer.Position && event.packet !is CPacketPlayer.PositionRotation) return@safeListener
            if (ignoredPackets.remove(event.packet)) return@safeListener

            val prevPos = PlayerPacketManager.prevServerSidePosition
            if (player.ticksExisted - lastCollidedTick <= 5) getStepArray(event.packet.y - prevPos.y)?.let {
                for (posY in it) {
                    val packet = CPacketPlayer.Position(prevPos.x, prevPos.y + posY, prevPos.z, true)
                    ignoredPackets.add(packet)
                    connection.sendPacket(packet)
                }
            }
        }
    }

    private fun getStepArray(diff: Double) = when {
        height >= diff && diff in 0.6..1.0 -> stepOne
        height >= diff && diff in 1.0..1.5 -> stepOneHalf
        height >= diff && diff in 1.5..2.0 -> stepTwo
        else -> null
    }

    private val stepOne = doubleArrayOf(0.41999, 0.75320)
    private val stepOneHalf = doubleArrayOf(0.41999, 0.75320, 1.00133, 1.16611, 1.24919, 1.17079)
    private val stepTwo = doubleArrayOf(0.42, 0.78, 0.63, 0.51, 0.90, 1.21, 1.45, 1.43)
}