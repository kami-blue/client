package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.ClientPlayerAttackEvent
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.MovementUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.Vec2f
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.MoverType
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
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
    private val autoRotate = register(Settings.b("AutoRotate", true))
    private val arrowKeyMove = register(Settings.b("ArrowKeyMove", true))
    private val disableOnDisconnect = register(Settings.b("DisconnectDisable", true))

    private var prevThirdPersonViewSetting = -1
    var cameraGuy: EntityPlayer? = null; private set
    var resetInput = false

    override fun onDisable() {
        if (mc.player == null) return
        mc.world.removeEntityFromWorld(-6969420)
        mc.setRenderViewEntity(mc.player)
        cameraGuy = null
        if (prevThirdPersonViewSetting != -1) mc.gameSettings.thirdPersonView = prevThirdPersonViewSetting
    }

    init {
        listener<ConnectionEvent.Disconnect> {
            prevThirdPersonViewSetting = -1
            cameraGuy = null
            mc.renderChunksMany = true
            if (disableOnDisconnect.value) disable()
        }

        listener<PacketEvent.Send> {
            if (mc.world == null || it.packet !is CPacketUseEntity) return@listener
            // Don't interact with self
            if (it.packet.getEntityFromWorld(mc.world) == mc.player) it.cancel()
        }

        listener<InputEvent.KeyInputEvent> {
            if (mc.world == null || mc.player == null) return@listener
            // Force it to stay in first person lol
            if (mc.gameSettings.keyBindTogglePerspective.isKeyDown) mc.gameSettings.thirdPersonView = 2
        }

        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@listener

            if (mc.player.isDead || mc.player.health <= 0.0f) {
                if (cameraGuy != null) onDisable() // Reset the view entity, but not disable it
                return@listener
            }

            if (cameraGuy == null && mc.player.ticksExisted > 20) spawnCameraGuy()

            if (!BaritoneUtils.isPathing && !BaritoneUtils.isActive) {
                if (autoRotate.value) updatePlayerRotation()

                if (arrowKeyMove.value) updatePlayerMovement()
            }
        }

        listener<ClientPlayerAttackEvent> {
            if (it.entity == mc.player) it.cancel()
        }
    }

    private fun spawnCameraGuy() {
        // Create a cloned player
        cameraGuy = FakeCamera(mc.player).also {
            // Add it to the world
            mc.world.addEntityToWorld(-6969420, it)

            // Set the render view entity to our camera guy
            mc.setRenderViewEntity(it)

            // Reset player movement input
            resetInput = true

            // Stores prev third person view setting
            prevThirdPersonViewSetting = mc.gameSettings.thirdPersonView
            mc.gameSettings.thirdPersonView = 0
        }
    }

    private fun updatePlayerRotation() {
        mc.objectMouseOver?.hitVec?.let {
            val rotation = Vec2f(RotationUtils.getRotationTo(it, true))
            mc.player.rotationYaw = rotation.x
            mc.player.rotationPitch = rotation.y
        }
    }

    private fun updatePlayerMovement() {
        cameraGuy?.let {
            val forward = Keyboard.isKeyDown(Keyboard.KEY_UP) to Keyboard.isKeyDown(Keyboard.KEY_DOWN)
            val strafe = Keyboard.isKeyDown(Keyboard.KEY_LEFT) to Keyboard.isKeyDown(Keyboard.KEY_RIGHT)
            val movementInput = calcMovementInput(forward, strafe, false to false)

            val yawDiff = mc.player.rotationYaw - it.rotationYaw
            val yawRad = MovementUtils.calcMoveYaw(yawDiff, movementInput.first, movementInput.second).toFloat()
            val inputTotal = min(abs(movementInput.first) + abs(movementInput.second), 1f)

            mc.player.movementInput.moveForward = cos(yawRad) * inputTotal
            mc.player.movementInput.moveStrafe = sin(yawRad) * inputTotal

            mc.player.movementInput.forwardKeyDown = mc.player.movementInput.moveForward > 0f
            mc.player.movementInput.backKeyDown = mc.player.movementInput.moveForward < 0f
            mc.player.movementInput.leftKeyDown = mc.player.movementInput.moveStrafe < 0f
            mc.player.movementInput.rightKeyDown = mc.player.movementInput.moveStrafe > 0f

            mc.player.movementInput.jump = Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)
        }
    }

    private class FakeCamera(val player: EntityPlayerSP) : EntityOtherPlayerMP(mc.world, mc.session.profile) {
        init {
            copyLocationAndAnglesFrom(mc.player)
            capabilities.allowFlying = true
            capabilities.isFlying = true
        }

        override fun onLivingUpdate() {
            // Update inventory
            inventory.copyInventory(player.inventory)

            // Update yaw head
            updateEntityActionState()

            // We have to update movement input from key binds because mc.player.movementInput is used by Baritone
            val forward = mc.gameSettings.keyBindForward.isKeyDown to mc.gameSettings.keyBindBack.isKeyDown
            val strafe = mc.gameSettings.keyBindLeft.isKeyDown to mc.gameSettings.keyBindRight.isKeyDown
            val vertical = mc.gameSettings.keyBindJump.isKeyDown to mc.gameSettings.keyBindSneak.isKeyDown
            val movementInput = calcMovementInput(forward, strafe, vertical)

            moveForward = movementInput.first
            moveStrafing = movementInput.second
            moveVertical = movementInput.third

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

    /**
     * @param forward <Forward, Backward>
     * @param strafe <Left, Right>
     * @param vertical <Up, Down>
     *
     * @return <Forward, Strafe, Vertical>
     */
    private fun calcMovementInput(forward: Pair<Boolean, Boolean>, strafe: Pair<Boolean, Boolean>, vertical: Pair<Boolean, Boolean>): Triple<Float, Float, Float> {
        // Forward movement input
        val moveForward = if (forward.first xor forward.second) {
            if (forward.first) 1f else -1f
        } else {
            0f
        }

        // Strafe movement input
        val moveStrafing = if (strafe.first xor strafe.second) {
            if (strafe.second) 1f else -1f
        } else {
            0f
        }

        // Vertical movement input
        val moveVertical = if (vertical.first xor vertical.second) {
            if (vertical.first) 1f else -1f
        } else {
            0f
        }

        return Triple(moveForward, moveStrafing, moveVertical)
    }
}