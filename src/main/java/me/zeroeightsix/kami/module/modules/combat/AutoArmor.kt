package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.manager.managers.PlayerInventoryManager.addInventoryTask
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.items.*
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.gameevent.TickEvent

object AutoArmor : Module(
    name = "AutoArmor",
    category = Category.COMBAT,
    description = "Automatically equips armour",
    modulePriority = 500
) {
    private val delay = setting("Delay", 5, 1..10, 1)

    private val timer = TickTimer(TimeUnit.TICKS)
    private var lastTask = TaskState(true)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (!lastTask.done || !timer.tick(delay.value.toLong())) return@safeListener

            val armorSlots = player.armorSlots
            val chestItem = player.chestSlot.stack.item

            // store slots and values of best armor pieces, initialize with currently equipped armor
            // Pair<Slot, Value>
            val bestArmors = Array(4) { armorSlots[it] to getArmorValue(armorSlots[it].stack) }

            // search inventory for better armor
            for (slot in player.inventorySlots) {
                val itemStack = slot.stack
                val item = itemStack.item
                if (item !is ItemArmor) continue

                val armorType = item.armorType
                if (armorType == EntityEquipmentSlot.CHEST && chestItem == Items.ELYTRA) continue // Skip if item is chestplate and we have elytra equipped

                val armorValue = getArmorValue(itemStack)
                val armorIndex = 3 - armorType.index

                if (armorValue > bestArmors[armorIndex].second) bestArmors[armorIndex] = slot to armorValue
            }

            // equip better armor
            equipArmor(armorSlots, bestArmors)
        }
    }

    private fun getArmorValue(itemStack: ItemStack): Float {
        val item = itemStack.item
        return if (item !is ItemArmor) -1f
        else item.damageReduceAmount * getProtectionModifier(itemStack)
    }

    private fun getProtectionModifier(itemStack: ItemStack): Float {
        val tagList = itemStack.enchantmentTagList
        for (i in 0 until tagList.tagCount()) {
            val compoundTag = tagList.getCompoundTagAt(i)

            val id = compoundTag.getInteger("id")
            if (id != 0) continue

            val level = compoundTag.getInteger("lvl")
            return 1.0f + 0.04f * level
        }
        return 1.0f
    }

    private fun equipArmor(armorSlots: List<Slot>, bestArmors: Array<Pair<Slot, Float>>) {
        for ((index, pair) in bestArmors.withIndex()) {
            if (pair.first.slotNumber < 9) continue // Skip if we didn't find a better armor in inventory

            val armorSlot = armorSlots[index]

            lastTask = if (!armorSlot.hasStack) {
                addInventoryTask(
                    ClickInfo(0, pair.first, type = ClickType.QUICK_MOVE) // Move the new one into armor slot
                )
            } else {
                addInventoryTask(
                    ClickInfo(0, armorSlot, type = ClickType.PICKUP), // Pick up the old armor from armor slot
                    ClickInfo(0, pair.first, type = ClickType.QUICK_MOVE), // Move the new one into armor slot
                    ClickInfo(0, pair.first, type = ClickType.PICKUP) // Put the old one into the empty slot
                )
            }

            break // Don't move more than one at once
        }
    }
}