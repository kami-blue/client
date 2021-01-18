package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.mixin.extension.*
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.MovementUtils.calcMoveYawDeg
import me.zeroeightsix.kami.util.MovementUtils.isInputting
import me.zeroeightsix.kami.util.MovementUtils.speed
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.WorldUtils.getGroundPos
import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.threads.runSafe
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketConfirmTeleport
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketEntityMetadata
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.math.Vec3d
import org.kamiblue.commons.extension.toRadian
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal object ElytraFlight2b2t : Module(
    name = "ElytraFlight2b2t",
    description = "Allows high speed infinite Elytra flight with no durability usage on 2b2t",
    category = Category.MOVEMENT
) {
    private val accelerateTime by setting("AccelerateTime", 7.0f, 0.5f..20.0f, 0.01f)
    private val speed by setting("Speed", 3.28f, 1.0f..3.28f, 0.005f)
    private val descendSpeed by setting("DescendSpeed", 0.2, 0.01..1.0, 0.01)
    private val idleSpeed by setting("IdleSpeed", 3.8f, 0.1f..5.0f, 0.1f)
    private val idleRadius by setting("IdleRadius", 0.08f, 0.01f..0.25f, 0.01f)
    private val minIdleVelocity by setting("MinIdleVelocity", 0.013f, 0.0f..0.1f, 0.001f)
    private val packetDelay by setting("PacketDelay", 122, 50..200, 1)
    private val rubberbandTimeout by setting("RubberbandTimeout", 300, 0..1000, 1)
    private val showDebug by setting("ShowDebug", false)

    private const val TAKEOFF_HEIGHT = 0.50

    /* Packet information */
    private val packetTimer = TickTimer()
    private val packetSet = Collections.synchronizedSet(HashSet<CPacketPlayer>())
    private var rotation = Vec2f.ZERO

    /* Startup variables */
    private var state = MovementState.NOT_STARTED
    private var accelerateStart = 0L

    /* Emergency teleport packet info */
    private val rubberbandTimer = TickTimer()
    private var teleportPosition = Vec3d.ZERO
    private var teleportRotation = Vec2f.ZERO

    private enum class MovementState {
        NOT_STARTED, IDLE, MOVING
    }

    override fun isActive(): Boolean {
        return isEnabled && state != MovementState.NOT_STARTED
    }

    init {
        onDisable {
            reset()
        }

        /**
         * Listen to PlayerTravelEvent in the case where the user is pressing the spacebar and we have not yet started.
         * This allows us to determine if we need to handle a jumping or a falling start.
         *
         * Also cancel movement that would otherwise cause us to jitter in the -Y direction
         */
        safeListener<PlayerTravelEvent> {
            val yawRad = calcYaw()

            if (state != MovementState.NOT_STARTED) {
                /* If we are not wearing an elytra then reset */
                if (player.inventory.armorInventory[2].item != Items.ELYTRA) {
                    reset()
                    return@safeListener
                }

                /* If we are too close to the ground then reset */
                if (player.posY - getGroundPos().y < TAKEOFF_HEIGHT) {
                    reset()
                    return@safeListener
                }

                player.motionY = 0.0
                player.capabilities.isFlying = true

                if (!rubberbandTimer.tick(rubberbandTimeout.toLong(), false)) {
                    player.setVelocity(0.0, 0.0, 0.0)
                    player.limbSwing = 0.0f
                    player.limbSwingAmount = 0.0f
                    player.prevLimbSwingAmount = 0.0f

                    accelerateStart = System.currentTimeMillis()
                    rotation = Vec2f(teleportRotation.x, teleportRotation.y)

                    it.cancel()
                    return@safeListener
                }

                if (player.movementInput.sneak) {
                    player.setVelocity(0.0, -descendSpeed, 0.0)
                    return@safeListener
                }

                /* Prevent the player from sprinting */
                if (player.isSprinting) {
                    player.isSprinting = false
                }
            }

            when (state) {
                MovementState.NOT_STARTED -> takeoff()
                MovementState.IDLE -> idle()
                MovementState.MOVING -> moving(yawRad)
            }
        }
    }

    private fun SafeClientEvent.calcYaw(): Double {
        val yawDeg = calcMoveYawDeg()
        rotation = Vec2f(yawDeg.toFloat(), player.pitchYaw.x)
        return yawDeg.toRadian()
    }

    /**
     * Calculate the starting height. Constantly update the position as we would in vanilla until the correct criteria
     * is met. Should only be called from the state NOT_STARTED.
     */
    private fun SafeClientEvent.takeoff() {
        // In air + falling + elytra equipped + at least 0.5 blocks from the ground
        if (!player.onGround
            && player.motionY < -0.02
            && player.inventory.armorInventory[2].item == Items.ELYTRA
            && player.posY - getGroundPos().y >= TAKEOFF_HEIGHT) {

            if (showDebug) sendChatMessage("$chatName Takeoff at height: " + player.posY)

            connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING))
            player.capabilities.isFlying = true
            state = MovementState.IDLE
        }
    }

    private fun SafeClientEvent.idle() {
        if (isInputting) {
            state = MovementState.MOVING
            accelerateStart = System.currentTimeMillis()
        } else {
            val length = idleSpeed * 1000.0f
            val deltaTime = System.currentTimeMillis() % length.toLong()
            val yaw = deltaTime / length * 2.0 * PI

            player.motionX = -sin(yaw) * idleRadius
            player.motionZ = cos(yaw) * idleRadius
            player.motionY = 0.0
        }
    }

    private fun SafeClientEvent.moving(yawRad: Double) {
        if (isInputting) {
            val length = accelerateTime * 1000.0f
            val multiplier = ((System.currentTimeMillis() - accelerateStart) / length).coerceAtMost(1.0f)
            val multipliedSpeed = speed * multiplier

            player.motionX = -sin(yawRad) * multipliedSpeed
            player.motionZ = cos(yawRad) * multipliedSpeed
            player.motionY = 0.0
        } else {
            accelerateStart = System.currentTimeMillis()
            if (player.speed < minIdleVelocity) {
                state = MovementState.IDLE
            }
        }
    }

    init {
        safeListener<PacketEvent.Receive> {
            if (state == MovementState.NOT_STARTED) return@safeListener
            when (it.packet) {
                /* Cancels the elytra opening animation */
                is SPacketEntityMetadata -> {
                    if (it.packet.entityId == player.entityId) it.cancel()
                }
                /* Set client side to wherever the server wants us to be */
                is SPacketPlayerPosLook -> {
                    rubberbandTimer.reset()
                    teleportRotation = Vec2f(it.packet.yaw, it.packet.pitch)
                    teleportPosition = Vec3d(it.packet.x, it.packet.y, it.packet.z)
                }
            }
        }

        /**
         * Cancel any packets that are not set directly by this module
         */
        safeListener<PacketEvent.Send> {
            if (state == MovementState.NOT_STARTED || it.packet !is CPacketPlayer) return@safeListener
            if (!packetSet.remove(it.packet)) {
                it.cancel()
            }
        }

        /**
         * Listen for CPacketConfirmTeleport. We must send a PositionRotation immediately.
         */
        safeListener<PacketEvent.PostSend> {
            if (state == MovementState.NOT_STARTED || it.packet !is CPacketConfirmTeleport) return@safeListener
            if (showDebug) sendChatMessage("$chatName Responding to emergency teleport packet from the server.")

//            player.setVelocity(0.0, 0.0, 0.0) Do we really need this? Worked for me without. Still no velo on rubberband
            accelerateStart = System.currentTimeMillis()

            /* This only sets the position and rotation client side since it is not salted with onGround */
            player.setPositionAndRotation(teleportPosition.x, teleportPosition.y, teleportPosition.z, teleportRotation.x, teleportRotation.y)
            rotation = Vec2f(teleportRotation.x, teleportRotation.y)

            /* Force send the packet */
            sendForcedPacket()
        }

        safeListener<RenderOverlayEvent> {
            if (state == MovementState.NOT_STARTED || !packetTimer.tick(packetDelay.toLong())) return@safeListener
            connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING))

            sendForcedPacket()
        }
    }

    private fun SafeClientEvent.sendForcedPacket() {
        val packet = CPacketPlayer.PositionRotation(player.posX, player.posY, player.posZ, rotation.x, rotation.y, false)
        packetSet.add(packet)
        player.connection.sendPacket(packet)
    }

    private fun reset() {
        runSafe {
            player.capabilities.isFlying = false
        }

        packetSet.clear()
        rotation = Vec2f.ZERO

        state = MovementState.NOT_STARTED
        accelerateStart = 0L

        teleportPosition = Vec3d.ZERO
        teleportRotation = Vec2f.ZERO
    }
}
