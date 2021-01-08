package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.util.KamiLang 
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import net.minecraft.inventory.EntityEquipmentSlot

object ArmorHide : Module(
    name = KamiLang.get("module.modules.render.ArmorHide.Armorhide"),
    category = Category.RENDER,
    description = KamiLang.get("module.modules.render.ArmorHide.HidesTheArmorOn"),
    showOnArray = false
) {
    val player = setting(KamiLang.get("module.modules.render.ArmorHide.Players"), false)
    val armourStand = setting(KamiLang.get("module.modules.render.ArmorHide.Armourstands"), true)
    val mobs = setting(KamiLang.get("module.modules.render.ArmorHide.Mobs"), true)
    private val helmet = setting(KamiLang.get("module.modules.render.ArmorHide.Helmet"), false)
    private val chestplate = setting(KamiLang.get("module.modules.render.ArmorHide.Chestplate"), false)
    private val leggings = setting(KamiLang.get("module.modules.render.ArmorHide.Leggings"), false)
    private val boots = setting(KamiLang.get("module.modules.render.ArmorHide.Boots"), false)

    @JvmStatic
    fun shouldHidePiece(slotIn: EntityEquipmentSlot): Boolean {
        return helmet.value && slotIn == EntityEquipmentSlot.HEAD
                || chestplate.value && slotIn == EntityEquipmentSlot.CHEST
                || leggings.value && slotIn == EntityEquipmentSlot.LEGS
                || boots.value && slotIn == EntityEquipmentSlot.FEET
    }
}