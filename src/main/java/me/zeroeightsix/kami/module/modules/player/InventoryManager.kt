package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.mixin.extension.syncCurrentPlayItem
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.setting.settings.impl.collection.CollectionSetting
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.extension.ceilToInt
import org.kamiblue.event.listener.listener

object InventoryManager : Module(
    name = "InventoryManager",
    category = Category.PLAYER,
    description = "Manages your inventory automatically"
) {
    private val defaultEjectList = linkedSetOf(
        "minecraft:grass",
        "minecraft:dirt",
        "minecraft:netherrack",
        "minecraft:gravel",
        "minecraft:sand",
        "minecraft:stone",
        "minecraft:cobblestone"
    )

    private val autoRefill = setting("AutoRefill", true)
    private val buildingMode = setting("BuildingMode", false, { autoRefill.value })
    val buildingBlockID = setting("BuildingBlockID", 0, 0..1000, 1, { false })
    private val refillThreshold = setting("RefillThreshold", 16, 1..63, 1, { autoRefill.value })
    private val itemSaver = setting("ItemSaver", false)
    private val duraThreshold = setting("DurabilityThreshold", 5, 1..50, 1, { itemSaver.value })
    val autoEject = setting("AutoEject", false)
    private val fullOnly = setting("OnlyAtFull", false, { autoEject.value })
    private val pauseMovement = setting("PauseMovement", true)
    private val delay = setting("DelayTicks", 1, 0..20, 1)
    val ejectList = setting(CollectionSetting("EjectList", defaultEjectList))

    enum class State {
        IDLE, SAVING_ITEM, REFILLING_BUILDING, REFILLING, EJECTING
    }

    private var currentState = State.IDLE
    private var paused = false
    private val timer = TickTimer(TimeUnit.TICKS)

    override fun isActive(): Boolean {
        return isEnabled && currentState != State.IDLE
    }

    init {
        onToggle {
            paused = false
            BaritoneUtils.unpause()
        }

        listener<PlayerTravelEvent> {
            if (mc.player == null || mc.player.isSpectator || !pauseMovement.value || !paused) return@listener
            mc.player.setVelocity(0.0, mc.player.motionY, 0.0)
            it.cancel()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START || player.isSpectator || mc.currentScreen is GuiContainer) return@safeListener
            if (!timer.tick(delay.value.toLong())) return@safeListener
            setState()
            if (currentState == State.IDLE) InventoryUtils.removeHoldingItem()
            when (currentState) {
                State.SAVING_ITEM -> saveItem()
                State.REFILLING_BUILDING -> refillBuilding()
                State.REFILLING -> refill()
                State.EJECTING -> eject()
                else -> {
                    // this is fine, Java meme
                }
            }
            playerController.syncCurrentPlayItem()
        }
    }

    private fun setState() {
        currentState = when {
            saveItemCheck() -> State.SAVING_ITEM
            refillBuildingCheck() -> State.REFILLING_BUILDING
            refillCheck() -> State.REFILLING
            ejectCheck() -> State.EJECTING
            else -> State.IDLE
        }

        if (currentState != State.IDLE && pauseMovement.value && !paused) {
            BaritoneUtils.pause()
            paused = true
        } else if (currentState == State.IDLE && paused) {
            BaritoneUtils.unpause()
            paused = false
        }
    }

    /* State checks */
    private fun saveItemCheck(): Boolean {
        if (!itemSaver.value) return false

        return checkDamage(mc.player.inventory.currentItem) ?: false
    }

    private fun refillBuildingCheck(): Boolean {
        if (!autoRefill.value || !buildingMode.value || buildingBlockID.value == 0) return false

        val totalCount = InventoryUtils.countItem(0, 35, buildingBlockID.value)
        val hotbarCount = InventoryUtils.countItem(0, 8, buildingBlockID.value)
        return totalCount > refillThreshold.value && (hotbarCount <= refillThreshold.value ||
            (getRefillableSlotBuilding() != null && currentState == State.REFILLING_BUILDING))

    }

    private fun refillCheck(): Boolean {
        if (!autoRefill.value) return false

        return getRefillableSlot() != null
    }

    private fun ejectCheck(): Boolean {
        if (!autoEject.value || ejectList.isEmpty()) return false

        return getEjectSlot() != null && ((InventoryUtils.getSlots(0, 35, 0) == null && fullOnly.value) || !fullOnly.value)
    }
    /* End of state checks */

    /* Tasks */
    private fun saveItem() {
        val currentSlot = mc.player.inventory.currentItem
        val currentItemID = mc.player.inventory.getCurrentItem().item.id

        if (autoRefill.value && getUndamagedItem(currentItemID) != null) { /* Replaces item if autoRefill is on and a undamaged (not reached threshold) item found */
            val targetSlot = getUndamagedItem(currentItemID)!!
            InventoryUtils.moveToSlot(currentSlot + 36, targetSlot)
        } else if (InventoryUtils.getSlotsFullInv(9, 44, 0) != null) { /* Moves item to inventory if empty slot found in inventory */
            InventoryUtils.moveToSlot(currentSlot + 36, InventoryUtils.getSlotsFullInv(9, 44, 0)!![0])
        } else {
            var hasAvailableSlot = false
            for (i in 0..8) {
                hasAvailableSlot = !(checkDamage(i) ?: false)
            }
            if (hasAvailableSlot) { /* Swaps to another slot if no empty slot found in hotbar */
                InventoryUtils.swapSlot((currentSlot + 1) % 9)
            } else { /* Drops item if all other slots in hotbar contains damaged items */
                mc.player.dropItem(false)
            }
        }
    }

    private fun refillBuilding() {
        val slots = InventoryUtils.getSlotsFullInvNoHotbar(buildingBlockID.value)
        InventoryUtils.quickMoveSlot(slots?.get(0) ?: return)
    }

    private fun refill() {
        val slotTo = getRefillableSlot() ?: return
        val stackTo = mc.player.inventoryContainer.inventory[slotTo]
        val slotFrom = getCompatibleStack(stackTo) ?: return
        InventoryUtils.moveToSlot(slotFrom, slotTo)
    }

    private fun eject() {
        val slot = getEjectSlot() ?: return
        InventoryUtils.throwAllInSlot(slot)
    }
    /* End of tasks */

    /**
     * Checks damage of item in given slot
     *
     * @return True if durability is lower than the value of [duraThreshold],
     * false if not lower than the value of [duraThreshold],
     * null if item is not damageable
     */
    private fun checkDamage(slot: Int): Boolean? {
        return if (!mc.player.inventory.getStackInSlot(slot).isEmpty) {
            val item = mc.player.inventory.getStackInSlot(slot)
            if (item.isItemStackDamageable) {
                item.itemDamage > item.maxDamage * (1.0f - duraThreshold.value.toFloat() / 100.0f)
            } else null
        } else null
    }

    /**
     * Same as [checkDamage], but uses full inventory slot
     *
     * @return True if durability is lower than the value of [duraThreshold],
     * false if not lower than the value of [duraThreshold],
     * null if item is not damageable or slot is empty
     */
    private fun checkDamageFullInv(slot: Int): Boolean? {
        return if (!mc.player.inventoryContainer.inventory[slot].isEmpty) {
            val item = mc.player.inventoryContainer.inventory[slot]
            if (item.isItemStackDamageable) {
                item.itemDamage > item.maxDamage * (1.0f - duraThreshold.value.toFloat() / 100.0f)
            } else null
        } else null
    }

    /**
     * Finds undamaged item with given ID in inventory, and return its slot
     *
     * @return Full inventory slot if undamaged item found, else return null
     */
    private fun getUndamagedItem(ItemID: Int): Int? {
        val slots = InventoryUtils.getSlotsFullInv(9, 44, ItemID) ?: return null
        for (slot in slots) {
            if (checkDamageFullInv(slot) == false) return slot
        }
        return null
    }

    private fun getRefillableSlotBuilding(): Int? {
        if (InventoryUtils.getSlotsNoHotbar(buildingBlockID.value) == null) return null
        for (i in 36..45) {
            val currentStack = mc.player.inventoryContainer.inventory[i]
            if (currentStack.item.id != buildingBlockID.value) continue
            if (!currentStack.isStackable || currentStack.count >= currentStack.maxStackSize) continue
            return i
        }
        return null
    }

    private fun getRefillableSlot(): Int? {
        for (i in 36..45) {
            val currentStack = mc.player.inventoryContainer.inventory[i]
            val stackTarget = (currentStack.maxStackSize / 64.0f * refillThreshold.value).ceilToInt()
            if (currentStack.isEmpty) continue
            if (!currentStack.isStackable || currentStack.count > stackTarget) continue
            if (currentStack.item.id == buildingBlockID.value && buildingMode.value) continue
            if (ejectList.contains(currentStack.item.registryName.toString()) && autoEject.value) continue
            if (getCompatibleStack(currentStack) == null) continue
            return i
        }
        return null
    }

    private fun getCompatibleStack(stack: ItemStack): Int? {
        val slots = InventoryUtils.getSlotsFullInvNoHotbar(stack.item.id) ?: return null
        for (slot in slots) {
            if (isCompatibleStacks(stack, mc.player.inventoryContainer.inventory[slot])) return slot
        }
        return null
    }

    private fun isCompatibleStacks(stack1: ItemStack, stack2: ItemStack): Boolean {
        return stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack2, stack1)
    }

    private fun getEjectSlot(): Int? {
        for (slot in 9..44) {
            val currentStack = mc.player.inventoryContainer.inventory[slot]
            if (((currentStack.item.id != buildingBlockID.value && buildingMode.value) || !buildingMode.value) && /* Don't throw the building block */
                ejectList.contains(currentStack.item.registryName.toString())) {
                return slot
            }
        }
        return null
    }
}