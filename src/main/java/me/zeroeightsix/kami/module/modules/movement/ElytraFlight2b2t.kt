package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.mixin.extension.*
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.MovementUtils.isInputting
import me.zeroeightsix.kami.util.MovementUtils.setSpeed
import me.zeroeightsix.kami.util.MovementUtils.speed
import me.zeroeightsix.kami.util.WorldUtils.getGroundPos
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.threads.BackgroundScope
import me.zeroeightsix.kami.util.threads.onMainThreadSafe
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketConfirmTeleport
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketEntityMetadata
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.cos
import kotlin.math.sin

internal object ElytraFlight2b2t : Module(
    name = "ElytraFlight2b2t",
    description = "Allows high speed infinite Elytra flight with no durability usage on 2b2t",
    category = Category.MOVEMENT
) {
    private val accelerateSpeed by setting("AccelerateSpeed", 0.22f, 0.0f..1.0f, 0.01f)
    private val maxVelocity by setting("MaxVelocity", 9.9f, 1.0f..20.0f, 0.05f)
    private val descendSpeed by setting("DescendSpeed", 0.1, 0.01..1.0, 0.01)
    private val idleSpeed by setting("IdleSpeed", 1000.00f, 400.0f..10000f, 1f)
    private val idleRadius by setting("IdleRadius", 0.05f, 0.0f..0.25f, 0.001f)
    private val minIdleVelocity by setting("MinIdleVelocity", 0.013f, 0.0f..0.25f, 0.001f)
    private val showDebug by setting("ShowDebug", false)

    private const val TAKEOFF_HEIGHT = 0.50

    private var lastPos = Vec3d(0.0, -1.0, 0.0)
    private var rotation = Vec2f(0.0F, 0.0F)
    private var lastRotation = Vec2f(0.0F, 0.0F)

    /* Startup variables */
    private var state = MovementState.NOT_STARTED
    private var originIdle = Vec3d(0.0, -1.0, 0.0)
    private var idleStart = System.currentTimeMillis()
    private var accelStart = System.currentTimeMillis()

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
        onEnable {
            onMainThreadSafe {
                reset()
            }
        }

        onDisable {
            onMainThreadSafe {
                reset()
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@safeListener


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
            }


            when (state) {
                MovementState.NOT_STARTED -> {
                    calcStartingHeight()
                }
                MovementState.IDLE -> {
                    val targetPos = getNextIdle()
                    player.setVelocity(targetPos.x, targetPos.y, targetPos.z)
                }
                MovementState.MOVING -> {
                }
            }
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
                    player.setVelocity(player.motionX, 0.0, player.motionZ)
                    /* In case they double press space (exits flying mode normally) */
                    player.capabilities.isFlying = true
                }
            }

            if (player.movementInput.sneak && state != MovementState.NOT_STARTED) {
                player.motionY = -descendSpeed
                return@safeListener
            }

            when (state) {
                MovementState.NOT_STARTED -> {
                }
                MovementState.IDLE -> {
                    if (isInputting) {
                        state = MovementState.MOVING
                        accelStart = System.currentTimeMillis()
                    }
                }
                MovementState.MOVING -> {
                    if (isInputting) {
                        if (player.speed < maxVelocity) {
                            val speed = (System.currentTimeMillis() - accelStart) / (10000 - (accelerateSpeed * 10000 - 1))
                            setSpeed(speed.toDouble())
                        } else {
                            it.cancel()
                        }

                        player.motionY = 0.0
                    } else {
                        accelStart = System.currentTimeMillis()
                    }
                }
            }

            /* Prevent the player from sprinting */
            if (player.isSprinting && state != MovementState.NOT_STARTED) player.isSprinting = false

            /* return to IDLE state after moving */
            if (player.speed < minIdleVelocity && !isInputting && state != MovementState.NOT_STARTED) {
                setToIdle()
            }
        }

        safeListener<PacketEvent.Receive> {
            if (state != MovementState.NOT_STARTED) {
                when (it.packet) {
                    /* Cancels the elytra opening animation */
                    is SPacketEntityMetadata -> {
                        if (it.packet.entityId == player.entityId) it.cancel()
                    }
                    /* Set client side to wherever the server wants us to be */
                    is SPacketPlayerPosLook -> {
                        teleportRotation = Vec2f(it.packet.pitch, it.packet.yaw)
                        teleportPosition = Vec3d(it.packet.x, it.packet.y, it.packet.z)
                    }
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
            if (state != MovementState.NOT_STARTED) {
                when (it.packet) {
                    is CPacketPlayer.Position -> {
                        if (it.packet.onGround) {
                            if (player.posY - getGroundPos().y > 0.0) {
                                it.packet.onGround = false
                            }
                        } else {
                            it.cancel()
                        }
                    }
                    is CPacketPlayer.PositionRotation -> {
                        if (it.packet.onGround) {
                            if (player.posY - getGroundPos().y > 0.0) {
                                it.packet.onGround = false
                            }
                        } else {
                            rotation = Vec2f(it.packet.pitch, it.packet.yaw)
                            it.cancel()
                        }
                    }
                    is CPacketPlayer.Rotation -> {
                        rotation = Vec2f(it.packet.pitch, it.packet.yaw)
                        it.cancel()
                    }
                }
            }
        }

        /**
         * Listen for CPacketConfirmTeleport. We must send a PositionRotation immediately.
         */
        safeListener<PacketEvent.PostSend> {
            when (it.packet) {
                is CPacketConfirmTeleport -> {
                    if (showDebug) sendChatMessage("Responding to emergency teleport packet from the server.")
                    player.setVelocity(0.0, 0.0, 0.0)
                    accelStart = System.currentTimeMillis()
                    /* This only sets the position and rotation client side since it is not salted with onGround */
                    player.setPositionAndRotation(teleportPosition.x,
                        teleportPosition.y,
                        teleportPosition.z,
                        teleportRotation.y,
                        teleportRotation.x)
                    /* Force send the packet */
                    sendForcedPacket(true)
                }
            }
        }

        BackgroundScope.launchLooping("FlyConnection", 150L) {
            if (state != MovementState.NOT_STARTED) {
                onMainThreadSafe {
                    connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING))

                    rotation = player.pitchYaw
                    sendForcedPacket(false)

                    connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING))
                    connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING))
                }
            }
        }

        safeListener<ConnectionEvent.Disconnect> {
            disable()
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
        /* Determine which packet we need to send: position, rotation, or positionRotation */
        if (player.posX != lastPos.x || player.posY != lastPos.y || player.posZ != lastPos.z || forceSendPosRot) {
            if (rotation.x != lastRotation.x || rotation.y != lastRotation.y || forceSendPosRot) {
                /* Position and rotation need to be sent to the server */
                player.connection.sendPacket(CPacketPlayer.PositionRotation(player.posX,
                    player.posY,
                    player.posZ,
                    player.pitchYaw.y,
                    player.pitchYaw.x,
                    true))
            } else {
                /* Position needs to be sent to the server */
                player.connection.sendPacket(CPacketPlayer.Position(player.posX,
                    player.posY,
                    player.posZ,
                    true))
            }
        } else {
            /* Position and rotation need to be sent to the server */
            player.connection.sendPacket(CPacketPlayer.PositionRotation(player.posX,
                player.posY,
                player.posZ,
                player.pitchYaw.y,
                player.pitchYaw.x,
                true))
        }

        lastRotation = rotation
        lastPos = player.positionVector
    }

    /**
     * Calculate the starting height. Constantly update the position as we would in vanilla until the correct criteria
     * is met. Should only be called from the state NOT_STARTED.
     */
    private fun SafeClientEvent.calcStartingHeight() {
        /* We are in the air at least 0.5 above the ground and have an elytra equipped */
        if (!player.onGround && player.inventory.armorInventory[2].item == Items.ELYTRA && (player.posY - getGroundPos().y >= TAKEOFF_HEIGHT)) {
            if (showDebug) sendChatMessage("Takeoff at height: " + player.posY)
            player.capabilities.isFlying = true
        }
    }

    private fun SafeClientEvent.setToIdle() {
        originIdle = player.positionVector
        state = MovementState.IDLE
        idleStart = System.currentTimeMillis()
    }

    /**
     * Calculate the idle position. Some movement is needed so that we do not get kicked for flying. Move in a circle.
     */
    private fun getNextIdle(): Vec3d {
        return Vec3d(
            cos((((System.currentTimeMillis() - idleStart) / idleSpeed) % 360.0f).toDouble()) * idleRadius,
            0.0,
            sin((((System.currentTimeMillis() - idleStart) / idleSpeed) % 360.0f).toDouble()) * idleRadius,
        )
    }

    private fun SafeClientEvent.reset() {
        player.capabilities.isFlying = false
        player.onGround = true
        state = MovementState.NOT_STARTED
        rotation = Vec2f(0.0F, 0.0F)
    }
}
