package org.kamiblue.client.util.combat

import net.minecraft.block.material.Material
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
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.util.Wrapper
import org.kamiblue.client.util.math.VectorUtils
import org.kamiblue.client.util.math.VectorUtils.distanceTo
import kotlin.math.max

object CrystalUtils {
    private val mc = Wrapper.minecraft

    /* Position Finding */
    fun SafeClientEvent.getPlacePos(target: EntityLivingBase?, center: Entity?, radius: Float): List<BlockPos> {
        if (center == null) return emptyList()
        val centerPos = if (center == player) center.getPositionEyes(1f) else center.positionVector
        return VectorUtils.getBlockPosInSphere(centerPos, radius).filter { canPlace(it, target) }
    }

    fun SafeClientEvent.getCrystalList(center: Vec3d, range: Float): List<EntityEnderCrystal> =
        world.loadedEntityList.toList()
            .filterIsInstance<EntityEnderCrystal>()
            .filter { entity -> entity.isEntityAlive && entity.distanceTo(center) <= range }

    /** Checks colliding with blocks and given entity */
    fun SafeClientEvent.canPlace(pos: BlockPos, entity: EntityLivingBase? = null): Boolean {
        val posUp1 = pos.up()
        val posUp2 = posUp1.up()

        return canPlaceOn(pos)
            && (entity == null || !getCrystalPlacingBB(posUp1).intersects(entity.entityBoundingBox))
            && mc.world?.let {
            isValidMaterial(it.getBlockState(posUp1).material)
                    && isValidMaterial(it.getBlockState(posUp2).material)
        } ?: false
    }

    /** Checks if the block is valid for placing crystal */
    fun SafeClientEvent.canPlaceOn(pos: BlockPos): Boolean {
        val block = mc.world?.getBlockState(pos)?.block
        return block == Blocks.BEDROCK || block == Blocks.OBSIDIAN
    }

    private fun isValidMaterial(material: Material) =
        !material.isLiquid && material.isReplaceable

    private fun getCrystalPlacingBB(pos: BlockPos) =
        AxisAlignedBB(
            pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
            pos.x + 1.0, pos.y + 2.0, pos.z + 1.0
        )

    fun getCrystalBB(pos: BlockPos): AxisAlignedBB =
        AxisAlignedBB(
            pos.x - 0.5, pos.y - 0.5, pos.z - 0.5,
            pos.x + 1.5, pos.y + 2.0, pos.z + 1.5
        )

    /** Checks colliding with All Entities */
    fun SafeClientEvent.canPlaceCollide(pos: BlockPos): Boolean {
        val placingBB = getCrystalPlacingBB(pos.up())
        return mc.world?.let { world ->
            world.getEntitiesWithinAABBExcludingEntity(null, placingBB).all {
                it.isDead || it is EntityLivingBase && it.health <= 0.0f
            }
        } ?: false
    }
    /* End of position finding */

    /* Damage calculation */
    fun SafeClientEvent.calcCrystalDamage(crystal: EntityEnderCrystal, entity: EntityLivingBase, entityPos: Vec3d? = entity.positionVector, entityBB: AxisAlignedBB? = entity.entityBoundingBox) =
        calcCrystalDamage(crystal.positionVector, entity, entityPos, entityBB)

    fun SafeClientEvent.calcCrystalDamage(pos: BlockPos, entity: EntityLivingBase, entityPos: Vec3d? = entity.positionVector, entityBB: AxisAlignedBB? = entity.entityBoundingBox) =
        calcCrystalDamage(Vec3d(pos).add(0.5, 1.0, 0.5), entity, entityPos, entityBB)

    fun SafeClientEvent.calcCrystalDamage(pos: Vec3d, entity: EntityLivingBase, entityPos: Vec3d? = entity.positionVector, entityBB: AxisAlignedBB? = entity.entityBoundingBox): Float {
        // Return 0 directly if entity is a player and in creative mode
        if (entity is EntityPlayer && entity.isCreative) return 0.0f

        // Calculate raw damage (based on blocks and distance)
        var damage = calcRawDamage(pos, entityPos ?: entity.positionVector, entityBB ?: entity.entityBoundingBox)

        // Calculate damage after armor, enchantment, resistance effect absorption
        damage = CombatUtils.calcDamage(entity, damage, getDamageSource(pos))

        // Multiply the damage based on difficulty if the entity is player
        if (entity is EntityPlayer) damage *= world.difficulty.id * 0.5f

        // The damage cannot be less than 0 lol
        return max(damage, 0.0f)
    }

    private fun SafeClientEvent.calcRawDamage(pos: Vec3d, entityPos: Vec3d, entityBB: AxisAlignedBB): Float {
        val distance = pos.distanceTo(entityPos)
        val v = (1.0 - (distance / 12.0)) * world.getBlockDensity(pos, entityBB)
        return ((v * v + v) / 2.0 * 84.0 + 1.0).toFloat()
    }

    private fun SafeClientEvent.getDamageSource(damagePos: Vec3d) =
        DamageSource.causeExplosionDamage(Explosion(world, player, damagePos.x, damagePos.y, damagePos.z, 6F, false, true))
    /* End of damage calculation */
}