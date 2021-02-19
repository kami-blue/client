package org.kamiblue.client.module.modules.combat

import net.minecraft.init.Items
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.*
import org.kamiblue.client.util.inventory.ClickTask
import org.kamiblue.client.util.inventory.clickTask
import org.kamiblue.client.util.inventory.confirmedOrTrue
import org.kamiblue.client.util.inventory.operation.click
import org.kamiblue.client.util.inventory.operation.shiftClick
import org.kamiblue.client.util.inventory.slot.armorSlots
import org.kamiblue.client.util.inventory.slot.chestSlot
import org.kamiblue.client.util.inventory.slot.inventorySlots
import org.kamiblue.client.util.threads.safeListener

internal object AutoArmor : Module(
    name = "AutoArmor",
    category = Category.COMBAT,
    description = "Automatically equips armour",
    modulePriority = 500
) {
    private val confirmTimeout by setting("Confirm Timeout", 5, 1..20, 1)
    private val delay by setting("Delay", 2, 1..10, 1)

    private var lastTask: ClickTask? = null

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (!lastTask.confirmedOrTrue) return@safeListener

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

            lastTask = clickTask {
                postDelay(delay.toLong(), TimeUnit.TICKS)
                timeout(confirmTimeout.toLong(), TimeUnit.TICKS)

                if (!armorSlot.hasStack) {
                    shiftClick(pair.first) // Move the new one into armor slot
                } else {
                    click(armorSlot)  // Pick up the old armor from armor slot
                    shiftClick(pair.first) // Move the new one into armor slot
                    click(pair.first) // Put the old one into the empty slot
                }
            }

            break // Don't move more than one at once
        }
    }
}