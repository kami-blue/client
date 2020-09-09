package me.zeroeightsix.kami.util.combat

import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.MobEffects
import net.minecraft.util.DamageSource
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Explosion
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow

/**
 * @author Xiaro
 *
 * Created by Xiaro on 08/09/20
 */
object CrystalUtils {
    private val mc = Minecraft.getMinecraft()

    /* Position Finding */
    @JvmStatic
    fun getPlacePos(target: EntityLivingBase?, center: Entity?, radius: Float): Map<Float, BlockPos> {
        if (target == null || center == null) return emptyMap()
        val squaredRadius = radius.pow(2).toDouble()
        val damagePosMap = HashMap<Float, BlockPos>()
        for (x in getAxisRange(center.posX, radius)) for (y in getAxisRange(center.posY, radius)) for (z in getAxisRange(center.posZ, radius)) {
            /* Valid position check */
            val blockPos = BlockPos(x, y, z)
            if (center.getDistanceSq(blockPos) > squaredRadius) continue
            if (!canPlace(blockPos, target)) continue

            /* Damage calculation */
            val damage = calcExplosionDamage(blockPos, target)
            damagePosMap[damage] = blockPos
        }
        return damagePosMap
    }

    @JvmStatic
    fun getAxisRange(d1: Double, d2: Float): IntRange {
        return IntRange(floor(d1 - d2).toInt(), ceil(d1 + d2).toInt())
    }

    @JvmStatic
    fun getCrystalList(range: Float): ArrayList<EntityEnderCrystal> {
        val crystalList = ArrayList<EntityEnderCrystal>()
        val entityList = ArrayList<Entity>()
        synchronized(mc.world.loadedEntityList) {
            entityList.addAll(mc.world.loadedEntityList)
        }
        for (entity in entityList) {
            if (entity.isDead) continue
            if (entity !is EntityEnderCrystal) continue
            if (mc.player.getDistance(entity) > range) continue
            crystalList.add(entity)
        }
        return crystalList
    }

    /* Checks colliding with blocks and given entity only */
    @JvmStatic
    fun canPlace(blockPos: BlockPos, entity: Entity): Boolean {
        val entityBB = entity.boundingBox
        val placingBB = getCrystalPlacingBB(blockPos.up())
        return !entityBB.intersects(placingBB)
                && (mc.world.getBlockState(blockPos).block == Blocks.BEDROCK
                || mc.world.getBlockState(blockPos).block == Blocks.OBSIDIAN)
                && !mc.world.checkBlockCollision(placingBB)
    }

    @JvmStatic
    private fun getCrystalPlacingBB(blockPos: BlockPos): AxisAlignedBB {
        return crystalPlacingBB.offset(Vec3d(blockPos).add(0.5, 0.0, 0.5))
    }

    private val crystalPlacingBB: AxisAlignedBB get() = AxisAlignedBB(-0.5, 0.0, -0.5, 0.5, 2.0, 0.5)

    /* Checks colliding with all entity */
    @JvmStatic
    fun canPlaceCollide(blockPos: BlockPos): Boolean {
        val placingBB = getCrystalPlacingBB(blockPos.up())
        return mc.world.checkNoEntityCollision(placingBB)
    }
    /* End of position finding */

    /* Damage calculation */
    @JvmStatic
    fun calcExplosionDamage(crystal: EntityEnderCrystal, entity: EntityLivingBase, calcBlastReduction: Boolean = true): Float {
        val pos = crystal.positionVector
        val rawDamage = calcExplosionDamage(pos, entity)
        return if (!calcBlastReduction) rawDamage
        else calcBlastReduction(rawDamage, pos, entity)
    }

    @JvmStatic
    fun calcExplosionDamage(blockPos: BlockPos, entity: EntityLivingBase, calcBlastReduction: Boolean = true): Float {
        val pos = Vec3d(blockPos).add(0.5, 1.0, 0.5)
        val rawDamage = calcExplosionDamage(pos, entity)
        return if (!calcBlastReduction) rawDamage
        else calcBlastReduction(rawDamage, pos, entity)
    }

    @JvmStatic
    private fun calcBlastReduction(damageIn: Float, damagePos: Vec3d, entity: EntityLivingBase): Float {
        if (entity is EntityPlayer) {
            var damage = CombatUtils.calcDamage(entity, damageIn) * CombatUtils.getProtectionModifier(entity, getDamageSource(damagePos))
            if (entity.isPotionActive(MobEffects.RESISTANCE)) damage *= 0.8f
            return max(damage * getDamageMultiplier(), 0.0f)
        }
        return CombatUtils.calcDamage(entity, damageIn, false)
    }

    @JvmStatic
    private fun getDamageSource(damagePos: Vec3d): DamageSource {
        return DamageSource.causeExplosionDamage(Explosion(mc.world, mc.player, damagePos.x, damagePos.y, damagePos.z, 6F, false, true))
    }

    @JvmStatic
    private fun getDamageMultiplier(): Float {
        return mc.world.difficulty.id * 0.5f
    }

    @JvmStatic
    private fun calcExplosionDamage(pos: Vec3d, entity: Entity): Float {
        val distance = pos.distanceTo(entity.positionVector)
        val v = (1.0 - (distance / 12.0)) * entity.world.getBlockDensity(pos, entity.boundingBox)
        return ((v * v + v) / 2.0 * 84.0 + 1.0).toFloat()
    }
    /* End of damage calculation */
}