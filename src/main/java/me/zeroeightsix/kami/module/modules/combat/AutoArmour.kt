package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
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
    private val delay = register(Settings.integerBuilder("Delay").withValue(5).withRange(1, 10).withStep(1))

    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.TICKS)

    override fun onUpdate(event: SafeTickEvent) {
        if (!timer.tick(delay.value.toLong(), false)) return
        if (!mc.player.inventory.getItemStack().isEmpty()) {
            if (mc.currentScreen is GuiContainer) timer.reset(150L) // Wait for 3 extra ticks if player is moving item
            else InventoryUtils.removeHoldingItem()
            return
        }
        // store slots and values of best armor pieces, initialize with currently equipped armor
        // Pair<Slot, Value>
        val bestArmors = Array(4) { -1 to getArmorValue(mc.player.inventory.armorInventory[it]) }

        // search inventory for better armor
        for (slot in 9..44) {
            val itemStack = mc.player.inventoryContainer.inventory[slot]
            val item = itemStack.getItem()
            if (item !is ItemArmor) continue

            val armorType = item.armorType.index
            if (armorType == 2 && mc.player.inventory.armorInventory[2].getItem() == Items.ELYTRA) continue // Skip if item is chestplate and we have elytra equipped
            val armorValue = getArmorValue(itemStack)

            if (armorValue > bestArmors[armorType].second) bestArmors[armorType] = slot to armorValue
        }

        // equip better armor
        for ((index, pair) in bestArmors.withIndex()) {
            if (pair.first == -1) continue // Skip if we didn't find a better armor
            InventoryUtils.moveToSlot(pair.first, 8 - index)
            timer.reset()
            break // Don't move more than one at once
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