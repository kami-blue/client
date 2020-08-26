package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.inventory.EntityEquipmentSlot

/**
 * Created by TBM on 30/12/2019.
 */
@Module.Info(
        name = "ArmourHide",
        category = Module.Category.RENDER,
        description = "Hides the armour on selected entities",
        showOnArray = Module.ShowOnArray.OFF
)
class ArmourHide : Module() {
    @JvmField
    var player: Setting<Boolean> = register(Settings.b("Players", false))

    @JvmField
    var armourstand: Setting<Boolean> = register(Settings.b("ArmourStands", true))

    @JvmField
    var mobs: Setting<Boolean> = register(Settings.b("Mobs", true))
    var helmet: Setting<Boolean> = register(Settings.b("Helmet", false))
    var chestplate: Setting<Boolean> = register(Settings.b("Chestplate", false))
    var leggings: Setting<Boolean> = register(Settings.b("Leggings", false))
    var boots: Setting<Boolean> = register(Settings.b("Boots", false))

    companion object {
        @JvmField
        var INSTANCE: ArmourHide? = null

        @JvmStatic
        fun shouldRenderPiece(slotIn: EntityEquipmentSlot): Boolean {
            return if (slotIn == EntityEquipmentSlot.HEAD && INSTANCE!!.helmet.value) {
                true
            } else if (slotIn == EntityEquipmentSlot.CHEST && INSTANCE!!.chestplate.value) {
                true
            } else if (slotIn == EntityEquipmentSlot.LEGS && INSTANCE!!.leggings.value) {
                true
            } else slotIn == EntityEquipmentSlot.FEET && INSTANCE!!.boots.value
        }
    }

    init {
        INSTANCE = this
    }
}