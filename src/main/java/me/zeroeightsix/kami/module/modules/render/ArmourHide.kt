package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.inventory.EntityEquipmentSlot

@Module.Info(
        name = "ArmourHide",
        category = Module.Category.RENDER,
        description = "Hides the armour on selected entities",
        showOnArray = Module.ShowOnArray.OFF
)
object ArmourHide : Module() {
    val player = register(Settings.b("Players", false))
    val armourStand = register(Settings.b("ArmourStands", true))
    val mobs = register(Settings.b("Mobs", true))
    private val helmet = register(Settings.b("Helmet", false))
    private val chestplate = register(Settings.b("Chestplate", false))
    private val leggings = register(Settings.b("Leggings", false))
    private val boots = register(Settings.b("Boots", false))

    @JvmStatic
    fun shouldHidePiece(slotIn: EntityEquipmentSlot): Boolean {
        return helmet.value && slotIn == EntityEquipmentSlot.HEAD
                || chestplate.value && slotIn == EntityEquipmentSlot.CHEST
                || leggings.value && slotIn == EntityEquipmentSlot.LEGS
                || boots.value && slotIn == EntityEquipmentSlot.FEET
    }
}