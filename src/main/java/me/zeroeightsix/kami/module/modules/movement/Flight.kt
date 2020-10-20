package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.KamiEvent
import me.zeroeightsix.kami.event.events.OnUpdateWalkingPlayerEvent
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.manager.mangers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MovementUtils
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.network.play.client.CPacketPlayer
import kotlin.math.cos
import kotlin.math.sin

@Module.Info(
        name = "Flight",
        category = Module.Category.MOVEMENT,
        description = "Makes the player fly",
        modulePriority = 500
)
object Flight : Module() {
    private val mode = register(Settings.enumBuilder(FlightMode::class.java, "Mode").withValue(FlightMode.VANILLA))
    private val speed = register(Settings.floatBuilder("Speed").withValue(1.0f).withRange(0.0f, 10.0f).withStep(0.1f))
    private val glideSpeed = register(Settings.doubleBuilder("GlideSpeed").withValue(0.05).withRange(0.0, 0.3).withStep(0.001))

    private enum class FlightMode {
        VANILLA, STATIC, PACKET
    }

    override fun onEnable() {
        if (mc.player == null) return
        if (mode.value == FlightMode.VANILLA) {
            mc.player.capabilities.isFlying = true
            if (mc.player.capabilities.isCreativeMode) return
            mc.player.capabilities.allowFlying = true
        }
    }

    override fun onDisable() {
        if (mode.value == FlightMode.VANILLA) {
            mc.player.capabilities.isFlying = false
            mc.player.capabilities.flySpeed = 0.05f
        }
    }

    init {
        listener<PlayerTravelEvent> {
            when (mode.value) {
                FlightMode.STATIC -> {
                    mc.player.capabilities.isFlying = false
                    mc.player.motionX = 0.0
                    mc.player.motionY = -glideSpeed.value
                    mc.player.motionZ = 0.0

                    if (mc.gameSettings.keyBindJump.isKeyDown) mc.player.motionY += speed.value / 2.0f
                    if (mc.gameSettings.keyBindSneak.isKeyDown) mc.player.motionY -= speed.value / 2.0f
                }
                FlightMode.VANILLA -> {
                    mc.player.capabilities.flySpeed = speed.value / 8.0f
                    mc.player.capabilities.isFlying = true

                    if (glideSpeed.value != 0.0
                            && !mc.gameSettings.keyBindJump.isKeyDown
                            && !mc.gameSettings.keyBindSneak.isKeyDown) mc.player.motionY = -glideSpeed.value
                }
                FlightMode.PACKET -> {
                    it.cancel()

                    mc.player.motionY = if (mc.gameSettings.keyBindJump.isKeyDown xor mc.gameSettings.keyBindSneak.isKeyDown) {
                        if (mc.gameSettings.keyBindJump.isKeyDown) 0.0622
                        else -0.0622
                    } else {
                        if (MovementUtils.isInputing()) {
                            val yaw = MovementUtils.calcMoveYaw()
                            mc.player.motionX = -sin(yaw) * 0.2f
                            mc.player.motionZ = cos(yaw) * 0.2f
                        }
                        -glideSpeed.value
                    }

                    val posX = mc.player.posX + mc.player.motionX
                    val posY = mc.player.posY + mc.player.motionY
                    val posZ = mc.player.posZ + mc.player.motionZ

                    mc.connection!!.sendPacket(CPacketPlayer.PositionRotation(posX, posY, posZ, mc.player.rotationYaw, mc.player.rotationPitch, false))
                    mc.connection!!.sendPacket(CPacketPlayer.Position(posX, mc.player.posY - 42069, posZ, true))
                }
                else -> {
                }
            }
        }

        listener<OnUpdateWalkingPlayerEvent> {
            if (mode.value != FlightMode.PACKET || it.era != KamiEvent.Era.PRE) return@listener
            PlayerPacketManager.addPacket(this, PlayerPacketManager.PlayerPacket(moving = false, rotating = false))
        }
    }
}