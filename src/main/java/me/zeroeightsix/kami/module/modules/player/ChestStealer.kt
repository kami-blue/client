package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.InventoryUtils.getEmptySlotContainer
import me.zeroeightsix.kami.util.TimerUtils
import net.minecraft.client.gui.GuiEnchantment
import net.minecraft.client.gui.GuiMerchant
import net.minecraft.client.gui.GuiRepair
import net.minecraft.client.gui.inventory.*
import net.minecraft.init.Items

@Module.Info(
        name = "ChestStealer",
        category = Module.Category.PLAYER,
        description = "Automatically steal items from containers"
)
object ChestStealer : Module() {
    val stealMode: Setting<StealMode> = register(Settings.e<StealMode>("StealMode", StealMode.TOGGLE))
    private val movingMode = register(Settings.e<MovingMode>("MovingMode", MovingMode.QUICK_MOVE))
    private val ignoreEjectItem = register(Settings.b("IgnoresEjectItem", false))
    private val delay = register(Settings.integerBuilder("Delay(ms)").withValue(250).withRange(0, 1000).build())

    enum class StealMode {
        ALWAYS, TOGGLE, MANUAL
    }

    private enum class MovingMode {
        QUICK_MOVE, PICKUP, THROW
    }

    var stealing = false
    val timer = TimerUtils.TickTimer()

    override fun onUpdate(event: SafeTickEvent) {
        stealing = if (isContainerOpen() && (stealing || stealMode.value == StealMode.ALWAYS)) {
            steal(getStealingSlot())
        } else {
            false
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
                else -> { }
            }
        }
        return true
    }

    private fun getStealingSlot(): Int? {
        val container = mc.player.openContainer.inventory
        val ejectList = InventoryManager.ejectArrayList
        for (slot in 0 until getContainerSlotSize()) {
            val item = container[slot].getItem()
            if (item == Items.AIR) continue
            if (ignoreEjectItem.value && ejectList.contains(item.registryName.toString())) continue
            return slot
        }
        return null
    }

    private fun getContainerSlotSize(): Int {
        if (mc.currentScreen !is GuiContainer) return 0
        return mc.player.openContainer.inventorySlots.size - 36
    }
}
