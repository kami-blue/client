package me.zeroeightsix.kami.module.modules.player

import baritone.api.pathing.goals.GoalTwoBlocks
import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.PlayerAttackEvent
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.MovementUtils.calcMoveYaw
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.threads.runSafe
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.MoverType
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.MovementInput
import net.minecraft.util.MovementInputFromOptions
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.extension.floorToInt
import org.kamiblue.commons.extension.toRadian
import org.kamiblue.commons.interfaces.DisplayEnum
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import kotlin.math.*

internal object Freecam : Module(
    name = "Freecam",
    category = Category.PLAYER,
    description = "Leave your body and transcend into the realm of the gods"
) {
    private val directionMode = setting("FlightMode", FlightMode.CREATIVE)
    private val horizontalSpeed = setting("HorizontalSpeed", 20f, 1f..50f, 1f)
    private val verticalSpeed = setting("VerticalSpeed", 20f, 1f..50f, 1f)
    private val autoRotate = setting("AutoRotate", true)
    private val arrowKeyMove = setting("ArrowKeyMove", true)
    private val disableOnDisconnect = setting("DisconnectDisable", true)
    private val leftClickCome = setting("LeftClickCome", true)

    private enum class FlightMode(override val displayName: String) : DisplayEnum {
        CREATIVE("Creative"),
        THREE_DEE("3D")
    }

    private var prevThirdPersonViewSetting = -1
    private val clickTimer = TickTimer(TimeUnit.SECONDS)
    var cameraGuy: EntityPlayer? = null; private set

    private const val ENTITY_ID = -6969420

    init {
        onEnable {
            mc.renderChunksMany = false
        }

        onDisable {
            mc.renderChunksMany = true
            resetCameraGuy()
            resetMovementInput(mc.player?.movementInput)
        }

        listener<ConnectionEvent.Disconnect> {
            prevThirdPersonViewSetting = -1
            if (disableOnDisconnect.value) disable()
            else cameraGuy = null
        }

        safeListener<PacketEvent.Send> {
            if (it.packet !is CPacketUseEntity) return@safeListener
            // Don't interact with self
            if (it.packet.getEntityFromWorld(world) == player) it.cancel()
        }

        listener<PlayerAttackEvent> {
            if (it.entity == mc.player) it.cancel()
        }

        safeListener<InputEvent.KeyInputEvent> {
            // Force it to stay in first person lol
            if (mc.gameSettings.keyBindTogglePerspective.isKeyDown) mc.gameSettings.thirdPersonView = 2
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeListener

            if (player.isDead || player.health <= 0.0f) {
                if (cameraGuy != null) resetCameraGuy()
                return@safeListener
            }

            if (cameraGuy == null && player.ticksExisted > 20) spawnCameraGuy()
        }

        safeListener<InputUpdateEvent>(9999) {
            if (it.movementInput !is MovementInputFromOptions || BaritoneUtils.isPathing) return@safeListener

            resetMovementInput(it.movementInput)

            if (BaritoneUtils.isActive) return@safeListener

            if (autoRotate.value) updatePlayerRotation()
            if (arrowKeyMove.value) updatePlayerMovement()
        }

        listener<InputEvent.MouseInputEvent> {
            if (leftClickCome.value && Mouse.getEventButton() == 0 && clickTimer.tick(1L)) {
                val result = mc.objectMouseOver ?: return@listener

                if (result.typeOfHit != RayTraceResult.Type.BLOCK) {
                    return@listener
                }

                BaritoneUtils.cancelEverything()
                BaritoneUtils.primary?.customGoalProcess?.setGoalAndPath(GoalTwoBlocks(result.hitVec.toBlockPos()))
            }
        }
    }

    @JvmStatic
    val renderChunkOffset
        get() = BlockPos(
            (mc.player.posX / 16).floorToInt() * 16,
            (mc.player.posY / 16).floorToInt() * 16,
            (mc.player.posZ / 16).floorToInt() * 16
        )

    @JvmStatic
    fun getRenderViewEntity(renderViewEntity: EntityPlayer): EntityPlayer {
        val player = mc.player
        return if (isEnabled && player != null) {
            player
        } else {
            renderViewEntity
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

    private fun SafeClientEvent.updatePlayerRotation() {
        mc.objectMouseOver?.let {
            val hitVec = it.hitVec
            if (it.typeOfHit == RayTraceResult.Type.MISS || hitVec == null) return
            val rotation = RotationUtils.getRotationTo(hitVec)
            player.apply {
                rotationYaw = rotation.x
                rotationPitch = rotation.y
            }
        }
    }

    private fun SafeClientEvent.updatePlayerMovement() {
        cameraGuy?.let {
            val forward = Keyboard.isKeyDown(Keyboard.KEY_UP) to Keyboard.isKeyDown(Keyboard.KEY_DOWN)
            val strafe = Keyboard.isKeyDown(Keyboard.KEY_LEFT) to Keyboard.isKeyDown(Keyboard.KEY_RIGHT)
            val movementInput = calcMovementInput(forward, strafe, false to false)

            val yawDiff = player.rotationYaw - it.rotationYaw
            val yawRad = calcMoveYaw(yawDiff, movementInput.first, movementInput.second).toFloat()
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

    private fun resetCameraGuy() {
        mc.addScheduledTask {
            runSafe {
                world.removeEntityFromWorld(ENTITY_ID)
                mc.renderViewEntity = player
                cameraGuy = null
                mc.renderGlobal.loadRenderers()
                if (prevThirdPersonViewSetting != -1) mc.gameSettings.thirdPersonView = prevThirdPersonViewSetting
            }
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

            val yawRad = (rotationYaw - RotationUtils.getRotationFromVec(Vec3d(moveStrafing.toDouble(), 0.0, moveForward.toDouble())).x).toDouble().toRadian()
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