package me.zeroeightsix.kami.util.combat

import me.zeroeightsix.kami.util.math.VectorUtils
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.util.DamageSource
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Explosion
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

object CrystalUtils {
    private val mc = Minecraft.getMinecraft()

    /* Position Finding */
    @JvmStatic
    fun getPlacePos(target: EntityLivingBase?, center: Entity?, radius: Float): Map<Float, BlockPos> {
        if (target == null || center == null) return emptyMap()
        val centerPos = if (center == mc.player) center.getPositionEyes(1f) else center.positionVector
        val posList = VectorUtils.getBlockPosInSphere(centerPos, radius)
        val damagePosMap = HashMap<Float, BlockPos>()
        for (pos in posList) {
            if (!canPlace(pos, target)) continue
            damagePosMap[calcDamage(pos, target)] = pos
        }
        return damagePosMap
    }

    fun getAxisRange(d1: Double, d2: Float): IntRange {
        return IntRange(floor(d1 - d2).toInt(), ceil(d1 + d2).toInt())
    }

    @JvmStatic
    fun getCrystalList(range: Float): ArrayList<EntityEnderCrystal> {
        return getCrystalList(mc.player.positionVector, range)
    }

    @JvmStatic
    fun getCrystalList(center: Vec3d, range: Float): ArrayList<EntityEnderCrystal> {
        val crystalList = ArrayList<EntityEnderCrystal>()
        val entityList = ArrayList<Entity>()
        synchronized(mc.world.loadedEntityList) {
            entityList.addAll(mc.world.loadedEntityList)
        }
        for (entity in entityList) {
            if (entity.isDead) continue
            if (entity !is EntityEnderCrystal) continue
            if (center.distanceTo(entity.positionVector) > range) continue
            crystalList.add(entity)
        }
        return crystalList
    }

    @JvmStatic
    fun canPlace(blockPos: BlockPos): Boolean {
        val placingBB = getCrystalPlacingBB(blockPos.up())
        return mc.world.checkNoEntityCollision(placingBB)
                && (mc.world.getBlockState(blockPos).block == Blocks.BEDROCK
                || mc.world.getBlockState(blockPos).block == Blocks.OBSIDIAN)
                && !mc.world.checkBlockCollision(placingBB)
    }

    /** Checks colliding with blocks and given entity only */
    @JvmStatic
    fun canPlace(blockPos: BlockPos, entity: Entity): Boolean {
        val entityBB = entity.boundingBox
        val placingBB = getCrystalPlacingBB(blockPos.up())
        return !entityBB.intersects(placingBB)
                && (mc.world.getBlockState(blockPos).block == Blocks.BEDROCK
                || mc.world.getBlockState(blockPos).block == Blocks.OBSIDIAN)
                && !mc.world.checkBlockCollision(placingBB)
    }

    /** Checks if the block below is valid for placing crystal */
    @JvmStatic
    fun canPlaceOn(blockPos: BlockPos) = mc.world.getBlockState(blockPos.down()).block == Blocks.BEDROCK || mc.world.getBlockState(blockPos.down()).block == Blocks.OBSIDIAN

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
    fun calcDamage(crystal: EntityEnderCrystal, entity: EntityLivingBase, calcBlastReduction: Boolean = true): Float {
        return calcDamage(crystal.positionVector, entity, calcBlastReduction)
    }

    @JvmStatic
    fun calcDamage(blockPos: BlockPos, entity: EntityLivingBase, calcBlastReduction: Boolean = true): Float {
        return calcDamage(Vec3d(blockPos).add(0.5, 1.0, 0.5), entity, calcBlastReduction)
    }

    @JvmStatic
    fun calcDamage(pos: Vec3d, entity: EntityLivingBase, calcBlastReduction: Boolean = true): Float {
        if (entity is EntityPlayer && entity.isCreative) return 0.0f // Return 0 directly if entity is a player and in creative mode
        var damage = calcRawDamage(pos, entity)
        if (calcBlastReduction) damage = CombatUtils.calcDamage(entity, damage, getDamageSource(pos))
        if (entity is EntityPlayer) damage *= getDamageMultiplier()
        return max(damage, 0f)
    }

    @JvmStatic
    private fun calcRawDamage(pos: Vec3d, entity: Entity): Float {
        val distance = pos.distanceTo(entity.positionVector)
        val v = (1.0 - (distance / 12.0)) * entity.world.getBlockDensity(pos, entity.boundingBox)
        return ((v * v + v) / 2.0 * 84.0 + 1.0).toFloat()
    }

    @JvmStatic
    private fun getDamageSource(damagePos: Vec3d): DamageSource {
        return DamageSource.causeExplosionDamage(Explosion(mc.world, mc.player, damagePos.x, damagePos.y, damagePos.z, 6F, false, true))
    }

    @JvmStatic
    private fun getDamageMultiplier(): Float {
        return mc.world.difficulty.id * 0.5f
    }
    /* End of damage calculation */
}