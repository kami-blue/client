package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import net.minecraft.inventory.EntityEquipmentSlot

object ArmorHide : Module(
    name = "ArmorHide",
    category = Category.RENDER,
    description = "Hides the armor on selected entities",
    showOnArray = false
) {
    val player = setting("Players", false)
    val armourStand = setting("ArmourStands", true)
    val mobs = setting("Mobs", true)
    private val helmet = setting("Helmet", false)
    private val chestplate = setting("Chestplate", false)
    private val leggings = setting("Leggings", false)
    private val boots = setting("Boots", false)

    @JvmStatic
    fun shouldHidePiece(slotIn: EntityEquipmentSlot): Boolean {
        return helmet.value && slotIn == EntityEquipmentSlot.HEAD
                || chestplate.value && slotIn == EntityEquipmentSlot.CHEST
                || leggings.value && slotIn == EntityEquipmentSlot.LEGS
                || boots.value && slotIn == EntityEquipmentSlot.FEET
    }
}