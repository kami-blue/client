package org.kamiblue.client.module.modules.movement

import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketCloseWindow
import org.kamiblue.client.event.Phase
import org.kamiblue.client.event.events.OnUpdateWalkingPlayerEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.event.events.PlayerTravelEvent
import org.kamiblue.client.manager.managers.PlayerPacketManager
import org.kamiblue.client.manager.managers.PlayerPacketManager.sendPlayerPacket
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.MovementUtils
import org.kamiblue.client.util.MovementUtils.calcMoveYaw
import org.kamiblue.client.util.threads.runSafe
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener
import kotlin.math.cos
import kotlin.math.sin

internal object Flight : Module(
    name = "Flight",
    category = Category.MOVEMENT,
    description = "Makes the player fly",
    modulePriority = 500
) {
    private val mode by setting("Mode", FlightMode.VANILLA)
    private val speed by setting("Speed", 1.0f, 0.0f..10.0f, 0.1f)
    private val glideSpeed by setting("Glide Speed", 0.05, 0.0..0.3, 0.001)

    private enum class FlightMode {
        VANILLA, STATIC, PACKET
    }

    init {
        onDisable {
            runSafe {
                player.capabilities?.apply {
                    isFlying = false
                    flySpeed = 0.05f
                }
            }
        }

        safeListener<PlayerTravelEvent> {
            when (mode) {
                FlightMode.STATIC -> {
                    player.capabilities.isFlying = true
                    player.capabilities.flySpeed = speed

                    player.motionX = 0.0
                    player.motionY = -glideSpeed
                    player.motionZ = 0.0

                    if (mc.gameSettings.keyBindJump.isKeyDown) player.motionY += speed / 2.0f
                    if (mc.gameSettings.keyBindSneak.isKeyDown) player.motionY -= speed / 2.0f
                }
                FlightMode.VANILLA -> {
                    player.capabilities.isFlying = true
                    player.capabilities.flySpeed = speed / 11.11f

                    if (glideSpeed != 0.0
                        && !mc.gameSettings.keyBindJump.isKeyDown
                        && !mc.gameSettings.keyBindSneak.isKeyDown) player.motionY = -glideSpeed
                }
                FlightMode.PACKET -> {
                    it.cancel()

                    player.motionY = if (mc.gameSettings.keyBindJump.isKeyDown xor mc.gameSettings.keyBindSneak.isKeyDown) {
                        if (mc.gameSettings.keyBindJump.isKeyDown) 0.0622
                        else -0.0622
                    } else {
                        if (MovementUtils.isInputting) {
                            val yaw = calcMoveYaw()
                            player.motionX = -sin(yaw) * 0.2f
                            player.motionZ = cos(yaw) * 0.2f
                        }
                        -glideSpeed
                    }

                    val posX = player.posX + player.motionX
                    val posY = player.posY + player.motionY
                    val posZ = player.posZ + player.motionZ

                    connection.sendPacket(CPacketPlayer.PositionRotation(posX, posY, posZ, player.rotationYaw, player.rotationPitch, false))
                    connection.sendPacket(CPacketPlayer.Position(posX, player.posY - 42069, posZ, true))
                }
            }
        }

        listener<OnUpdateWalkingPlayerEvent> {
            if (mode != FlightMode.PACKET || it.phase != Phase.PRE) return@listener
            sendPlayerPacket {
                cancelAll()
            }
        }

        listener<PacketEvent.Receive> {
            if (mode == FlightMode.PACKET && it.packet is SPacketCloseWindow) it.cancel()
        }
    }
}
