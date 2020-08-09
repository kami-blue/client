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
import me.zeroeightsix.kami.util.InventoryUtils.getSlots
import me.zeroeightsix.kami.util.InventoryUtils.getSlotsFullInv
import me.zeroeightsix.kami.util.InventoryUtils.getSlotsFullInvNoHotbar
import me.zeroeightsix.kami.util.InventoryUtils.getSlotsNoHotbar
import me.zeroeightsix.kami.util.InventoryUtils.moveToSlot
import me.zeroeightsix.kami.util.InventoryUtils.quickMoveSlot
import me.zeroeightsix.kami.util.InventoryUtils.swapSlot
import me.zeroeightsix.kami.util.InventoryUtils.throwAllInSlot
import net.minecraft.client.gui.inventory.GuiContainer
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
class InventoryManager : Module() {
    private val defaultEjectList = "minecraft:grass,minecraft:dirt,minecraft:netherrack,minecraft:gravel,minecraft:sand,minecraft:stone,minecraft:cobblestone"

    private val autoRefill = register(Settings.b("AutoRefill", true))
    private val buildingMode = register(Settings.booleanBuilder("BuildingMode").withValue(false).withVisibility { autoRefill.value }.build())
    val buildingBlockID: Setting<Int> = register(Settings.integerBuilder("BuildingBlockID").withValue(0).withVisibility { false }.build())
    private val refillThreshold = register(Settings.integerBuilder("RefillThreshold").withValue(16).withRange(1, 63).withVisibility { autoRefill.value }.build())
    private val itemSaver = register(Settings.b("ItemSaver", false))
    private val duraThreshold = register(Settings.integerBuilder("DurabilityThreshold").withValue(5).withRange(1, 50).withVisibility { itemSaver.value }.build())
    val autoEject = register(Settings.b("AutoEject", false))
    private val fullOnly = register(Settings.booleanBuilder("OnlyAtFull").withValue(false).withVisibility { autoEject.value })
    private val pauseMovement: Setting<Boolean> = register(Settings.b("PauseMovement", true))
    private val delayTicks = register(Settings.floatBuilder("DelayTicks").withValue(1.0f).withRange(0.0f, 5.0f).build())
    private val ejectList = register(Settings.stringBuilder("EjectList").withValue(defaultEjectList).withVisibility { false }.build())

    /* Eject list */
    lateinit var ejectArrayList: ArrayList<String>

    private fun ejectGetArrayList(): ArrayList<String> {
        return ArrayList(ejectList.value.split(","))
    }

    fun ejectGetString(): String {
        return ejectArrayList.joinToString()
    }

    fun ejectAdd(name: String) {
        ejectArrayList.add(name)
        ejectList.value = ejectGetString()
    }

    fun ejectRemove(name: String) {
        ejectArrayList.remove(name)
        ejectList.value = ejectGetString()
    }

    fun ejectSet(name: String) {
        ejectClear()
        ejectAdd(name)
    }

    fun ejectDefault() {
        ejectList.value = defaultEjectList
        ejectArrayList = ejectGetArrayList()
    }

    fun ejectClear() {
        ejectList.value = ""
        ejectArrayList.clear()
    }
    /* End of eject list */

    enum class State {
        IDLE, SAVING_ITEM, REFILLING_BUILDING, REFILLING, EJECTING
    }

    private var paused = false
    private var currentState = State.IDLE

    @EventHandler
    private val playerTravelListener = Listener(EventHook { event: PlayerTravelEvent ->
        if (mc.player == null || mc.player.isSpectator || !paused || !pauseMovement.value) return@EventHook
        mc.player.setVelocity(0.0, mc.player.motionY, 0.0)
    })

    override fun onEnable() {
        ejectArrayList = ejectGetArrayList()
    }

    override fun onToggle() {
        InventoryUtils.inProgress = false
        unpause()
    }

    override fun onUpdate() {
        if (mc.player.isSpectator || mc.currentScreen is GuiContainer) return
        setState()
        if (InventoryUtils.inProgress) return
        when (currentState) {
            State.SAVING_ITEM -> saveItem()
            State.REFILLING_BUILDING -> refillBuilding()
            State.REFILLING -> refill()
            State.EJECTING -> eject()
            else -> {
            }
        }
        mc.playerController.syncCurrentPlayItem()
    }

