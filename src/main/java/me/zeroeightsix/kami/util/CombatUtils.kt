package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.module.Module
import net.minecraft.client.Minecraft
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.EnumCreatureAttribute
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.potion.Potion
import net.minecraft.util.CombatRules
import net.minecraft.util.DamageSource
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Explosion
import kotlin.collections.set
import kotlin.math.*

/**
 * @author Xiaro
 *
 * Created by Xiaro on 06/08/20
 */
object CombatUtils {
    private val mc: Minecraft = Minecraft.getMinecraft()

    fun calcDamage(entity: EntityLivingBase, roundDamage: Boolean): Float {
        return calcDamage(entity, 100f, roundDamage)
    }

    fun calcDamage(entity: EntityLivingBase, damageIn: Float, roundDamage: Boolean): Float {
        val damage = CombatRules.getDamageAfterAbsorb(damageIn, entity.totalArmorValue.toFloat(), entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat())
        return if (roundDamage) round(damage) else damage
    }

    fun equipBestWeapon(hitMode: PreferWeapon = PreferWeapon.NONE) {
        var bestSlot = -1
        var maxDamage = 0.0
        for (i in 0..8) {
            val stack = Module.mc.player.inventory.getStackInSlot(i)
            if (stack.isEmpty) continue
            if (stack.getItem() !is ItemAxe && hitMode == PreferWeapon.AXE) continue
            if (stack.getItem() !is ItemSword && hitMode == PreferWeapon.SWORD) continue

            if (stack.getItem() is ItemSword && (hitMode == PreferWeapon.SWORD || hitMode == PreferWeapon.NONE)) {
                val damage = (stack.getItem() as ItemSword).attackDamage + EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED).toDouble()
                if (damage > maxDamage) {
                    maxDamage = damage
                    bestSlot = i
                }
            } else if (stack.getItem() is ItemAxe && (hitMode == PreferWeapon.AXE || hitMode == PreferWeapon.NONE)) {
                val damage = (stack.getItem() as ItemTool).attackDamage + EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED).toDouble()
                if (damage > maxDamage) {
                    maxDamage = damage
                    bestSlot = i
                }
            } else if (stack.getItem() is ItemTool) {
                val damage = (stack.getItem() as ItemTool).attackDamage + EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED).toDouble()
                if (damage > maxDamage) {
                    maxDamage = damage
                    bestSlot = i
                }
            }
        }
        if (bestSlot != -1) InventoryUtils.swapSlot(bestSlot)
    }

    enum class PreferWeapon {
        SWORD, AXE, NONE
    }

    object CrystalUtils {
        /* Position Finding */
        fun getPlacePos(target: EntityLivingBase?, center: Entity?, radius: Float, fastCalc: Boolean = false, feetLevel: Boolean = false): Map<Float, BlockPos> {
            if (target == null || center == null) return emptyMap()
            val squaredRadius = radius.pow(2).toDouble()
            val yRange = if (!fastCalc && !feetLevel) getAxisRange(center.posY, radius) else IntRange(target.posY.toInt() - 1, target.posY.toInt() - 1)
            val damagePosMap = HashMap<Float, BlockPos>()
            for (x in getAxisRange(center.posX, radius)) for (y in yRange) for (z in getAxisRange(center.posZ, radius)) {
                /* Valid position check */
                val blockPos = BlockPos(x, y, z)
                if (center.getDistanceSq(blockPos) > squaredRadius) continue
                if (!canPlace(blockPos, target)) continue

                /* Damage calculation */
                val damage = calcExplosionDamage(blockPos, target, fastCalc)
                damagePosMap[damage] = blockPos
            }
            return damagePosMap
        }

        fun getAxisRange(d1: Double, d2: Float): IntRange {
            return IntRange(floor(d1 - d2).toInt(), ceil(d1 + d2).toInt())
        }

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
        fun canPlace(blockPos: BlockPos, entity: Entity): Boolean {
            val entityBB = entity.boundingBox
            val placingBB = getCrystalPlacingBB(blockPos.up())
            return !entityBB.intersects(placingBB)
                    && (mc.world.getBlockState(blockPos).block == Blocks.BEDROCK
                    || mc.world.getBlockState(blockPos).block == Blocks.OBSIDIAN)
                    && !mc.world.checkBlockCollision(placingBB)
        }

        private fun getCrystalPlacingBB(blockPos: BlockPos): AxisAlignedBB {
            return crystalPlacingBB.offset(Vec3d(blockPos).add(0.5, 0.0, 0.5))
        }

        private val crystalPlacingBB: AxisAlignedBB get() = AxisAlignedBB(-0.5, 0.0, -0.5, 0.5, 2.0, 0.5)

        /* Checks colliding with all entity */
        fun canPlaceCollide(blockPos: BlockPos): Boolean {
            val placingBB = getCrystalPlacingBB(blockPos.up())
            return mc.world.checkNoEntityCollision(placingBB)
        }
        /* End of position finding */

        /* Damage calculation */

        fun calcExplosionDamage(crystal: EntityEnderCrystal, entity: EntityLivingBase, fastCalc: Boolean = false, calcBlastReduction: Boolean = true): Float {
            val pos = crystal.positionVector
            val rawDamage = calcExplosionDamage(pos, entity, fastCalc)
            return if (fastCalc || !calcBlastReduction) rawDamage
            else calcBlastReduction(rawDamage, pos, entity)
        }

        fun calcExplosionDamage(blockPos: BlockPos, entity: EntityLivingBase, fastCalc: Boolean = false, calcBlastReduction: Boolean = true): Float {
            val pos = Vec3d(blockPos).add(0.5, 1.0, 0.5)
            val rawDamage = calcExplosionDamage(pos, entity, fastCalc)
            return if (fastCalc || !calcBlastReduction) rawDamage
            else calcBlastReduction(rawDamage, pos, entity)
        }

        private fun calcBlastReduction(damageIn: Float, damagePos: Vec3d, entity: EntityLivingBase): Float {
            var damage = damageIn
            if (entity is EntityPlayer) {
                val damageSource = DamageSource.causeExplosionDamage(Explosion(mc.world, null, damagePos.x, damagePos.y, damagePos.z, 6F, false, true))
                damage = calcDamage(entity, damage, false)
                val k = EnchantmentHelper.getEnchantmentModifierDamage(entity.armorInventoryList, damageSource)
                val f = MathHelper.clamp(k.toFloat(), 0.0f, 20.0f)
                damage *= (1.0f - f / 25.0f)
                if (entity.isPotionActive(Potion.getPotionById(11)!!)) {
                    damage -= damage / 5
                }
                damage = max(damage, 0.0f)
                return damage * getDamageMultiplier()
            }
            return calcDamage(entity, damage, false)
        }

        private fun getDamageMultiplier(): Float {
            return mc.world.difficulty.id * 0.5f
        }

        private fun calcExplosionDamage(pos: Vec3d, entity: Entity, fastCalc: Boolean): Float {
            val distance = pos.distanceTo(entity.positionVector)
            return if (!fastCalc) {
                val v = (1.0 - (distance / 12.0)) * entity.world.getBlockDensity(pos, entity.boundingBox)
                ((v * v + v) / 2.0 * 84.0 + 1.0).toFloat()
            } else {
                1f / distance.toFloat() /* Use the reciprocal number so it can be sorted correctly */
            }
        }
        /* End of damage calculation */
    }
}