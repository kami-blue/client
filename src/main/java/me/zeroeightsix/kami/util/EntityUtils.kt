package me.zeroeightsix.kami.util

import com.google.gson.JsonParser
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.util.math.RotationUtils.getRotationFromVec
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
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.net.URL
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object EntityUtils {
    private val mc = Minecraft.getMinecraft()

    @JvmStatic
    fun mobTypeSettings(e: Entity, mobs: Boolean, passive: Boolean, neutral: Boolean, hostile: Boolean): Boolean {
        return mobs && (passive && isPassiveMob(e) || neutral && isCurrentlyNeutral(e) || hostile && isMobAggressive(e))
    }

    @JvmStatic
    fun isPassiveMob(e: Entity?): Boolean { // TODO: usages of this
        return e is EntityAnimal || e is EntityAgeable || e is EntityTameable || e is EntityAmbientCreature || e is EntitySquid
    }

    @JvmStatic
    fun isLiving(e: Entity?): Boolean {
        return e is EntityLivingBase
    }

    @JvmStatic
    fun isFakeLocalPlayer(entity: Entity?): Boolean {
        return entity != null && entity.getEntityId() == -100 && Wrapper.player !== entity
    }

    /**
     * Find the entities interpolated amount
     */
    fun getInterpolatedAmount(entity: Entity, ticks: Float): Vec3d {
        return Vec3d(
                (entity.posX - entity.lastTickPosX) * ticks,
                (entity.posY - entity.lastTickPosY) * ticks,
                (entity.posZ - entity.lastTickPosZ) * ticks
        )
    }

    fun isMobAggressive(entity: Entity): Boolean {
        if (entity is EntityPigZombie) {
            // arms raised = aggressive, angry = either game or we have set the anger cooldown
            if (entity.isArmsRaised || entity.isAngry) {
                return true
            }
        } else if (entity is EntityWolf) {
            return entity.isAngry &&
                    Wrapper.player != entity.owner
        } else if (entity is EntityEnderman) {
            return entity.isScreaming
        } else if (entity is EntityIronGolem) {
            return entity.revengeTarget == null
        }
        return isHostileMob(entity)
    }

    /**
     * If the mob is currently neutral but not aggressive
     */
    @JvmStatic
    fun isCurrentlyNeutral(entity: Entity): Boolean {
        return isNeutralMob(entity) && !isMobAggressive(entity)
    }

    /**
     * If the mob by default wont attack the player, but will if the player attacks it
     */
    fun isNeutralMob(entity: Entity?): Boolean {
        return entity is EntityPigZombie ||
                entity is EntityWolf ||
                entity is EntityEnderman ||
                entity is EntityIronGolem
    }

    /**
     * If the mob is friendly
     */
    fun isFriendlyMob(entity: Entity): Boolean {
        return entity.isCreatureType(EnumCreatureType.CREATURE, false) && !isNeutralMob(entity) ||
                entity.isCreatureType(EnumCreatureType.AMBIENT, false) ||
                entity is EntityVillager
    }

    /**
     * If the mob is hostile
     */
    fun isHostileMob(entity: Entity): Boolean {
        return entity.isCreatureType(EnumCreatureType.MONSTER, false) && !isNeutralMob(entity)
    }

    /**
     * Find the entities interpolated position
     */
    @JvmStatic
    fun getInterpolatedPos(entity: Entity, ticks: Float): Vec3d {
        return Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(getInterpolatedAmount(entity, ticks))
    }

    @JvmStatic
    fun getInterpolatedRenderPos(entity: Entity, ticks: Float): Vec3d {
        return getInterpolatedPos(entity, ticks).subtract(Wrapper.minecraft.getRenderManager().renderPosX, Wrapper.minecraft.getRenderManager().renderPosY, Wrapper.minecraft.getRenderManager().renderPosZ)
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

    fun isDrivenByPlayer(entityIn: Entity?): Boolean {
        return mc.player != null && entityIn != null && entityIn == mc.player.getRidingEntity()
    }

    fun isAboveWater(entity: Entity?): Boolean {
        return isAboveWater(entity, false)
    }

    fun isAboveWater(entity: Entity?, packet: Boolean): Boolean {
        if (entity == null) return false
        val y = entity.posY - if (packet) 0.03 else if (isPlayer(entity)) 0.2 else 0.5 // increasing this seems to flag more in NCP but needs to be increased so the player lands on solid water
        for (x in MathHelper.floor(entity.posX) until MathHelper.ceil(entity.posX)) for (z in MathHelper.floor(entity.posZ) until MathHelper.ceil(entity.posZ)) {
            val pos = BlockPos(x, MathHelper.floor(y), z)
            if (mc.world.getBlockState(pos).block is BlockLiquid) return true
        }
        return false
    }

    fun isPlayer(entity: Entity?): Boolean {
        return entity is EntityPlayer
    }

    fun getRelativeX(yaw: Float): Double {
        return sin(Math.toRadians(-yaw.toDouble()))
    }

    fun getRelativeZ(yaw: Float): Double {
        return cos(Math.toRadians(yaw.toDouble()))
    }

    fun resetHSpeed(speed: Float, player: EntityPlayer) {
        val vec3d = Vec3d(player.motionX, player.motionY, player.motionZ)
        val yaw = Math.toRadians(getRotationFromVec(vec3d).x)
        player.motionX = sin(-yaw) * speed
        player.motionZ = cos(yaw) * speed
    }

    fun getSpeed(entity: Entity): Float {
        return sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ).toFloat()
    }

    /**
     * Gets the MC username tied to a given UUID.
     *
     * @param uuid UUID to get name from.
     * @return The name tied to the UUID.
     */
    @JvmStatic
    fun getNameFromUUID(uuid: String): String? {
        try {
            KamiMod.log.info("Attempting to get name from UUID: $uuid")
            val jsonUrl = IOUtils.toString(URL("https://api.mojang.com/user/profiles/" + uuid.replace("-", "") + "/names"))
            val parser = JsonParser()
            return parser.parse(jsonUrl).asJsonArray[parser.parse(jsonUrl).asJsonArray.size() - 1].asJsonObject["name"].toString()
        } catch (ex: IOException) {
            KamiMod.log.error(ex.stackTrace)
            KamiMod.log.error("Failed to get username from UUID due to an exception. Maybe your internet is being the big gay? Somehow?")
        }
        return null
    }


    enum class EntityPriority {
        DISTANCE, HEALTH
    }

    fun getPrioritizedTarget(targetList: ArrayList<EntityLivingBase>, priority: EntityPriority): EntityLivingBase {
        var entity = targetList[0]
        when (priority) {
            EntityPriority.DISTANCE -> {
                var distance = mc.player.getDistance(targetList[0])
                for (i in targetList.indices) {
                    val currentDistance = mc.player.getDistance(targetList[i])
                    if (currentDistance < distance) {
                        distance = currentDistance
                        entity = targetList[i]
                    }
                }
            }
            EntityPriority.HEALTH -> {
                var health = targetList[0].health
                for (i in targetList.indices) {
                    val currentHealth = targetList[i].health
                    if (currentHealth < health) {
                        health = currentHealth
                        entity = targetList[i]
                    }
                }
            }
        }
        return entity
    }

    fun getTargetList(player: Array<Boolean>, mobs: Array<Boolean>, invisible: Boolean, range: Float): ArrayList<EntityLivingBase> {
        if (mc.world.loadedEntityList == null) return ArrayList()
        val entityList = ArrayList<EntityLivingBase>()
        for (entity in mc.world.loadedEntityList) {
            /* Entity type check */
            if (entity !is EntityLivingBase) continue
            if (entity.name == mc.player.name) continue
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
    fun playerTypeCheck(player: EntityPlayer, friend: Boolean, sleeping: Boolean): Boolean {
        return (friend || !Friends.isFriend(player.name)) && (sleeping || !player.isPlayerSleeping)
    }

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
        return mc.world.rayTraceBlocks(Vec3d(mc.player.posX, mc.player.posY + mc.player.eyeHeight, mc.player.posZ), Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ), false, true, false) == null
    }

    fun getDroppedItems(itemId: Int, range: Float): Array<Entity>? {
        val entityList = arrayListOf<Entity>()
        for (currentEntity in mc.world.loadedEntityList) {
            if (currentEntity.getDistance(mc.player) > range) continue /* Entities within specified  blocks radius */
            if (currentEntity !is EntityItem) continue /* Entites that are dropped item */
            if (Item.getIdFromItem(currentEntity.item.getItem()) != itemId) continue /* Dropped items that are has give item id */
            entityList.add(currentEntity)
        }
        return if (entityList.isNotEmpty()) entityList.toTypedArray() else null
    }

    fun getDroppedItem(itemId: Int, range: Float): BlockPos? {
        val entityList = getDroppedItems(itemId, range)
        if (entityList != null) {
            for (dist in 1..ceil(range).toInt()) for (currentEntity in entityList) {
                if (currentEntity.getDistance(mc.player) > dist) continue
                return currentEntity.position
            }
        }
        return null
    }

    fun getRidingEntity(): Entity? {
        return mc.player?.let {
            mc.player.ridingEntity
        }
    }
}