package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.TimerUtils
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.init.Items
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack

@Module.Info(
        name = "AutoArmour",
        category = Module.Category.COMBAT,
        description = "Automatically equips armour"
)
object AutoArmour : Module() {
    val timer = TimerUtils.TickTimer()

    override fun onUpdate() {
        if (!timer.tick(100L, false)) return
        if (!mc.player.inventory.getItemStack().isEmpty()) {
            if (mc.currentScreen is GuiContainer) timer.reset() // Wait for 2 ticks if player is moving item
            else InventoryUtils.removeHoldingItem()
            return
        }
        // store slots and values of best armor pieces, initialize with currently equipped armor
        // Pair<Slot, Value>
        val bestArmors = Array(4) { -1 to getArmorValue(mc.player.inventory.armorItemInSlot(it)) }

        // search inventory for better armor
        for (slot in 9..44) {
            val itemStack = mc.player.inventoryContainer.inventory[slot]
            val item = itemStack.getItem()
            if (item !is ItemArmor) continue

            val armorType = item.armorType.ordinal - 2
            if (armorType == 2 && mc.player.inventory.armorInventory[2].getItem() == Items.ELYTRA) continue // Skip if item is chestplate and we have elytra equipped
            val armorValue = getArmorValue(itemStack)

            if (armorValue > bestArmors[armorType].second) bestArmors[armorType] = slot to armorValue
        }

        // equip better armor
        for (armorType in 0..3) {
            val slot = bestArmors[armorType].first
            if (slot == -1) continue // Skip if we didn't find a better armor
            InventoryUtils.moveToSlot(slot, 8 - armorType)
            timer.reset()
        }
    }

    private fun getArmorValue(itemStack: ItemStack): Float {
        val item = itemStack.getItem()
        return if (item !is ItemArmor) -1f
        else item.damageReduceAmount * getProtectionModifier(itemStack)
    }

    private fun getProtectionModifier(itemStack: ItemStack): Float {
        for (i in 0 until itemStack.enchantmentTagList.tagCount()) {
            val id = itemStack.enchantmentTagList.getCompoundTagAt(i).getShort("id").toInt()
            val level = itemStack.enchantmentTagList.getCompoundTagAt(i).getShort("lvl").toInt()
            if (id != 0) continue
            return 1f + 0.04f * level
        }
        return 1f
    }
}