package org.kamiblue.client.module.modules.render

import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.EntityEquipmentSlot
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module

internal object ArmorHide : Module(
    name = "ArmorHide",
    category = Category.RENDER,
    description = "Hides the armor on selected entities",
    showOnArray = false
) {
    private val player by setting("Players", false)
    private val armorStands by setting("Armour Stands", true)
    private val mobs by setting("Mobs", true)
    private val helmet by setting("Helmet", false)
    private val chestplate by setting("Chestplate", false)
    private val leggings by setting("Leggings", false)
    private val boots by setting("Boots", false)

    @JvmStatic
    fun shouldHide(slotIn: EntityEquipmentSlot, entity: EntityLivingBase): Boolean {
        return when (entity) {
            is EntityPlayer -> player && shouldHidePiece(slotIn)
            is EntityArmorStand -> armorStands && shouldHidePiece(slotIn)
            is EntityMob -> mobs && shouldHidePiece(slotIn)
            else -> false
        }
    }

    private fun shouldHidePiece(slotIn: EntityEquipmentSlot): Boolean {
        return helmet && slotIn == EntityEquipmentSlot.HEAD
            || chestplate && slotIn == EntityEquipmentSlot.CHEST
            || leggings && slotIn == EntityEquipmentSlot.LEGS
            || boots && slotIn == EntityEquipmentSlot.FEET
    }
}