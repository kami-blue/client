package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtils
import net.minecraft.client.Minecraft
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation

@Module.Info(
        name = "Flight",
        category = Module.Category.MOVEMENT,
        description = "Makes the player fly"
)
object Flight : Module() {
    private val mode = register(Settings.enumBuilder(FlightMode::class.java).withName("Mode").withValue(FlightMode.VANILLA).build())
    private val speed = register(Settings.floatBuilder("Speed").withValue(10f).withMinimum(0f).build())
    private val glideSpeed = register(Settings.floatBuilder("GlideSpeed").withValue(0.25f).withRange(0f, 5f).withVisibility { mode.value != FlightMode.PACKET }.build())

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

    override fun onUpdate() {
        when (mode.value) {
            FlightMode.STATIC -> {
                mc.player.capabilities.isFlying = false
                mc.player.motionX = 0.0
                mc.player.motionY = -glideSpeed.value / 20.0
                mc.player.motionZ = 0.0
                mc.player.jumpMovementFactor = speed.value

                if (mc.gameSettings.keyBindJump.isKeyDown) mc.player.motionY += speed.value
                if (mc.gameSettings.keyBindSneak.isKeyDown) mc.player.motionY -= speed.value
            }
            FlightMode.VANILLA -> {
                if (glideSpeed.value != 0f) mc.player.motionY = -glideSpeed.value / 20.0
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
                    mc.player.motionX = EntityUtils.getRelativeX(yaw) * 0.2f
                    mc.player.motionZ = EntityUtils.getRelativeZ(yaw) * 0.2f
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
}