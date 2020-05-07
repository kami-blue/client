package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Pair
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import java.util.*

/**
 * Created on 29 November 2019 by hub
 */
@Module.Info(
        name = "AutoReplenish",
        category = Module.Category.COMBAT,
        description = "Refills items in your hotbar"
)
class AutoReplenish : Module() {
    private val threshold = register(Settings.integerBuilder("Refill at").withMinimum(1).withValue(32).withMaximum(63).build())
    private val tickDelay = register(Settings.integerBuilder("TickDelay").withMinimum(1).withValue(2).withMaximum(10).build())

    private var delayStep = 0

    override fun onUpdate() {
        if (mc.player == null || mc.currentScreen is GuiContainer) return

        delayStep = if (delayStep < tickDelay.value) {
            delayStep++
            return
        } else {
            0
        }

        val slots = findReplenishableHotbarSlot() ?: return
        val inventorySlot = slots.key
        val hotbarSlot = slots.value

        // pick up inventory slot
        mc.playerController.windowClick(0, inventorySlot, 0, ClickType.PICKUP, mc.player)

        // click on hotbar slot
        mc.playerController.windowClick(0, hotbarSlot, 0, ClickType.PICKUP, mc.player)

        // put back inventory slot
        mc.playerController.windowClick(0, inventorySlot, 0, ClickType.PICKUP, mc.player)
    }

    /**
     * Returns the first found combination of replenishable inventory slot and compatible hotbar slot.
     *
     * @return mergable pair (key: inventorySlot, value: hotbarSlot) of replenishable inventory slot and hotbar slot, otherwise null
     */
    private fun findReplenishableHotbarSlot(): Pair<Int, Int>? {
        var returnPair: Pair<Int, Int>? = null

        for ((key, stack) in hotbar) {
            if (stack.isEmpty || stack.getItem() === Items.AIR) {
                continue
            }
            if (!stack.isStackable) {
                continue
            }
            if (stack.stackSize >= stack.maxStackSize) {
                continue
            }
            if (stack.stackSize > threshold.value) {
                continue
            }

            val inventorySlot = findCompatibleInventorySlot(stack)

            if (inventorySlot == -1) {
                continue
            }
            returnPair = Pair(inventorySlot, key)
        }
        return returnPair
    }

    /**
     * Find a compatible inventory slot, holding a stack mergable with the hotbarStack.
     *
     * @param hotbarStack the hotbar slot id that a inventory slot with compatible stack is searched for
     * @return inventory slot id holding compatible stack, otherwise -1
     */
    private fun findCompatibleInventorySlot(hotbarStack: ItemStack): Int {
        var inventorySlot = -1
        var smallestStackSize = 999

        for ((key, inventoryStack) in inventory) {
            if (inventoryStack.isEmpty || inventoryStack.getItem() === Items.AIR) {
                continue
            }
            if (!isCompatibleStacks(hotbarStack, inventoryStack)) {
                continue
            }

            val currentStackSize = mc.player.inventoryContainer.inventory[key].stackSize

            if (smallestStackSize > currentStackSize) {
                smallestStackSize = currentStackSize
                inventorySlot = key
            }
        }
        return inventorySlot
    }

    /**
     * Returns true if stacks can be merged, otherwise false.
     * This ignores stacksize!
     *
     * @param stack1 Stack 1
     * @param stack2 Stack 2
     * @return true if stacks can be merged, otherwise false.
     */
    private fun isCompatibleStacks(stack1: ItemStack, stack2: ItemStack): Boolean {

        // check if not same item
        if (stack1.getItem() != stack2.getItem()) {
            return false
        }

        // check if not same block
        if (stack1.getItem() is ItemBlock && stack2.getItem() is ItemBlock) {
            val block1 = (stack1.getItem() as ItemBlock).block
            val block2 = (stack2.getItem() as ItemBlock).block
            if (block1.material != block2.material) {
                return false
            }
        }

        // check if not same names
        if (stack1.displayName != stack2.displayName) {
            return false
        }

        // check if not same damage (e.g. skulls)
        return stack1.getItemDamage() == stack2.getItemDamage()
    }

    companion object {
        /**
         * Returns player inventory (without hotbar)
         *
         * @return Map(Key = Slot Id, Value = Slot ItemStack) Player Inventory
         */
        private val inventory: Map<Int, ItemStack>
            get() = getInventorySlots(9, 35)

        /**
         * Returns player hotbar
         *
         * @return Map(Key = Slot Id, Value = Slot ItemStack) Player Hotbar
         */
        private val hotbar: Map<Int, ItemStack>
            get() = getInventorySlots(36, 44)

        private fun getInventorySlots(current: Int, last: Int): Map<Int, ItemStack> {
            var current = current
            val fullInventorySlots: MutableMap<Int, ItemStack> = HashMap()
            while (current <= last) {
                fullInventorySlots[current] = mc.player.inventoryContainer.inventory[current]
                current++
            }
            return fullInventorySlots
        }
    }
}