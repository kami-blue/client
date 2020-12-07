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
import net.minecraft.util.MovementInput
import net.minecraft.util.MovementInputFromOptions
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.interfaces.DisplayEnum
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
    private val directionMode = register(Settings.e<FlightMode>("FlightMode", FlightMode.CREATIVE))
    private val horizontalSpeed = register(Settings.floatBuilder("HorizontalSpeed").withValue(20f).withRange(1f, 50f).withStep(1f))
    private val verticalSpeed = register(Settings.floatBuilder("VerticalSpeed").withValue(20f).withRange(1f, 50f).withStep(1f).withVisibility { directionMode.value == FlightMode.CREATIVE })
    private val autoRotate = register(Settings.b("AutoRotate", true))
    private val arrowKeyMove = register(Settings.b("ArrowKeyMove", true))
    private val disableOnDisconnect = register(Settings.b("DisconnectDisable", true))

    private enum class FlightMode(override val displayName: String) : DisplayEnum {
        CREATIVE("Creative"),
        THREE_DEE("3D")
    }

    private var prevThirdPersonViewSetting = -1
    var cameraGuy: EntityPlayer? = null; private set

    private const val ENTITY_ID = -6969420

    override fun onDisable() {
        mc.renderChunksMany = true
        resetCameraGuy()
        resetMovementInput(mc.player?.movementInput)
    }

    override fun onEnable() {
        mc.renderChunksMany = false
    }

    init {
        listener<ConnectionEvent.Disconnect> {
            prevThirdPersonViewSetting = -1
            if (disableOnDisconnect.value) disable()
            else cameraGuy = null
        }

        listener<PacketEvent.Send> {
            if (mc.world == null || it.packet !is CPacketUseEntity) return@listener
            // Don't interact with self
            if (it.packet.getEntityFromWorld(mc.world) == mc.player) it.cancel()
        }

        listener<ClientPlayerAttackEvent> {
            if (it.entity == mc.player) it.cancel()
        }

        listener<InputEvent.KeyInputEvent> {
            if (mc.world == null || mc.player == null) return@listener
            // Force it to stay in first person lol
            if (mc.gameSettings.keyBindTogglePerspective.isKeyDown) mc.gameSettings.thirdPersonView = 2
        }

        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@listener

            if (mc.player.isDead || mc.player.health <= 0.0f) {
                if (cameraGuy != null) resetCameraGuy()
                return@listener
            }

            if (cameraGuy == null && mc.player.ticksExisted > 20) spawnCameraGuy()
        }

        listener<InputUpdateEvent>(9999) {
            if (it.movementInput !is MovementInputFromOptions || BaritoneUtils.isPathing || BaritoneUtils.isActive) return@listener

            resetMovementInput(it.movementInput)
            if (autoRotate.value) updatePlayerRotation()
            if (arrowKeyMove.value) updatePlayerMovement()
        }
    }

    private fun resetMovementInput(movementInput: MovementInput?) {
        if (movementInput !is MovementInputFromOptions) return
        movementInput.apply {
            moveForward = 0f
            moveStrafe = 0f
            forwardKeyDown = false
            backKeyDown = false
            leftKeyDown = false
            rightKeyDown = false
            jump = false
            sneak = false
        }
    }

    private fun spawnCameraGuy() {
        // Create a cloned player
        cameraGuy = FakeCamera(mc.player).also {
            // Add it to the world
            mc.world?.addEntityToWorld(ENTITY_ID, it)

            // Set the render view entity to our camera guy
            mc.renderViewEntity = it

            // Reset player movement input
            resetMovementInput(mc.player?.movementInput)

            // Stores prev third person view setting
            prevThirdPersonViewSetting = mc.gameSettings.thirdPersonView
            mc.gameSettings.thirdPersonView = 0
        }
    }

    private fun updatePlayerRotation() {
        mc.objectMouseOver?.let {
            val hitVec = it.hitVec
            if (it.typeOfHit == RayTraceResult.Type.MISS || hitVec == null) return
            val rotation = Vec2f(RotationUtils.getRotationTo(hitVec, true))
            mc.player?.apply {
                rotationYaw = rotation.x
                rotationPitch = rotation.y
            }
        }
    }

    private fun updatePlayerMovement() {
        mc.player?.let { player ->
            cameraGuy?.let {
                val forward = Keyboard.isKeyDown(Keyboard.KEY_UP) to Keyboard.isKeyDown(Keyboard.KEY_DOWN)
                val strafe = Keyboard.isKeyDown(Keyboard.KEY_LEFT) to Keyboard.isKeyDown(Keyboard.KEY_RIGHT)
                val movementInput = calcMovementInput(forward, strafe, false to false)

                val yawDiff = player.rotationYaw - it.rotationYaw
                val yawRad = MovementUtils.calcMoveYaw(yawDiff, movementInput.first, movementInput.second).toFloat()
                val inputTotal = min(abs(movementInput.first) + abs(movementInput.second), 1f)

                player.movementInput?.apply {
                    moveForward = cos(yawRad) * inputTotal
                    moveStrafe = sin(yawRad) * inputTotal

                    forwardKeyDown = moveForward > 0f
                    backKeyDown = moveForward < 0f
                    leftKeyDown = moveStrafe < 0f
                    rightKeyDown = moveStrafe > 0f

                    jump = Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)
                }
            }
        }
    }

    private fun resetCameraGuy() {
        mc.addScheduledTask {
            if (mc.player == null) return@addScheduledTask
            mc.world?.removeEntityFromWorld(ENTITY_ID)
            mc.renderViewEntity = mc.player
            cameraGuy = null
            if (prevThirdPersonViewSetting != -1) mc.gameSettings.thirdPersonView = prevThirdPersonViewSetting
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

            if (directionMode.value == FlightMode.THREE_DEE) {
                val pitchRad = Math.toRadians(rotationPitch.toDouble()) * moveForward
                motionX = -sin(yawRad) * cos(pitchRad) * speed
                motionY = -sin(pitchRad) * speed
                motionZ = cos(yawRad) * cos(pitchRad) * speed
            } else {
                motionX = -sin(yawRad) * speed
                motionY = moveVertical.toDouble() * (verticalSpeed.value / 20f)
                motionZ = cos(yawRad) * speed
            }

            if (isSprinting) {
                motionX *= 1.5
                motionY *= 1.5
                motionZ *= 1.5
            }

            noClip = true

            move(MoverType.SELF, motionX, motionY, motionZ)
        }

        override fun getEyeHeight() = 1.65f

        override fun isInvisible() = true

        override fun isInvisibleToPlayer(player: EntityPlayer) = true
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