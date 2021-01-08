package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.EntityEquipmentSlot

object ArmorHide : Module(
    name = "ArmorHide",
    category = Category.RENDER,
    description = "Hides the armor on selected entities",
    showOnArray = false
) {
    val player by setting("Players", false)
    val armorStand by setting("ArmourStands", true)
    val mobs by setting("Mobs", true)
    private val helmet = setting("Helmet", false)
    private val chestplate = setting("Chestplate", false)
    private val leggings = setting("Leggings", false)
    private val boots = setting("Boots", false)

    @JvmStatic
    fun shouldHide(slotIn: EntityEquipmentSlot, entity: EntityLivingBase): Boolean {
        return when(entity) {
            is EntityPlayer -> player && shouldHidePiece(slotIn)
            is EntityArmorStand -> armorStand && shouldHidePiece(slotIn)
            is EntityMob -> mobs && shouldHidePiece(slotIn)
            else -> false
        }
    }

    private fun shouldHidePiece(slotIn: EntityEquipmentSlot): Boolean {
        return helmet.value && slotIn == EntityEquipmentSlot.HEAD
                || chestplate.value && slotIn == EntityEquipmentSlot.CHEST
                || leggings.value && slotIn == EntityEquipmentSlot.LEGS
                || boots.value && slotIn == EntityEquipmentSlot.FEET
    }
}