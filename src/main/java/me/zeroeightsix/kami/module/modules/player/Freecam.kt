package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.math.RotationUtils
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.MoverType
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.InputEvent
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Module.Info(
        name = "Freecam",
        category = Module.Category.PLAYER,
        description = "Leave your body and transcend into the realm of the gods"
)
object Freecam : Module() {
    private val horizontalSpeed = register(Settings.floatBuilder("HorizontalSpeed").withValue(20f).withRange(1f, 50f).withStep(1f))
    private val verticalSpeed = register(Settings.floatBuilder("VerticalSpeed").withValue(20f).withRange(1f, 50f).withStep(1f))

    private var prevThirdPersonViewSetting = -1
    var cameraGuy: EntityPlayer? = null
        private set
    var resetInput = false

    @EventHandler
    private val disconnectListener = Listener(EventHook { event: ConnectionEvent.Disconnect ->
        prevThirdPersonViewSetting = -1
        cameraGuy = null
        mc.renderChunksMany = true
    })

    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (mc.world == null || event.packet !is CPacketUseEntity) return@EventHook
        // Don't interact with self
        if (event.packet.getEntityFromWorld(mc.world) == mc.player) event.cancel()
    })

    @EventHandler
    private val keyboardListener = Listener(EventHook { event: InputEvent.KeyInputEvent ->
        if (mc.world == null || mc.player == null) return@EventHook
        // Force it to stay in first person lol
        if (mc.gameSettings.keyBindTogglePerspective.isKeyDown) mc.gameSettings.thirdPersonView = 2
    })

    override fun onDisable() {
        if (mc.player == null) return
        mc.world.removeEntityFromWorld(-6969420)
        mc.setRenderViewEntity(mc.player)
        cameraGuy = null
        mc.player.rotationYawHead
        if (prevThirdPersonViewSetting != -1) mc.gameSettings.thirdPersonView = prevThirdPersonViewSetting
    }

    override fun onUpdate() {
        if (cameraGuy == null && mc.player.ticksExisted > 20) {
            // Create a cloned player
            cameraGuy = CameraGuy(mc.player).also {
                // Add it to the world
                mc.world.addEntityToWorld(-6969420, it)

                // Reset player movement input
                resetInput = true

                // Stores prev third person view setting
                prevThirdPersonViewSetting = mc.gameSettings.thirdPersonView
            }
        }
    }

    private class CameraGuy(val player: EntityPlayerSP): EntityOtherPlayerMP(mc.world, mc.session.profile) {
        init {
            copyLocationAndAnglesFrom(mc.player)
            capabilities.allowFlying = true
            capabilities.isFlying = true
        }

        override fun onLivingUpdate() {
            // Force the render view entity to be our camera guy
            if (mc.renderViewEntity != this) mc.setRenderViewEntity(this)

            // Update inventory
            inventory.copyInventory(player.inventory)

            // Update yaw head
            updateEntityActionState()

            // We have to update movement input from key binds because mc.player.movementInput is used by Baritone
            // Updates forward movement input
            moveForward = if (mc.gameSettings.keyBindForward.isKeyDown xor mc.gameSettings.keyBindBack.isKeyDown) {
                if (mc.gameSettings.keyBindForward.isKeyDown) 1f else -1f
            } else {
                0f
            }

            // Updates strafe movement input
            moveStrafing = if (mc.gameSettings.keyBindLeft.isKeyDown xor mc.gameSettings.keyBindRight.isKeyDown) {
                if (mc.gameSettings.keyBindRight.isKeyDown) 1f else -1f
            } else {
                0f
            }

            // Updates vertical movement input
            moveVertical = if (mc.gameSettings.keyBindJump.isKeyDown xor mc.gameSettings.keyBindSneak.isKeyDown) {
                if (mc.gameSettings.keyBindJump.isKeyDown) 1f else -1f
            } else {
                0f
            }

            // Update sprinting
            isSprinting = mc.gameSettings.keyBindSprint.isKeyDown

            val yawRad = Math.toRadians(rotationYaw - RotationUtils.getRotationFromVec(Vec3d(moveStrafing.toDouble(), 0.0, moveForward.toDouble())).x)
            val speed = (horizontalSpeed.value / 20f) * min(abs(moveForward) + abs(moveStrafing), 1f)

            motionX = -sin(yawRad) * speed
            motionY = moveVertical.toDouble() * (verticalSpeed.value / 20f)
            motionZ = cos(yawRad) * speed

            if (isSprinting) {
                motionX *= 1.5
                motionY *= 1.5
                motionZ *= 1.5
            }

            noClip = true

            move(MoverType.SELF, motionX, motionY, motionZ)
        }

        override fun getEyeHeight() = 1.65f
    }
}