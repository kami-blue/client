package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils.pause
import me.zeroeightsix.kami.util.BaritoneUtils.unpause
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.InventoryUtils.countItem
import me.zeroeightsix.kami.util.InventoryUtils.getSlotsFullInv
import me.zeroeightsix.kami.util.InventoryUtils.getSlotsFullInvNoHotbar
import me.zeroeightsix.kami.util.InventoryUtils.getSlotsNoHotbar
import me.zeroeightsix.kami.util.InventoryUtils.moveAllToSlot
import me.zeroeightsix.kami.util.InventoryUtils.moveToSlot
import me.zeroeightsix.kami.util.InventoryUtils.quickMoveSlot
import me.zeroeightsix.kami.util.InventoryUtils.swapSlot
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.item.Item
import net.minecraft.item.Item.getIdFromItem
import net.minecraft.item.ItemStack
import kotlin.math.ceil

/**
 * Created by Xiaro on 7/13/20
 */

@Module.Info(
        name = "InventoryManager",
        category = Module.Category.PLAYER,
        description = "Manages your inventory automatically"
)
// TODO: ADD AUTO EJECT
// TODO: ADD FULL INVENTORY ONLY OPTION
class InventoryManager : Module() {
    private val DEFAULT_EJECT_CONFIG = "minecraft:grass,minecraft:dirt,minecraft:netherrack,minecraft:gravel,minecraft:sand,minecraft:stone,minecraft:cobblestone"
    private val autoRefill = register(Settings.b("AutoRefill", true))
    private val buildingMode = register(Settings.booleanBuilder("BuildingMode").withValue(false).withVisibility { autoRefill.value }.build())
    val buildingBlockID: Setting<Int> = register(Settings.integerBuilder("BuildingBlockID").withValue(0).withVisibility { false }.build())
    private val refillThreshold = register(Settings.integerBuilder("RefillThreshold").withValue(16).withRange(1, 63).withVisibility { autoRefill.value }.build())
    private val itemSaver = register(Settings.b("ItemSaver", false))
    private val duraThreshold = register(Settings.integerBuilder("DurabilityThreshold").withValue(5).withRange(1, 50).withVisibility { itemSaver.value }.build())
    private val autoEject = register(Settings.b("AutoEject", false))
    private val fullInventoryOnly = register(Settings.booleanBuilder("FullInventoryOnly").withValue(false).withVisibility { autoEject.value })
    private val pauseMovement: Setting<Boolean> = register(Settings.b("PauseMovement", true))
    private val delayTicks = register(Settings.floatBuilder("DelayTicks").withValue(2.0f).withRange(0.0f, 5.0f).build())
    private val ejectList = register(Settings.stringBuilder("EjectList").withValue(DEFAULT_EJECT_CONFIG).withVisibility { false }.build())
    private val ejectItems: Set<Item>? = null

    fun extGet(): String? {
        return extGetInternal(null)
    }

    // Add entry by arbitrary user-provided string
    fun extAdd(s: String) {
        ejectList.value = extGetInternal(null) + ", " + s
    }

    // Remove entry by arbitrary user-provided string
    fun extRemove(s: String?) {
        ejectList.value = extGetInternal(Item.getByNameOrId(s?: return))
    }

    // Clears the list.
    fun extClear() {
        ejectList.value = ""
    }

    // Resets the list to default
    fun extDefaults() {
        extClear()
        extAdd(DEFAULT_EJECT_CONFIG)
    }

    // Set the list to 1 value
    fun extSet(s: String) {
        extClear()
        extAdd(s)
    }

    private fun extGetInternal(filter: Item?): String? {
        val sb = StringBuilder()
        var notFirst = false
        if (ejectItems != null) {
            for (i in ejectItems) {
                if (i === filter) continue
                if (notFirst) sb.append(", ")
                notFirst = true
                sb.append(Item.REGISTRY.getNameForObject(i))
            }
        }
        return sb.toString()
    }

    enum class State {
        IDLE, SAVING_ITEM, REFILLING_BUILDING, REFILLING, EJECTING
    }

    private var paused = false
    private var currentState = State.IDLE

    @EventHandler
    private val playerTravelListener = Listener(EventHook { event: PlayerTravelEvent ->
        if (mc.player == null || mc.player.isSpectator || !paused || !pauseMovement.value) return@EventHook
        mc.player.setVelocity(0.0, mc.player.motionY, 0.0)
        event.cancel()
    })

    override fun onToggle() {
        InventoryUtils.inProgress = false
        unpause()
    }

    override fun onUpdate() {
        if (mc.player.isSpectator || mc.currentScreen is GuiInventory || InventoryUtils.inProgress) return

        setState()

        when (currentState) {
            State.SAVING_ITEM -> saveItem()
            State.REFILLING_BUILDING -> refillBuilding()
            State.REFILLING -> refill()
            else -> { }
        }
    }

