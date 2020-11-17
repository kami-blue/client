package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
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
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

object EntityUtils {
    private val mc = Minecraft.getMinecraft()

    @JvmStatic
    fun mobTypeSettings(entity: Entity, mobs: Boolean, passive: Boolean, neutral: Boolean, hostile: Boolean): Boolean {
        return mobs && (passive && isPassiveMob(entity) || neutral && isCurrentlyNeutral(entity) || hostile && isMobAggressive(entity))
    }

    @JvmStatic
    fun isPassiveMob(entity: Entity?) = entity is EntityAnimal || entity is EntityAgeable || entity is EntityTameable || entity is EntityAmbientCreature || entity is EntitySquid

    val Entity.prevPosVector get() = Vec3d(this.prevPosX, this.prevPosY, this.prevPosZ)

    /**
     * Find the entities interpolated position
     */
    @JvmStatic
    fun getInterpolatedPos(entity: Entity, ticks: Float): Vec3d = entity.prevPosVector.add(getInterpolatedAmount(entity, ticks))

    /**
     * Find the entities interpolated amount
     */
    fun getInterpolatedAmount(entity: Entity, ticks: Float): Vec3d = entity.positionVector.subtract(entity.prevPosVector).scale(ticks.toDouble())

    /**
     * If the mob is currently neutral but not aggressive
     */
    fun isCurrentlyNeutral(entity: Entity) = isNeutralMob(entity) && !isMobAggressive(entity)

    /**
     * If the mob by default wont attack the player, but will if the player attacks it
     */
    private fun isNeutralMob(entity: Entity) = entity is EntityPigZombie || entity is EntityWolf || entity is EntityEnderman || entity is EntityIronGolem

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
            isHostileMob(entity)
        }
    }

    /**
     * If the mob is hostile
     */
    private fun isHostileMob(entity: Entity): Boolean {
        return entity.isCreatureType(EnumCreatureType.MONSTER, false) && !isNeutralMob(entity)
    }

    fun isInWater(entity: Entity?): Boolean {
        if (entity == null) return false
        val y = entity.posY + 0.01
        for (x in MathHelper.floor(entity.posX) until MathHelper.ceil(entity.posX)) for (z in MathHelper.floor(entity.posZ) until MathHelper.ceil(entity.posZ)) {
            val pos = BlockPos(x, y.toInt(), z)
            if (mc.world.getBlockState(pos).block is BlockLiquid) return true
        }
        return false
    }

    fun isDrivenByPlayer(entity: Entity) = mc.player != null && entity == mc.player.getRidingEntity()

    fun isAboveWater(entity: Entity?) = isAboveWater(entity, false)

    fun isAboveWater(entity: Entity?, packet: Boolean): Boolean {
        if (entity == null) return false
        val y = entity.posY - if (packet) 0.03 else if (entity is EntityPlayer) 0.2 else 0.5 // increasing this seems to flag more in NCP but needs to be increased so the player lands on solid water
        for (x in MathHelper.floor(entity.posX) until MathHelper.ceil(entity.posX)) for (z in MathHelper.floor(entity.posZ) until MathHelper.ceil(entity.posZ)) {
            val pos = BlockPos(x, MathHelper.floor(y), z)
            if (mc.world.getBlockState(pos).block is BlockLiquid) return true
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

    @JvmStatic
    fun playerTypeCheck(player: EntityPlayer, friend: Boolean, sleeping: Boolean) = (friend || !FriendManager.isFriend(player.name)) && (sleeping || !player.isPlayerSleeping)

    /**
     * Ray tracing the 8 vertex of the entity bounding box
     *
     * @return [Vec3d] of the visible vertex, null if none
     */
    fun canEntityHitboxBeSeen(entity: Entity): Vec3d? {
        val playerPos = mc.player.positionVector.add(0.0, mc.player.eyeHeight.toDouble(), 0.0)
        val box = entity.boundingBox
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

    fun getDroppedItems(itemId: Int, range: Float): ArrayList<Entity>? {
        val entityList = ArrayList<Entity>()
        for (currentEntity in mc.world.loadedEntityList) {
            if (currentEntity.getDistance(mc.player) > range) continue /* Entities within specified  blocks radius */
            if (currentEntity !is EntityItem) continue /* Entites that are dropped item */
            if (Item.getIdFromItem(currentEntity.item.getItem()) != itemId) continue /* Dropped items that are has give item id */
            entityList.add(currentEntity)
        }
        return entityList
    }

    fun getDroppedItem(itemId: Int, range: Float) = getDroppedItems(itemId, range)?.minBy { mc.player.getDistance(it) }?.positionVector?.toBlockPos()
}