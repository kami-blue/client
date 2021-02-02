package org.kamiblue.client.util

import net.minecraft.enchantment.Enchantment
import net.minecraft.init.Enchantments
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import kotlin.math.abs

object EnchantmentUtils {

    fun getAllEnchantments(itemStack: ItemStack): List<LeveledEnchantment> {
        val enchantmentList = ArrayList<LeveledEnchantment>()
        val nbtTagList = (if (itemStack.item == Items.ENCHANTED_BOOK) itemStack.tagCompound?.getTagList("StoredEnchantments", 10)
        else itemStack.enchantmentTagList) ?: return enchantmentList

        for (i in 0 until nbtTagList.tagCount()) {
            val compound = nbtTagList.getCompoundTagAt(i)
            val enchantment = Enchantment.getEnchantmentByID(compound.getShort("id").toInt()) ?: continue
            val level = compound.getShort("lvl")
            enchantmentList.add(LeveledEnchantment(enchantment, level))
        }
        return enchantmentList
    }

    /**
     * Get alias for given enchantment
     *
     * @param [enchantment] Enchantment in
     * @return Alias for [enchantment]
     */
    fun getEnchantmentAlias(enchantment: Enchantment): String {
        return getEnumEnchantment(enchantment)?.alias ?: "Null"
    }

    /**
     * Get EnumEnchantment for given enchantment
     *
     * @param [enchantment] Enchantment in
     * @return [EnumEnchantments] matches with [enchantment]
     */
    fun getEnumEnchantment(enchantment: Enchantment): EnumEnchantments? {
        return enchantmentMap[enchantment]
    }

    private val enchantmentMap = EnumEnchantments.values().map { it.enchantment to it }.toMap()

    class LeveledEnchantment(val enchantment: Enchantment, val level: Short) {
        val isSingleLevel = enchantment.maxLevel == 1
        val isMax = level >= enchantment.maxLevel
        val is32K = abs(level.toInt()) >= 32000
        val alias = getEnchantmentAlias(enchantment)
        val levelText = if (isSingleLevel) "" else if (is32K) "32K" else if (isMax) "MAX" else "$level"
    }

    enum class EnumEnchantments(val enchantment: Enchantment, val alias: String) {
        PROTECTION(Enchantments.PROTECTION, "PRO"),
        FIRE_PROTECTION(Enchantments.FIRE_PROTECTION, "FRP"),
        FEATHER_FALLING(Enchantments.FEATHER_FALLING, "FEA"),
        BLAST_PROTECTION(Enchantments.BLAST_PROTECTION, "BLA"),
        PROJECTILE_PROTECTION(Enchantments.PROJECTILE_PROTECTION, "PJP"),
        RESPIRATION(Enchantments.RESPIRATION, "RES"),
        AQUA_AFFINITY(Enchantments.AQUA_AFFINITY, "AQU"),
        THORNS(Enchantments.THORNS, "THR"),
        DEPTH_STRIDER(Enchantments.DEPTH_STRIDER, "DEP"),
        FROST_WALKER(Enchantments.FROST_WALKER, "FRO"),
        BINDING_CURSE(Enchantments.BINDING_CURSE, "BIN"),
        SHARPNESS(Enchantments.SHARPNESS, "SHA"),
        SMITE(Enchantments.SMITE, "SMI"),
        BANE_OF_ARTHROPODS(Enchantments.BANE_OF_ARTHROPODS, "BAN"),
        KNOCKBACK(Enchantments.KNOCKBACK, "KNB"),
        FIRE_ASPECT(Enchantments.FIRE_ASPECT, "FIA"),
        LOOTING(Enchantments.LOOTING, "LOO"),
        SWEEPING(Enchantments.SWEEPING, "SWE"),
        EFFICIENCY(Enchantments.EFFICIENCY, "EFF"),
        SILK_TOUCH(Enchantments.SILK_TOUCH, "SIL"),
        UNBREAKING(Enchantments.UNBREAKING, "UNB"),
        FORTUNE(Enchantments.FORTUNE, "FOT"),
        POWER(Enchantments.POWER, "POW"),
        PUNCH(Enchantments.PUNCH, "PUN"),
        FLAME(Enchantments.FLAME, "FLA"),
        INFINITY(Enchantments.INFINITY, "INF"),
        LUCK_OF_THE_SEA(Enchantments.LUCK_OF_THE_SEA, "LUC"),
        LURE(Enchantments.LURE, "LUR"),
        MENDING(Enchantments.MENDING, "MEN"),
        VANISHING_CURSE(Enchantments.VANISHING_CURSE, "VAN")
    }
}