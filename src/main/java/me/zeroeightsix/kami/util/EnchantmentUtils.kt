package me.zeroeightsix.kami.util

import net.minecraft.enchantment.Enchantment
import net.minecraft.init.Enchantments

object EnchantmentUtils {

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

    enum class EnumEnchantments(val enchantment: Enchantment, val alias: String) {
        FIRE_PROTECTION(Enchantments.FIRE_PROTECTION, "Frp"),
        FEATHER_FALLING(Enchantments.FEATHER_FALLING, "Fea"),
        BLAST_PROTECTION(Enchantments.BLAST_PROTECTION, "Bla"),
        PROJECTILE_PROTECTION(Enchantments.PROJECTILE_PROTECTION, "Pjp"),
        RESPIRATION(Enchantments.RESPIRATION, "Res"),
        AQUA_AFFINITY(Enchantments.AQUA_AFFINITY, "Aqu"),
        THORNS(Enchantments.THORNS, "Thr"),
        DEPTH_STRIDER(Enchantments.DEPTH_STRIDER, "Dep"),
        FROST_WALKER(Enchantments.FROST_WALKER, "Fro"),
        BINDING_CURSE(Enchantments.BINDING_CURSE, "Bin"),
        SHARPNESS(Enchantments.SHARPNESS, "Sha"),
        SMITE(Enchantments.SMITE, "Smi"),
        BANE_OF_ARTHROPODS(Enchantments.BANE_OF_ARTHROPODS, "Ban"),
        KNOCKBACK(Enchantments.KNOCKBACK, "Knb"),
        FIRE_ASPECT(Enchantments.FIRE_ASPECT, "Fia"),
        LOOTING(Enchantments.LOOTING, "Loo"),
        SWEEPING(Enchantments.SWEEPING, "Swe"),
        EFFICIENCY(Enchantments.EFFICIENCY, "Eff"),
        SILK_TOUCH(Enchantments.SILK_TOUCH, "Sil"),
        UNBREAKING(Enchantments.UNBREAKING, "Unb"),
        FORTUNE(Enchantments.FORTUNE, "For"),
        POWER(Enchantments.POWER, "Pow"),
        PUNCH(Enchantments.PUNCH, "Pun"),
        FLAME(Enchantments.FLAME, "Fla"),
        INFINITY(Enchantments.INFINITY, "Inf"),
        LUCK_OF_THE_SEA(Enchantments.LUCK_OF_THE_SEA, "Luc"),
        LURE(Enchantments.LURE, "Lur"),
        MENDING(Enchantments.MENDING, "Men"),
        VANISHING_CURSE(Enchantments.VANISHING_CURSE, "Van")
    }
}