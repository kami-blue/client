package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityBoat
import net.minecraft.entity.passive.AbstractHorse
import net.minecraft.entity.passive.EntityHorse
import net.minecraft.entity.passive.EntityPig
import net.minecraft.world.chunk.EmptyChunk
import kotlin.math.cos
import kotlin.math.sin

/**
 * Created by 086 on 16/12/2017.
 * Updated by littlebroto1 on 06/09/2020. (DD-MM-YYYY)
 */
@Module.Info(
        name = "EntitySpeed",
        category = Module.Category.MOVEMENT,
        description = "Abuse client-sided movement to shape sound barrier breaking rideables"
)
class EntitySpeed : Module() {
    private val speed = register(Settings.f("Speed", 1f))
    private val antiStuck = register(Settings.b("AntiStuck"))
    private val flight = register(Settings.b("Flight", false))
    private val wobble = register(Settings.booleanBuilder("Wobble").withValue(true).withVisibility { b: Boolean? -> flight.value }.build())

    override fun onUpdate() {
        if (mc.world != null && mc.player.getRidingEntity() != null) {
            val riding = mc.player.getRidingEntity()
            if (riding is EntityPig || riding is AbstractHorse) {
                steerEntity(riding)
            } else if (riding is EntityBoat && riding.controllingPassenger == mc.player) {
                steerBoat(boat)
            }
        }
    }

    private fun steerEntity(entity: Entity) {
        if (!flight.value) {
            entity.motionY = -0.4
        }
        if (flight.value) {
            if (mc.gameSettings.keyBindJump.isKeyDown) entity.motionY = speed.value.toDouble()
            else if (mc.gameSettings.keyBindForward.isKeyDown || mc.gameSettings.keyBindBack.isKeyDown) entity.motionY = (if (wobble.value) sin(mc.player.ticksExisted.toDouble()) else 0.0).toDouble()
        }
        moveForward(entity, speed.value * 3.8)
        if (entity is EntityHorse) {
            entity.rotationYaw = mc.player.rotationYaw
        }
    }

    private fun steerBoat(boat: EntityBoat?) {
        if (boat == null) return

        var angle: Int
        val forward = mc.gameSettings.keyBindForward.isKeyDown
        val left = mc.gameSettings.keyBindLeft.isKeyDown
        val right = mc.gameSettings.keyBindRight.isKeyDown
        val back = mc.gameSettings.keyBindBack.isKeyDown

        if (flight.value) {
            if (!(forward && back)) boat.motionY = 0.0
            if (mc.gameSettings.keyBindJump.isKeyDown) boat.motionY += speed.value / 2f.toDouble()
        }
        if (!forward && !left && !right && !back) return
        if (left && right) angle = if (forward) 0 else if (back) 180 else -1 else if (forward && back) angle = if (left) -90 else if (right) 90 else -1 else {
            angle = if (left) -90 else if (right) 90 else 0
            if (forward) angle /= 2 else if (back) angle = 180 - angle / 2
        }
        if (angle == -1) return

        val yaw = mc.player.rotationYaw + angle
        boat.motionX = EntityUtils.getRelativeX(yaw) * speed.value
        boat.motionZ = EntityUtils.getRelativeZ(yaw) * speed.value
    }

    override fun onRender() {
        val boat = boat ?: return
        boat.rotationYaw = mc.player.rotationYaw
        boat.updateInputs(false, false, false, false) // Make sure the boat doesn't turn etc (params: isLeftDown, isRightDown, isForwardDown, isBackDown)
    }

    private val boat: EntityBoat?
        get() = if (mc.player.getRidingEntity() != null && mc.player.getRidingEntity() is EntityBoat && (mc.player.getRidingEntity() as EntityBoat).controllingPassenger == mc.player) mc.player.getRidingEntity() as EntityBoat? else null

    private fun moveForward(entity: Entity?, speed: Double) {
        if (entity != null) {
            val movementInput = mc.player.movementInput
            var forward = movementInput.moveForward.toDouble()
            var strafe = movementInput.moveStrafe.toDouble()
            val movingForward = forward != 0.0
            val movingStrafe = strafe != 0.0
            var yaw = mc.player.rotationYaw

            if (!movingForward && !movingStrafe) {
                setEntitySpeed(entity, 0.0, 0.0)
            } else {
                if (forward != 0.0) {
                    if (strafe > 0.0) {
                        yaw += (if (forward > 0.0) -45 else 45).toFloat()
                    } else if (strafe < 0.0) {
                        yaw += (if (forward > 0.0) 45 else -45).toFloat()
                    }
                    strafe = 0.0
                    forward = if (forward > 0.0) {
                        1.0
                    } else {
                        -1.0
                    }
                }
                var motX = forward * speed * cos(Math.toRadians(yaw + 90.0f.toDouble())) + strafe * speed * sin(Math.toRadians(yaw + 90.0f.toDouble()))
                var motZ = forward * speed * sin(Math.toRadians(yaw + 90.0f.toDouble())) - strafe * speed * cos(Math.toRadians(yaw + 90.0f.toDouble()))
                if (isBorderingChunk(entity, motX, motZ)) {
                    motZ = 0.0
                    motX = motZ
                }
                setEntitySpeed(entity, motX, motZ)
            }
        }
    }

    private fun setEntitySpeed(entity: Entity, motX: Double, motZ: Double) {
        entity.motionX = motX
        entity.motionZ = motZ
    }

    private fun isBorderingChunk(entity: Entity, motX: Double, motZ: Double): Boolean {
        return antiStuck.value && mc.world.getChunk((entity.posX + motX).toInt() shr 4, (entity.posZ + motZ).toInt() shr 4) is EmptyChunk
    }

    companion object {
        private val opacity = Settings.f("BoatOpacity", .5f)

        @JvmStatic
        fun getOpacity(): Float {
            return opacity.value
        }
    }

    init {
        register(opacity)
    }
}