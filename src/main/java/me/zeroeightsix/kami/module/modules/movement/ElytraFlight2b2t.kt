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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal object ElytraFlight2b2t : Module(
    name = "ElytraFlight2b2t",
    description = "Allows high speed infinite Elytra flight with no durability usage on 2b2t",
    category = Category.MOVEMENT
) {
    private val accelerateTime by setting("AccelerateTime", 15.0f, 0.5f..30.0f, 0.5f)
    private val speed by setting("Speed", 2.7f, 1.0f..10.0f, 0.01f)
    private val descendSpeed by setting("DescendSpeed", 0.1, 0.01..1.0, 0.01)
    private val idleSpeed by setting("IdleSpeed", 2.0f, 0.1f..5.0f, 0.1f)
    private val idleRadius by setting("IdleRadius", 0.05f, 0.01f..0.25f, 0.01f)
    private val minIdleVelocity by setting("MinIdleVelocity", 0.013f, 0.0f..0.25f, 0.001f)
    private val packetDelay by setting("PacketDelay", 150, 50..1000, 10)
    private val packets by setting("Packets", 3, 1..10, 1)
    private val showDebug by setting("ShowDebug", false)

    private const val TAKEOFF_HEIGHT = 0.50

    /* Packet information */
    private val packetTimer = TickTimer()
    private var lastPos = Vec3d.ZERO
    private var rotation = Vec2f.ZERO
    private var lastRotation = Vec2f.ZERO

    /* Startup variables */
    private var state = MovementState.NOT_STARTED
    private var accelerateStart = 0L

    /* Emergency teleport packet info */
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
            /* If are are pressing the space and not started then we need to start in the jump state */
            if (player.movementInput.jump) {
                if (state == MovementState.NOT_STARTED) {
                    return@safeListener
                } else {
                    /* Don't go up but maintain other velocities if the user tries to fly up */
                    player.motionY = 0.0

                    /* In case they double press space (exits flying mode normally) */
                    player.capabilities.isFlying = true
                }
            }

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
                MovementState.NOT_STARTED -> notStarted()
                MovementState.IDLE -> idle()
                MovementState.MOVING -> moving(yawRad)
            }
        }
    }

    private fun SafeClientEvent.calcYaw(): Double {
        val yawDeg = calcMoveYawDeg()
        rotation = Vec2f(yawDeg.toFloat(), 0.0f)
        return yawDeg.toRadian()
    }

    /**
     * Calculate the starting height. Constantly update the position as we would in vanilla until the correct criteria
     * is met. Should only be called from the state NOT_STARTED.
     */
    private fun SafeClientEvent.notStarted() {
        /* We are in the air at least 0.5 above the ground and have an elytra equipped */
        if (!player.onGround && player.inventory.armorInventory[2].item == Items.ELYTRA && (player.posY - getGroundPos().y >= TAKEOFF_HEIGHT)) {
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
                    teleportRotation = Vec2f(it.packet.yaw, it.packet.pitch)
                    teleportPosition = Vec3d(it.packet.x, it.packet.y, it.packet.z)
                }
            }
        }

        /**
         * Cancel any packets that are not set directly by this module
         *
         * Note that while we are active, all packets should have onGround = false. We salt the desired packets by setting
         * onGround == true (which we obviously need to revert before sending to the server)
         */
        safeListener<PacketEvent.Send> {
            if (state == MovementState.NOT_STARTED || it.packet !is CPacketPlayer) return@safeListener

            when (it.packet) {
                is CPacketPlayer.Position, is CPacketPlayer.PositionRotation -> {
                    if (it.packet.onGround) {
                        if (player.posY > getGroundPos().y) {
                            it.packet.onGround = false
                        }
                    } else {
                        it.cancel()
                    }
                }
                else -> {
                    it.cancel()
                }
            }
        }

        /**
         * Listen for CPacketConfirmTeleport. We must send a PositionRotation immediately.
         */
        safeListener<PacketEvent.PostSend> {
            if (state == MovementState.NOT_STARTED || it.packet !is CPacketConfirmTeleport) return@safeListener
            if (showDebug) sendChatMessage("$chatName Responding to emergency teleport packet from the server.")

            player.setVelocity(0.0, 0.0, 0.0)
            accelerateStart = System.currentTimeMillis()

            /* This only sets the position and rotation client side since it is not salted with onGround */
            player.setPositionAndRotation(teleportPosition.x, teleportPosition.y, teleportPosition.z, teleportRotation.x, teleportRotation.y)

            /* Force send the packet */
            sendForcedPacket(true)
        }

        safeListener<RenderOverlayEvent> {
            if (state == MovementState.NOT_STARTED || !packetTimer.tick(packetDelay.toLong())) return@safeListener
            connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING))

            sendForcedPacket(false)

            for (i in 1 until packets) {
                connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING))
            }
        }
    }

    /**
     * @param forceSendPosRot: If we should force send the position and rotation regardless of if it is our current position/rotation
     *
     * Sends a packet salted with onGround == true to allow it to be sent in PacketEvent.Send.
     *
     * By default, send the position and rotation (if this function is called we ALWAYS need to send a packet). If not,
     * send either the position or both the position and rotation, whichever is appropriate.
     */
    private fun SafeClientEvent.sendForcedPacket(forceSendPosRot: Boolean) {
        val posVec = player.positionVector

        /* Determine which packet we need to send: position, rotation, or positionRotation */
        if (forceSendPosRot || posVec != lastPos && rotation != lastRotation) {
            /* Position and rotation need to be sent to the server */
            player.connection.sendPacket(CPacketPlayer.PositionRotation(player.posX, player.posY, player.posZ, rotation.x, rotation.y, true))
        } else {
            /* Position needs to be sent to the server */
            player.connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY, player.posZ, true))
        }

        lastRotation = rotation
        lastPos = posVec
    }

    private fun reset() {
        runSafe {
            player.capabilities.isFlying = false
        }

        lastPos = Vec3d.ZERO
        rotation = Vec2f.ZERO
        lastRotation = Vec2f.ZERO

        state = MovementState.NOT_STARTED
        accelerateStart = 0L

        teleportPosition = Vec3d.ZERO
        teleportRotation = Vec2f.ZERO
    }
}
