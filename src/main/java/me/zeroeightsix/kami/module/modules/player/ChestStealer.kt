package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.items.*
import me.zeroeightsix.kami.util.threads.runSafe
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiEnchantment
import net.minecraft.client.gui.GuiMerchant
import net.minecraft.client.gui.GuiRepair
import net.minecraft.client.gui.inventory.*
import net.minecraft.init.Items
import net.minecraftforge.fml.common.gameevent.TickEvent

internal object ChestStealer : Module(
    name = "ChestStealer",
    category = Category.PLAYER,
    description = "Automatically steal or store items from containers"
) {
    val mode = setting("Mode", Mode.TOGGLE)
    private val movingMode = setting("MovingMode", MovingMode.QUICK_MOVE)
    private val ignoreEjectItem = setting("IgnoresEjectItem", false, description = "Ignore AutoEject items in InventoryManager")
    private val delay = setting("Delay", 250, 0..1000, 25, description = "Move stack delay in ms")

    enum class Mode {
        ALWAYS, TOGGLE, MANUAL
    }

    private enum class MovingMode {
        QUICK_MOVE, PICKUP, THROW
    }

    private enum class ContainerMode(val offset: Int) {
        STEAL(36), STORE(0)
    }

    var stealing = false
    var storing = false
    val timer = TickTimer()

    init {
        safeListener<TickEvent.ClientTickEvent> {
            stealing = if (isContainerOpen() && (stealing || mode.value == Mode.ALWAYS)) {
                stealOrStore(getStealingSlot(), ContainerMode.STEAL)
            } else {
                false
            }

            storing = if (isContainerOpen() && (storing || mode.value == Mode.ALWAYS)) {
                stealOrStore(getStoringSlot(), ContainerMode.STORE)
            } else {
                false
            }
        }
    }

    private fun SafeClientEvent.canSteal(): Boolean {
        return getStealingSlot() != null
    }

    private fun SafeClientEvent.canStore(): Boolean {
        return getStoringSlot() != null
    }

    private fun SafeClientEvent.isContainerOpen(): Boolean {
        return player.openContainer != null
            && isValidGui()
    }

    fun isValidGui(): Boolean {
        return mc.currentScreen !is GuiEnchantment
            && mc.currentScreen !is GuiMerchant
            && mc.currentScreen !is GuiRepair
            && mc.currentScreen !is GuiBeacon
            && mc.currentScreen !is GuiCrafting
            && mc.currentScreen !is GuiContainerCreative
            && mc.currentScreen !is GuiInventory
    }

    @JvmStatic
    fun updateButton(button: GuiButton, left: Int, size: Int, top: Int) {
        runSafe {
            if (isEnabled && isContainerOpen()) {
                if (button.id == 696969) {
                    val str = if (stealing) {
                        "Stop"
                    } else {
                        "Steal"
                    }

                    button.x = left + size + 2
                    button.y = top + 2
                    button.enabled = canSteal() and !storing
                    button.visible = true
                    button.displayString = str
                } else if (button.id == 420420) {
                    val str = if (storing) {
                        "Stop"
                    } else {
                        "Store"
                    }

                    button.x = left + size + 2
                    button.y = top + 24
                    button.enabled = canStore() and !stealing
                    button.visible = true
                    button.displayString = str
                }
            } else {
                button.visible = false
            }
        }
    }

    private fun SafeClientEvent.stealOrStore(slot: Int?, containerMode: ContainerMode): Boolean {
        if (slot == null) return false

        val size = getContainerSlotSize()
        val rangeStart = if (containerMode == ContainerMode.STEAL) size else 0
        val slotTo = player.openContainer.getSlots(rangeStart until size + containerMode.offset).firstEmpty()
            ?: return false
        val windowID = player.openContainer.windowId

        if (timer.tick(delay.value.toLong())) {
            when (movingMode.value) {
                MovingMode.QUICK_MOVE -> quickMoveSlot(windowID, slot)
                MovingMode.PICKUP -> moveToSlot(windowID, slot, slotTo.slotNumber)
                MovingMode.THROW -> throwAllInSlot(windowID, slot)
            }
        }

        return true
    }

    private fun SafeClientEvent.getStealingSlot(): Int? {
        val container = player.openContainer.inventory

        for (slot in 0 until getContainerSlotSize()) {
            val item = container[slot].item
            if (item == Items.AIR) continue
            if (ignoreEjectItem.value && InventoryManager.ejectList.contains(item.registryName.toString())) continue
            return slot
        }

        return null
    }

    private fun SafeClientEvent.getStoringSlot(): Int? {
        val container = player.openContainer.inventory
        val size = getContainerSlotSize()

        for (slot in size until size + 36) {
            val item = container[slot].item
            if (item == Items.AIR) continue
            return slot
        }

        return null
    }

    private fun SafeClientEvent.getContainerSlotSize(): Int {
        if (mc.currentScreen !is GuiContainer) return 0
        return player.openContainer.inventorySlots.size - 36
    }
}
