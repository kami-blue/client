package me.zeroeightsix.kami.util

import com.google.gson.JsonParser
import me.zeroeightsix.kami.KamiMod
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
import kotlin.math.*

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
        return entity != null && entity.getEntityId() == -100 && Wrapper.getPlayer() !== entity
    }

    /**
     * Find the entities interpolated amount
     */
    fun getInterpolatedAmount(entity: Entity, x: Double, y: Double, z: Double): Vec3d {
        return Vec3d(
                (entity.posX - entity.lastTickPosX) * x,
                (entity.posY - entity.lastTickPosY) * y,
                (entity.posZ - entity.lastTickPosZ) * z
        )
    }

    fun getInterpolatedAmount(entity: Entity, vec: Vec3d): Vec3d {
        return getInterpolatedAmount(entity, vec.x, vec.y, vec.z)
    }

    fun getInterpolatedAmount(entity: Entity, ticks: Double): Vec3d {
        return getInterpolatedAmount(entity, ticks, ticks, ticks)
    }

    fun isMobAggressive(entity: Entity): Boolean {
        if (entity is EntityPigZombie) {
            // arms raised = aggressive, angry = either game or we have set the anger cooldown
            if (entity.isArmsRaised || entity.isAngry) {
                return true
            }
        } else if (entity is EntityWolf) {
            return entity.isAngry &&
                    Wrapper.getPlayer() != entity.owner
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
        return Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(getInterpolatedAmount(entity, ticks.toDouble()))
    }

    @JvmStatic
    fun getInterpolatedRenderPos(entity: Entity, ticks: Float): Vec3d {
        return getInterpolatedPos(entity, ticks).subtract(Wrapper.getMinecraft().getRenderManager().renderPosX, Wrapper.getMinecraft().getRenderManager().renderPosY, Wrapper.getMinecraft().getRenderManager().renderPosZ)
    }

    fun isInWater(entity: Entity?): Boolean {
        if (entity == null) return false
        val y = entity.posY + 0.01
        for (x in MathHelper.floor(entity.posX) until MathHelper.ceil(entity.posX)) for (z in MathHelper.floor(entity.posZ) until MathHelper.ceil(entity.posZ)) {
            val pos = BlockPos(x, y.toInt(), z)
            if (Wrapper.getWorld().getBlockState(pos).block is BlockLiquid) return true
        }
        return false
    }

    fun isDrivenByPlayer(entityIn: Entity?): Boolean {
        return Wrapper.getPlayer() != null && entityIn != null && entityIn == Wrapper.getPlayer().getRidingEntity()
    }

    fun isAboveWater(entity: Entity?): Boolean {
        return isAboveWater(entity, false)
    }

    fun isAboveWater(entity: Entity?, packet: Boolean): Boolean {
        if (entity == null) return false
        val y = entity.posY - if (packet) 0.03 else if (isPlayer(entity)) 0.2 else 0.5 // increasing this seems to flag more in NCP but needs to be increased so the player lands on solid water
        for (x in MathHelper.floor(entity.posX) until MathHelper.ceil(entity.posX)) for (z in MathHelper.floor(entity.posZ) until MathHelper.ceil(entity.posZ)) {
            val pos = BlockPos(x, MathHelper.floor(y), z)
            if (Wrapper.getWorld().getBlockState(pos).block is BlockLiquid) return true
        }
        return false
    }

    @JvmStatic
    fun calculateLookAt(px: Double, py: Double, pz: Double, me: EntityPlayer): DoubleArray {
        var dirx = me.posX - px
        var diry = me.posY - py
        var dirz = me.posZ - pz
        val len = sqrt(dirx * dirx + diry * diry + dirz * dirz)
        dirx /= len
        diry /= len
        dirz /= len
        var pitch = asin(diry)
        var yaw = atan2(dirz, dirx)

        // to degree
        pitch = Math.toDegrees(pitch)
        yaw = Math.toDegrees(yaw) + 90.0
        return doubleArrayOf(yaw, pitch)
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

    fun getRotationFromVec3d(vec3d: Vec3d): Array<Double> {
        var x = vec3d.x
        var y = vec3d.y
        var z = vec3d.z
        val speed = sqrt(x * x + y * y + z * z)

        x /= speed
        y /= speed
        z /= speed

        val yaw = Math.toDegrees(atan2(z, x)) - 90.0
        val pitch = Math.toDegrees(-asin(y))

        return arrayOf(yaw, pitch)
    }

    fun getRotationFromBlockPos(posFrom: BlockPos, posTo: BlockPos): Array<Double> {
        val delta = doubleArrayOf((posFrom.x - posTo.x).toDouble(), (posFrom.y - posTo.y).toDouble(), (posFrom.z - posTo.z).toDouble())
        val yaw = Math.toDegrees(atan2(delta[0], -delta[2]))
        val dist = sqrt(delta[0] * delta[0] + delta[2] * delta[2])
        val pitch = Math.toDegrees(atan2(delta[1], dist))
        return arrayOf(yaw, pitch)
    }

    fun resetHSpeed(speed: Float, player: EntityPlayer) {
        val vec3d = Vec3d(player.motionX, player.motionY, player.motionZ)
        val yaw = Math.toRadians(getRotationFromVec3d(vec3d)[0])
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

    fun getPrioritizedTarget(targetList: Array<Entity>, priority: EntityPriority): Entity {
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
                var health = (targetList[0] as EntityLivingBase).health
                for (i in targetList.indices) {
                    val currentHealth = (targetList[i] as EntityLivingBase).health
                    if (currentHealth < health) {
                        health = currentHealth
                        entity = targetList[i]
                    }
                }
            }
        }
        return entity
    }

    fun getTargetList(player: Array<Boolean>, mobs: Array<Boolean>, ignoreWalls: Boolean, immune: Boolean, range: Float): Array<Entity> {
        val entityList = ArrayList<Entity>()
        for (entity in mc.world.loadedEntityList) {
            /* Entity type check */
            if (!isLiving(entity)) continue
            if (entity == mc.player) continue
            if (entity is EntityPlayer) {
                if (!player[0]) continue
                if (!player[1] && Friends.isFriend(entity.name)) continue
                if (!player[2] && entity.isPlayerSleeping) continue
            } else if (!mobTypeSettings(entity, mobs[0], mobs[1], mobs[2], mobs[3])) continue

            if (mc.player.isRiding && entity == mc.player.ridingEntity) continue // Riding entity check
            if (mc.player.getDistance(entity) > range) continue // Distance check
            if ((entity as EntityLivingBase).health <= 0) continue // HP check
            if (!ignoreWalls && !mc.player.canEntityBeSeen(entity) && !canEntityFeetBeSeen(entity)) continue  // If walls is on & you can't see the feet or head of the target, skip. 2 raytraces needed
            if (immune && entity.hurtTime != 0) continue //Hurt time check
            entityList.add(entity)
        }
        return entityList.toTypedArray()
    }

    fun canEntityFeetBeSeen(entityIn: Entity): Boolean {
        return mc.world.rayTraceBlocks(Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ), false, true, false) == null
    }

    fun faceEntity(entity: Entity) {
        val diffX = entity.posX - mc.player.posX
        val diffZ = entity.posZ - mc.player.posZ
        val diffY = mc.player.posY + mc.player.getEyeHeight().toDouble() - (entity.posY + entity.eyeHeight.toDouble())

        val xz = MathHelper.sqrt(diffX * diffX + diffZ * diffZ).toDouble()
        val yaw = MathsUtils.normalizeAngle(atan2(diffZ, diffX) * 180.0 / Math.PI - 90.0f).toFloat()
        val pitch = MathsUtils.normalizeAngle(-atan2(diffY, xz) * 180.0 / Math.PI).toFloat()

        mc.player.rotationYaw = yaw
        mc.player.rotationPitch = -pitch
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