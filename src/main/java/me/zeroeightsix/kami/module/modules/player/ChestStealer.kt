package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.gui.mc.KamiGuiStealButton
import me.zeroeightsix.kami.mixin.client.gui.MixinGuiContainer
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.player.ChestStealer.canSteal
import me.zeroeightsix.kami.module.modules.player.ChestStealer.stealing
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.InventoryUtils.getEmptySlotContainer
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiEnchantment
import net.minecraft.client.gui.GuiMerchant
import net.minecraft.client.gui.GuiRepair
import net.minecraft.client.gui.inventory.*
import net.minecraft.init.Items
import net.minecraftforge.fml.common.gameevent.TickEvent

object ChestStealer : Module(
    name = "ChestStealer",
    category = Category.PLAYER,
    description = "Automatically steal items from containers"
) {
    val stealMode = setting("StealMode", StealMode.TOGGLE)
    private val movingMode = setting("MovingMode", MovingMode.QUICK_MOVE)
    private val ignoreEjectItem = setting("IgnoresEjectItem", false)
    private val delay = setting("Delay(ms)", 250, 0..1000, 25)

    enum class StealMode {
        ALWAYS, TOGGLE, MANUAL
    }

    private enum class MovingMode {
        QUICK_MOVE, PICKUP, THROW
    }

    var stealing = false
    val timer = TickTimer()

    init {
        safeListener<TickEvent.ClientTickEvent> {
            stealing = if (isContainerOpen() && (stealing || stealMode.value == StealMode.ALWAYS)) {
                steal(getStealingSlot())
            } else {
                false
            }
        }
    }

    fun canSteal(): Boolean {
        return getStealingSlot() != null
    }

    fun isContainerOpen(): Boolean {
        return mc.player.openContainer != null
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
        if (isEnabled && isContainerOpen()) {
            val str = if (stealing) {
                "Stop"
            } else {
                "Steal"
            }

            button.x = left + size + 2
            button.y = top + 2
            button.enabled = canSteal()
            button.visible = true
            button.displayString = str
        } else {
            button.visible = false
        }
    }

    private fun steal(slot: Int?): Boolean {
        if (slot == null) return false
        val size = getContainerSlotSize()
        val slotTo = getEmptySlotContainer(size, size + 35) ?: return false
        val windowID = mc.player.openContainer.windowId

        if (timer.tick(delay.value.toLong())) {
            when (movingMode.value) {
                MovingMode.QUICK_MOVE -> InventoryUtils.quickMoveSlot(windowID, slot)
                MovingMode.PICKUP -> InventoryUtils.moveToSlot(windowID, slot, slotTo)
                MovingMode.THROW -> InventoryUtils.throwAllInSlot(windowID, slot)
            }
        }
        return true
    }

    private fun getStealingSlot(): Int? {
        val container = mc.player.openContainer.inventory
        for (slot in 0 until getContainerSlotSize()) {
            val item = container[slot].item
            if (item == Items.AIR) continue
            if (ignoreEjectItem.value && InventoryManager.ejectList.contains(item.registryName.toString())) continue
            return slot
        }
        return null
    }

    private fun getContainerSlotSize(): Int {
        if (mc.currentScreen !is GuiContainer) return 0
        return mc.player.openContainer.inventorySlots.size - 36
    }
}