    private fun setState() {
        currentState = when {
            saveItemCheck() -> State.SAVING_ITEM
            refillBuildingCheck() -> State.REFILLING_BUILDING
            refillCheck() -> State.REFILLING
            else -> State.IDLE
        }

        if (currentState != State.IDLE && !paused && pauseMovement.value) {
            pause()
            paused = true
        } else if (currentState == State.IDLE && paused) {
            unpause()
            paused = false
        }
    }

    /* State checks */
    private fun saveItemCheck(): Boolean {
        if (!itemSaver.value) return false

        return checkDamage(mc.player.inventory.currentItem) ?: false
    }

    private fun refillBuildingCheck(): Boolean {
        if (!autoRefill.value || !buildingMode.value || buildingBlockID.value.toInt() == 0) return false

        val itemCount = countItem(0, 8, buildingBlockID.value.toInt())
        return itemCount <= refillThreshold.value || (findRefillableSlotBuilding() != null && InventoryUtils.inProgress)
    }

    private fun refillCheck(): Boolean {
        if (!autoRefill.value) return false

        return findRefillableSlot() != null || (findRefillableSlot() != null && InventoryUtils.inProgress)
    }
    /* End of state checks */

    /* Tasks */
    private fun saveItem() {
        val currentSlot = mc.player.inventory.currentItem
        val currentItemID = getIdFromItem(mc.player.inventory.getCurrentItem().getItem())

        if (autoRefill.value && findUndamagedItem(currentItemID) != null) { /* Replaces item if autoRefill is on and a undamaged (not reached threshold) item found */
            val targetSlot = findUndamagedItem(currentItemID)!!
            moveToSlot(currentSlot + 36, targetSlot, (delayTicks.value * 50).toLong())
        } else if (getSlotsFullInv(9, 44, 0) != null) { /* Moves item to inventory if empty slot found in inventory */
            moveToSlot(currentSlot + 36, getSlotsFullInv(9, 44, 0)!![0], (delayTicks.value * 50).toLong())
        } else {
            var hasAvailableSlot = false
            for (i in 0..8) {
                hasAvailableSlot = !(checkDamage(i) ?: false)
            }
            if (hasAvailableSlot) { /* Swaps to another slot if no empty slot found in hotbar */
                swapSlot((currentSlot + 1) % 9)
            } else { /* Drops item if all other slots in hotbar contains damaged items */
                mc.player.dropItem(false)
            }
        }
    }

    private fun refillBuilding() {
        val slots = getSlotsFullInvNoHotbar(buildingBlockID.value)
        quickMoveSlot(slots?.get(0) ?: return, (delayTicks.value * 50).toLong())
    }

    private fun refill() {
        val slotTo = (findRefillableSlot() ?: return) + 36
        val stackTo = mc.player.inventoryContainer.inventory[slotTo]
        val slotFrom = findCompatibleStack(stackTo) ?: return
        moveAllToSlot(slotFrom, slotTo, (delayTicks.value * 50).toLong())
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
    private fun findUndamagedItem(ItemID: Int): Int? {
        val slots = getSlotsFullInv(9, 44, ItemID) ?: return null
        for (i in slots.indices) {
            val currentSlot = slots[i]
            if (checkDamageFullInv(currentSlot) == false) return currentSlot
        }
        return null
    }

    private fun findRefillableSlotBuilding(): Int? {
        if (getSlotsNoHotbar(buildingBlockID.value) == null) return null
        for (i in 0..8) {
            val currentStack = mc.player.inventory.getStackInSlot(i)
            if (getIdFromItem(currentStack.getItem()) != buildingBlockID.value) continue
            if (!currentStack.isStackable || currentStack.count >= currentStack.maxStackSize) continue
            return i
        }
        return null
    }

    private fun findRefillableSlot(): Int? {
        for (i in 0..8) {
            val currentStack = mc.player.inventory.getStackInSlot(i)
            val stackTarget = ceil(currentStack.maxStackSize / 64.0f * refillThreshold.value).toInt()
            if (currentStack.isEmpty) continue
            if (!currentStack.isStackable || currentStack.count > stackTarget) continue
            if (getIdFromItem(currentStack.getItem()) == buildingBlockID.value && buildingMode.value) continue
            if (findCompatibleStack(currentStack) == null) continue
            return i
        }
        return null
    }

    private fun findCompatibleStack(stack: ItemStack): Int? {
        val slots = getSlotsFullInvNoHotbar(getIdFromItem(stack.getItem())) ?: return null
        for (i in slots.indices) {
            val currentSlot = slots[i]
            if (isCompatibleStacks(stack, mc.player.inventoryContainer.inventory[currentSlot])) return currentSlot
        }
        return null
    }

    private fun isCompatibleStacks(stack1: ItemStack, stack2: ItemStack): Boolean {
        return stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack2, stack1)
    }
}