    private fun setState() {
        currentState = when {
            saveItemCheck() -> State.SAVING_ITEM
            refillBuildingCheck() -> State.REFILLING_BUILDING
            refillCheck() -> State.REFILLING
            ejectCheck() -> State.EJECTING
            else -> State.IDLE
        }

        if (currentState != State.IDLE && currentState != State.EJECTING && !paused && pauseMovement.value) {
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

        val totalCount = countItem(0, 35, buildingBlockID.value.toInt())
        val hotbarCount = countItem(0, 8, buildingBlockID.value.toInt())
        return totalCount > refillThreshold.value && (hotbarCount <= refillThreshold.value ||
                (getRefillableSlotBuilding() != null && currentState == State.REFILLING_BUILDING))

    }

    private fun refillCheck(): Boolean {
        if (!autoRefill.value) return false

        return getRefillableSlot() != null
    }

    private fun ejectCheck(): Boolean {
        if (!autoEject.value || ejectArrayList.isEmpty()) return false

        return getEjectSlot() != null && ((getSlots(0, 35, 0) == null && fullOnly.value) || !fullOnly.value)
    }
    /* End of state checks */

    /* Tasks */
    private fun saveItem() {
        val currentSlot = mc.player.inventory.currentItem
        val currentItemID = getIdFromItem(mc.player.inventory.getCurrentItem().getItem())

        if (autoRefill.value && getUndamagedItem(currentItemID) != null) { /* Replaces item if autoRefill is on and a undamaged (not reached threshold) item found */
            val targetSlot = getUndamagedItem(currentItemID)!!
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
        val slotTo = (getRefillableSlot() ?: return) + 36
        val stackTo = mc.player.inventoryContainer.inventory[slotTo]
        val slotFrom = getCompatibleStack(stackTo) ?: return
        moveToSlot(slotFrom, slotTo, (delayTicks.value * 50).toLong())
    }

    private fun eject() {
        val slot = getEjectSlot() ?: return
        throwAllInSlot(slot, (delayTicks.value * 50).toLong())
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
        val slots = getSlotsFullInv(9, 44, ItemID) ?: return null
        for (i in slots.indices) {
            val currentSlot = slots[i]
            if (checkDamageFullInv(currentSlot) == false) return currentSlot
        }
        return null
    }

    private fun getRefillableSlotBuilding(): Int? {
        if (getSlotsNoHotbar(buildingBlockID.value) == null) return null
        for (i in 0..8) {
            val currentStack = mc.player.inventory.getStackInSlot(i)
            if (getIdFromItem(currentStack.getItem()) != buildingBlockID.value) continue
            if (!currentStack.isStackable || currentStack.count >= currentStack.maxStackSize) continue
            return i
        }
        return null
    }

    private fun getRefillableSlot(): Int? {
        for (i in 0..8) {
            val currentStack = mc.player.inventory.getStackInSlot(i)
            val stackTarget = ceil(currentStack.maxStackSize / 64.0f * refillThreshold.value).toInt()
            if (currentStack.isEmpty) continue
            if (!currentStack.isStackable || currentStack.count > stackTarget) continue
            if (getIdFromItem(currentStack.getItem()) == buildingBlockID.value && buildingMode.value) continue
            if (ejectArrayList.contains(currentStack.getItem().registryName.toString()) && autoEject.value) continue
            if (getCompatibleStack(currentStack) == null) continue
            return i
        }
        return null
    }

    private fun getCompatibleStack(stack: ItemStack): Int? {
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

    private fun getEjectSlot(): Int? {
        for (slot in 9..44) {
            val currentStack = mc.player.inventoryContainer.inventory[slot]
            if (((getIdFromItem(currentStack.getItem()) != buildingBlockID.value && buildingMode.value) || !buildingMode.value) && /* Don't throw the building block */
                    ejectArrayList.contains(currentStack.getItem().registryName.toString())) {
                return slot
            }
        }
        return null
    }
}