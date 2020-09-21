package me.zeroeightsix.kami.util.combat

import me.zeroeightsix.kami.util.InventoryUtils
import net.minecraft.client.Minecraft
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.EnumCreatureAttribute
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.util.CombatRules
import net.minecraft.util.DamageSource
import net.minecraft.util.math.MathHelper
import kotlin.math.max
import kotlin.math.round

object CombatUtils {
    private val mc: Minecraft = Minecraft.getMinecraft()

    @JvmStatic
    fun calcDamageFromMob(entity: EntityMob): Float {
        var damage = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).attributeValue.toFloat()
        damage += EnchantmentHelper.getModifierForCreature(entity.heldItemMainhand, mc.player.creatureAttribute)
        return calcDamage(mc.player, damage)
    }

    @JvmStatic
    fun calcDamage(entity: EntityLivingBase, damageIn: Float = 100f, source: DamageSource = DamageSource.GENERIC, roundDamage: Boolean = false): Float {
        if (entity is EntityPlayer && entity.isCreative) return 0.0f // Return 0 directly if entity is a player and in creative mode
        var damage = CombatRules.getDamageAfterAbsorb(damageIn, entity.totalArmorValue.toFloat(), entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat())

        if (source != DamageSource.OUT_OF_WORLD) {
            entity.getActivePotionEffect(MobEffects.RESISTANCE)?.let {
                damage *= max(1f - it.amplifier * 0.2f, 0f)
            }
        }
        if (entity is EntityPlayer) {
            damage *= getProtectionModifier(entity, source)
        }
        return if (roundDamage) round(damage) else damage
    }

    @JvmStatic
    fun getProtectionModifier(entity: EntityPlayer, damageSource: DamageSource): Float {
        var modifier = 0
        for (armor in entity.armorInventoryList) {
            if (armor.isEmpty()) continue // Skip if item stack is empty
            val nbtTagList = armor.enchantmentTagList
            for (i in 0 until nbtTagList.tagCount()) {
                val id = nbtTagList.getCompoundTagAt(i).getShort("id").toInt()
                val level = nbtTagList.getCompoundTagAt(i).getShort("lvl").toInt()
                Enchantment.getEnchantmentByID(id)?.let { modifier += it.calcModifierDamage(level, damageSource) }
            }
        }
        modifier = MathHelper.clamp(modifier, 0, 20)
        return (1.0f - modifier / 25.0f)
    }

    @JvmStatic
    fun equipBestWeapon(hitMode: PreferWeapon = PreferWeapon.NONE) {
        var bestSlot = -1
        var maxDamage = 0.0
        for (i in 0..8) {
            val stack = mc.player.inventory.getStackInSlot(i)
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
}