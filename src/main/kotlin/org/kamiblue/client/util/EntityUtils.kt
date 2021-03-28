package org.kamiblue.client.util

import net.minecraft.block.BlockLiquid
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityAgeable
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.EnumCreatureType
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.entity.monster.EntityPigZombie
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.chunk.EmptyChunk
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.manager.managers.FriendManager
import org.kamiblue.client.util.MovementUtils.calcMoveYaw
import org.kamiblue.client.util.items.id
import org.kamiblue.client.util.math.VectorUtils.toBlockPos
import org.kamiblue.commons.extension.ceilToInt
import org.kamiblue.commons.extension.floorToInt
import kotlin.math.cos
import kotlin.math.sin

object EntityUtils {
    private val mc = Minecraft.getMinecraft()

    val Entity.flooredPosition get() = BlockPos(posX.floorToInt(), posY.floorToInt(), posZ.floorToInt())
    val Entity.prevPosVector get() = Vec3d(this.prevPosX, this.prevPosY, this.prevPosZ)

    val Entity.isPassive
        get() = this is EntityAnimal
            || this is EntityAgeable
            || this is EntityTameable
            || this is EntityAmbientCreature
            || this is EntitySquid

    val Entity.isNeutral get() = isNeutralMob(this) && !isMobAggressive(this)

    val Entity.isHostile get() = isMobAggressive(this)

    val Entity.isInOrAboveLiquid get() = world.containsAnyLiquid(entityBoundingBox.grow(0.0, -1.0, 0.0).shrink(0.001))

    val EntityPlayer.isFakeOrSelf get() = this == mc.player || this == mc.renderViewEntity || this.entityId < 0

    private fun isNeutralMob(entity: Entity) = entity is EntityPigZombie
        || entity is EntityWolf
        || entity is EntityEnderman
        || entity is EntityIronGolem

    private fun isMobAggressive(entity: Entity) = when (entity) {
        is EntityPigZombie -> {
            // arms raised = aggressive, angry = either game or we have set the anger cooldown
            entity.isArmsRaised || entity.isAngry
        }
        is EntityWolf -> {
            entity.isAngry && mc.player != entity.owner
        }
        is EntityEnderman -> {
            entity.isScreaming
        }
        is EntityIronGolem -> {
            entity.revengeTarget != null
        }
        else -> {
            entity.isCreatureType(EnumCreatureType.MONSTER, false)
        }
    }

    fun mobTypeSettings(entity: Entity, mobs: Boolean, passive: Boolean, neutral: Boolean, hostile: Boolean): Boolean {
        return mobs && (passive && entity.isPassive || neutral && entity.isNeutral || hostile && entity.isHostile)
    }

    /**
     * Find the entities interpolated position
     */
    fun getInterpolatedPos(entity: Entity, ticks: Float): Vec3d = entity.prevPosVector.add(getInterpolatedAmount(entity, ticks))

    /**
     * Find the entities interpolated amount
     */
    fun getInterpolatedAmount(entity: Entity, ticks: Float): Vec3d = entity.positionVector.subtract(entity.prevPosVector).scale(ticks.toDouble())

    fun isAboveLiquid(entity: Entity) = isAboveLiquid(entity, false)

    fun isAboveLiquid(entity: Entity, packet: Boolean): Boolean {
        mc.world?.let {
            val y = entity.posY -
                // increasing this seems to flag more in NCP but needs to be increased so the player lands on solid water
                when {
                    packet -> 0.03
                    entity is EntityPlayer -> 0.2
                    else -> 0.5
                }

            val flooredY = y.toInt()

            for (x in entity.posX.floorToInt() until entity.posX.ceilToInt()) {
                for (z in entity.posZ.floorToInt() until entity.posZ.ceilToInt()) {
                    if (it.getBlockState(BlockPos(x, flooredY, z)).block is BlockLiquid) return true
                }
            }
        }

        return false
    }

