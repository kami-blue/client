package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.module.mangers.CombatManager
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Blocks
import net.minecraft.util.CombatRules
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

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

    fun calcDamage(entity: EntityLivingBase, damage: Float, roundDamage: Boolean): Float {
        val damage = CombatRules.getDamageAfterAbsorb(damage, entity.totalArmorValue.toFloat(), entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat())
        return if (roundDamage) round(damage) else damage
    }

    object CrystalUtils {
        /* Position Finding */
        fun getPlacePos(target: Entity?, radius: Double, fastCalc: Boolean, feetLevel: Boolean): Map<Float, BlockPos> {
            if (target == null) return emptyMap()
            val feetPosY = target.posY.toInt() - 1
            val yRange = if (!fastCalc && !feetLevel) getIntRange(target.posY, radius) else IntRange(feetPosY, feetPosY)
            val damagePosMap = HashMap<Float, BlockPos>()
            for (x in getIntRange(target.posX, radius)) for (y in yRange) for (z in getIntRange(target.posZ, radius)) {
                /* Valid position check */
                val blockPos = BlockPos(x, y, z)
                if (target.getDistanceSq(blockPos) > radius * radius) continue
                if (!canPlace(blockPos)) continue

                /* Damage calculation */
                val damage = calcExplosionDamage(blockPos, target, fastCalc)
                damagePosMap[damage] = blockPos
            }
            return damagePosMap
        }

        fun getCrystalList(range: Float): Map<EntityEnderCrystal, Float> {
            val crystalList = HashMap<EntityEnderCrystal, Float>()
            val entityList = ArrayList<Entity>()
            try {
                entityList.addAll(mc.world.loadedEntityList)
                for (entity in entityList) {
                    if (entity !is EntityEnderCrystal) continue
                    if (entity.isDead) continue
                    if (mc.player.getDistance(entity) > range) continue
                    crystalList[entity] = 0.5f
                }
            } catch (ignored: ConcurrentModificationException) {
            }
            return crystalList
        }

        /* Checks blocks and target colliding only */
        fun canPlace(blockPos: BlockPos): Boolean {
            val pos1 = blockPos.up()
            val pos2 = pos1.up()
            val bBox = CombatManager.currentTarget?.boundingBox ?: return false
            val xArray = arrayOf(floor(bBox.minX).toInt(), floor(bBox.maxX).toInt())
            val yArray = arrayOf(floor(bBox.minY).toInt(), floor(bBox.maxY).toInt())
            val zArray = arrayOf(floor(bBox.minZ).toInt(), floor(bBox.maxZ).toInt())
            for (x in xArray) for (y in yArray) for (z in zArray) {
                if (pos1 == BlockPos(x, y, z)
                        || pos2 == BlockPos(x, y, z)) return false
            }
            return (mc.world.getBlockState(blockPos).block == Blocks.BEDROCK || mc.world.getBlockState(blockPos).block == Blocks.OBSIDIAN)
                    && mc.world.isAirBlock(pos1) && mc.world.isAirBlock(pos2)
        }

        /* Checks crystal colliding */
        fun canPlaceCollide(blockPos: BlockPos): Boolean {
            val pos = blockPos.up()
            return try {
                mc.world.checkNoEntityCollision(AxisAlignedBB(pos))
            } catch (ignored: ConcurrentModificationException) {
                false
            }
        }

        fun getIntRange(d1: Double, d2: Double): IntRange {
            return IntRange(floor(d1 - d2).toInt(), ceil(d1 + d2).toInt())
        }
        /* End of position findind */

        /* Damage calculation */
        fun calcExplosionDamage(blockPos: BlockPos, entity: Entity, fastCalc: Boolean): Float {
            val posX = blockPos.x + 0.5
            val posY = blockPos.y + 1.0
            val posZ = blockPos.z + 0.5
            val vec3d = Vec3d(posX, posY, posZ)
            return calcExplosionDamage(vec3d, entity, fastCalc)
        }

        fun calcExplosionDamage(crystal: EntityEnderCrystal, entity: Entity, fastCalc: Boolean): Float {
            val vec3d = crystal.positionVector
            return calcExplosionDamage(vec3d, entity, fastCalc)
        }

        fun calcExplosionDamage(pos: Vec3d, entity: Entity, fastCalc: Boolean): Float {
            val distance = entity.getDistance(pos.x, pos.y, pos.z)
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