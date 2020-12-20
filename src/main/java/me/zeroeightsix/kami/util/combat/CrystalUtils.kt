package me.zeroeightsix.kami.util.combat

import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.math.VectorUtils
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
import kotlin.math.max

object CrystalUtils {
    private val mc = Wrapper.minecraft

    /* Position Finding */
    fun getPlacePos(target: EntityLivingBase?, center: Entity?, radius: Float): List<BlockPos> {
        if (center == null) return emptyList()
        val centerPos = if (center == mc.player) center.getPositionEyes(1f) else center.positionVector
        return VectorUtils.getBlockPosInSphere(centerPos, radius).filter { canPlace(it, target) }
    }

    fun getCrystalList(center: Vec3d, range: Float): ArrayList<EntityEnderCrystal> {
        val crystalList = ArrayList<EntityEnderCrystal>()
        mc.world?.loadedEntityList?.let {
            for (entity in ArrayList(it)) {
                if (entity !is EntityEnderCrystal) continue
                if (entity.isDead) continue
                if (center.distanceTo(entity.positionVector) > range) continue
                crystalList.add(entity)
            }
        }
        return crystalList
    }

    /** Checks colliding with blocks and given entity */
    fun canPlace(pos: BlockPos, entity: EntityLivingBase? = null): Boolean {
        val placeBB = getCrystalPlacingBB(pos.up())
        return canPlaceOn(pos)
            && (entity == null || !placeBB.intersects(entity.entityBoundingBox))
            && mc.world?.checkBlockCollision(placeBB) == false
    }

    /** Checks if the block is valid for placing crystal */
    fun canPlaceOn(pos: BlockPos): Boolean {
        val block = mc.world?.getBlockState(pos)?.block
        return block == Blocks.BEDROCK || block == Blocks.OBSIDIAN
    }

    private fun getCrystalPlacingBB(pos: BlockPos) = AxisAlignedBB(-0.5, 0.0, -0.5, 0.5, 2.0, 0.5).offset(Vec3d(pos).add(0.5, 0.0, 0.5))

    fun getCrystalBB(pos: BlockPos): AxisAlignedBB = AxisAlignedBB(-1.0, 0.0, -1.0, 1.0, 2.0, 1.0).offset(Vec3d(pos).add(0.5, 0.0, 0.5))

    /** Checks colliding with all entity */
    fun canPlaceCollide(pos: BlockPos): Boolean {
        val placingBB = getCrystalPlacingBB(pos.up())
        return mc.world?.let { world ->
            world.getEntitiesWithinAABBExcludingEntity(null, placingBB).firstOrNull { !it.isDead } == null
        } ?: false
    }
    /* End of position finding */

    /* Damage calculation */
    fun calcDamage(crystal: EntityEnderCrystal, entity: EntityLivingBase, entityPos: Vec3d? = entity.positionVector, entityBB: AxisAlignedBB? = entity.entityBoundingBox) =
        calcDamage(crystal.positionVector, entity, entityPos, entityBB)

    fun calcDamage(pos: BlockPos, entity: EntityLivingBase, entityPos: Vec3d? = entity.positionVector, entityBB: AxisAlignedBB? = entity.entityBoundingBox) =
        calcDamage(Vec3d(pos).add(0.5, 1.0, 0.5), entity, entityPos, entityBB)

    fun calcDamage(pos: Vec3d, entity: EntityLivingBase, entityPos: Vec3d? = entity.positionVector, entityBB: AxisAlignedBB? = entity.entityBoundingBox): Float {
        // Return 0 directly if entity is a player and in creative mode
        if (entity is EntityPlayer && entity.isCreative) return 0.0f

        // Calculate raw damage (based on blocks and distance)
        var damage = calcRawDamage(pos, entityPos ?: entity.positionVector, entityBB ?: entity.entityBoundingBox)

        // Calculate damage after armor, enchantment, resistance effect absorption
        damage = CombatUtils.calcDamage(entity, damage, getDamageSource(pos) ?: return 0.0f)

        // Multiply the damage based on difficulty if the entity is player
        if (entity is EntityPlayer) damage *= mc.world.difficulty.id * 0.5f

        // The damage cannot be less than 0 lol
        return max(damage, 0.0f)
    }

    private fun calcRawDamage(pos: Vec3d, entityPos: Vec3d, entityBB: AxisAlignedBB): Float {
        val distance = pos.distanceTo(entityPos)
        val v = (1.0 - (distance / 12.0)) * (mc.world?.getBlockDensity(pos, entityBB) ?: return 0.0f)
        return ((v * v + v) / 2.0 * 84.0 + 1.0).toFloat()
    }

    private fun getDamageSource(damagePos: Vec3d) =
        mc.world?.let { world ->
            mc.player?.let {
                DamageSource.causeExplosionDamage(Explosion(world, it, damagePos.x, damagePos.y, damagePos.z, 6F, false, true))
            }
        }
    /* End of damage calculation */
}