    fun getTargetList(player: Array<Boolean>, mobs: Array<Boolean>, invisible: Boolean, range: Float, ignoreSelf: Boolean = true): ArrayList<EntityLivingBase> {
        if (mc.world.loadedEntityList.isNullOrEmpty()) return ArrayList()
        val entityList = ArrayList<EntityLivingBase>()
        val clonedList = ArrayList(mc.world.loadedEntityList)
        for (entity in clonedList) {
            /* Entity type check */
            if (entity !is EntityLivingBase) continue
            if (ignoreSelf && entity.name == mc.player.name) continue
            if (entity == mc.renderViewEntity) continue
            if (entity is EntityPlayer) {
                if (!player[0]) continue
                if (!playerTypeCheck(entity, player[1], player[2])) continue
            } else if (!mobTypeSettings(entity, mobs[0], mobs[1], mobs[2], mobs[3])) continue

            if (mc.player.isRiding && entity == mc.player.ridingEntity) continue // Riding entity check
            if (mc.player.getDistance(entity) > range) continue // Distance check
            if (entity.health <= 0) continue // HP check
            if (!invisible && entity.isInvisible) continue
            entityList.add(entity)
        }
        return entityList
    }

    fun playerTypeCheck(player: EntityPlayer, friend: Boolean, sleeping: Boolean) = (friend || !FriendManager.isFriend(player.name))
        && (sleeping || !player.isPlayerSleeping)

    /**
     * Ray tracing the 8 vertex of the entity bounding box
     *
     * @return [Vec3d] of the visible vertex, null if none
     */
    fun canEntityHitboxBeSeen(entity: Entity): Vec3d? {
        val playerPos = mc.player.positionVector.add(0.0, mc.player.eyeHeight.toDouble(), 0.0)
        val box = entity.entityBoundingBox
        val xArray = arrayOf(box.minX + 0.1, box.maxX - 0.1)
        val yArray = arrayOf(box.minY + 0.1, box.maxY - 0.1)
        val zArray = arrayOf(box.minZ + 0.1, box.maxZ - 0.1)

        for (x in xArray) for (y in yArray) for (z in zArray) {
            val vertex = Vec3d(x, y, z)
            if (mc.world.rayTraceBlocks(vertex, playerPos, false, true, false) == null) return vertex
        }
        return null
    }

    fun canEntityFeetBeSeen(entityIn: Entity): Boolean {
        return mc.world.rayTraceBlocks(mc.player.getPositionEyes(1f), entityIn.positionVector, false, true, false) == null
    }

    fun SafeClientEvent.getDroppedItems(itemId: Int, range: Float): ArrayList<EntityItem> {
        val entityList = ArrayList<EntityItem>()
        for (entity in world.loadedEntityList) {
            if (entity !is EntityItem) continue /* Entites that are dropped item */
            if (entity.item.item.id != itemId) continue /* Dropped items that are has give item id */
            if (entity.getDistance(player) > range) continue /* Entities within specified  blocks radius */

            entityList.add(entity)
        }
        return entityList
    }

    fun SafeClientEvent.getDroppedItem(itemId: Int, range: Float) =
        getDroppedItems(itemId, range)
            .minByOrNull { player.getDistance(it) }
            ?.positionVector
            ?.toBlockPos()

    fun SafeClientEvent.steerEntity(entity: Entity, speed: Float, antiStuck: Boolean) {
        val yawRad = calcMoveYaw()

        val motionX = -sin(yawRad) * speed
        val motionZ = cos(yawRad) * speed

        if (MovementUtils.isInputting && !isBorderingChunk(entity, motionX, motionZ, antiStuck)) {
            entity.motionX = motionX
            entity.motionZ = motionZ
        } else {
            entity.motionX = 0.0
            entity.motionZ = 0.0
        }
    }

    private fun SafeClientEvent.isBorderingChunk(entity: Entity, motionX: Double, motionZ: Double, antiStuck: Boolean): Boolean {
        return antiStuck && world.getChunk((entity.posX + motionX).toInt() shr 4, (entity.posZ + motionZ).toInt() shr 4) is EmptyChunk
    }
}