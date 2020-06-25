package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.InventoryEffectRenderer
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack

/**
 * Created by 086 on 24/01/2018.
 */
@Module.Info(
        name = "AutoArmour",
        category = Module.Category.PLAYER,
        description = "Automatically equips armour"
)
class AutoArmour : Module() {
    override fun onUpdate() {
        if (mc.player.ticksExisted % 2 == 0) return

        // check screen
        if (mc.currentScreen is GuiContainer
                && mc.currentScreen !is InventoryEffectRenderer) return

        // store slots and values of best armor pieces
        val bestArmorSlots = IntArray(4)
        val bestArmorValues = IntArray(4)

        // initialize with currently equipped armor
        (0..3).forEach { armorType ->
            val oldArmor = mc.player.inventory.armorItemInSlot(armorType)

            if (oldArmor != null && oldArmor.getItem() is ItemArmor)
                bestArmorValues[armorType] =
                        (oldArmor.getItem() as ItemArmor).damageReduceAmount

            bestArmorSlots[armorType] = -1
        }

        // search inventory for better armor
        (0..35).forEach { slot ->
            val stack = mc.player.inventory.getStackInSlot(slot)

            if (stack.count > 1) return@forEach

            if (stack == null || stack.getItem() !is ItemArmor) return@forEach

            val armor = stack.getItem() as ItemArmor
            val armorType = armor.armorType.ordinal - 2

            if (armorType == 2 && mc.player.inventory.armorItemInSlot(armorType).getItem() == Items.ELYTRA) return@forEach

            val armorValue = armor.damageReduceAmount

            if (armorValue > bestArmorValues[armorType]) {
                bestArmorSlots[armorType] = slot
                bestArmorValues[armorType] = armorValue
            }
        }

        // equip better armor
        for (armorType in 0..3) {
            // check if better armor was found
            var slot = bestArmorSlots[armorType]
            if (slot == -1) continue

            // check if armor can be swapped
            // needs 1 free slot where it can put the old armor
            val oldArmor = mc.player.inventory.armorItemInSlot(armorType)
            if (oldArmor == null || oldArmor != ItemStack.EMPTY || mc.player.inventory.firstEmptyStack != -1) {
                // hotbar fix
                if (slot < 9) slot += 36

                // swap armor
                mc.playerController.windowClick(0, 8 - armorType, 0,
                        ClickType.QUICK_MOVE, mc.player)
                mc.playerController.windowClick(0, slot, 0,
                        ClickType.QUICK_MOVE, mc.player)
                break
            }
        }
    }
}