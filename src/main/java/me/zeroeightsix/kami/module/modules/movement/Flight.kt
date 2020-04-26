package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtil
import net.minecraft.client.Minecraft
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation

/**
 * Created by 086 on 25/08/2017.
 */
@Module.Info(
        name = "Flight",
        category = Module.Category.MOVEMENT,
        description = "Makes the player fly"
)
class Flight : Module() {
    private val speed = register(Settings.f("Speed", 10f))
    private val mode = register(Settings.e<FlightMode>("Mode", FlightMode.VANILLA))

    override fun onEnable() {
        if (mc.player == null) return
        if (mode.value == FlightMode.VANILLA) {
            mc.player.capabilities.isFlying = true
            if (mc.player.capabilities.isCreativeMode) return
            mc.player.capabilities.allowFlying = true
        }
    }

    override fun onUpdate() {
        when (mode.value) {
            FlightMode.STATIC -> {
                mc.player.capabilities.isFlying = false
                mc.player.motionX = 0.0
                mc.player.motionY = 0.0
                mc.player.motionZ = 0.0
                mc.player.jumpMovementFactor = speed.value

                if (mc.gameSettings.keyBindJump.isKeyDown) mc.player.motionY += speed.value
                if (mc.gameSettings.keyBindSneak.isKeyDown) mc.player.motionY -= speed.value
            }
            FlightMode.VANILLA -> {
                mc.player.capabilities.flySpeed = speed.value / 100f
                mc.player.capabilities.isFlying = true
                if (mc.player.capabilities.isCreativeMode) return
                mc.player.capabilities.allowFlying = true
            }
            FlightMode.PACKET -> {
                var angle: Int
                val forward = mc.gameSettings.keyBindForward.isKeyDown
                val left = mc.gameSettings.keyBindLeft.isKeyDown
                val right = mc.gameSettings.keyBindRight.isKeyDown
                val back = mc.gameSettings.keyBindBack.isKeyDown

                if (left && right) angle = if (forward) 0 else if (back) 180 else -1 else if (forward && back) angle = if (left) -90 else if (right) 90 else -1 else {
                    angle = if (left) -90 else if (right) 90 else 0
                    if (forward) angle /= 2 else if (back) angle = 180 - angle / 2
                }
                if (angle != -1 && (forward || left || right || back)) {
                    val yaw = mc.player.rotationYaw + angle
                    mc.player.motionX = EntityUtil.getRelativeX(yaw) * 0.2f
                    mc.player.motionZ = EntityUtil.getRelativeZ(yaw) * 0.2f
                }

                mc.player.motionY = 0.0
                mc.player.connection.sendPacket(PositionRotation(mc.player.posX + mc.player.motionX, mc.player.posY + (if (Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown) 0.0622 else 0.0).toDouble() - (if (Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown) 0.0622 else 0.0).toDouble(), mc.player.posZ + mc.player.motionZ, mc.player.rotationYaw, mc.player.rotationPitch, false))
                mc.player.connection.sendPacket(PositionRotation(mc.player.posX + mc.player.motionX, mc.player.posY - 42069, mc.player.posZ + mc.player.motionZ, mc.player.rotationYaw, mc.player.rotationPitch, true))
            }
        }
    }

    override fun onDisable() {
        if (mode.value == FlightMode.VANILLA) {
            mc.player.capabilities.isFlying = false
            mc.player.capabilities.flySpeed = 0.05f
            if (mc.player.capabilities.isCreativeMode) return
            mc.player.capabilities.allowFlying = false
        }
    }

    fun moveLooking(): DoubleArray {
        return doubleArrayOf(mc.player.rotationYaw * 360.0f / 360.0f * 180.0f / 180.0f.toDouble(), 0.0)
    }

    enum class FlightMode {
        VANILLA, STATIC, PACKET
    }
}