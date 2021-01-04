package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.mixin.extension.syncCurrentPlayItem
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.extension.ceilToInt
import org.kamiblue.event.listener.listener

@Module.Info(
        name = "InventoryManager",
        category = Module.Category.PLAYER,
        description = "Manages your inventory automatically"
)
object InventoryManager : Module() {
    private const val defaultEjectList = "minecraft:grass,minecraft:dirt,minecraft:netherrack,minecraft:gravel,minecraft:sand,minecraft:stone,minecraft:cobblestone"

    private val autoRefill = register(Settings.b("AutoRefill", true))
    private val buildingMode = register(Settings.booleanBuilder("BuildingMode").withValue(false).withVisibility { autoRefill.value })
    val buildingBlockID = register(Settings.integerBuilder("BuildingBlockID").withValue(0).withVisibility { false })
    private val refillThreshold = register(Settings.integerBuilder("RefillThreshold").withValue(16).withRange(1, 63).withStep(1).withVisibility { autoRefill.value })
    private val itemSaver = register(Settings.b("ItemSaver", false))
    private val duraThreshold = register(Settings.integerBuilder("DurabilityThreshold").withValue(5).withRange(1, 50).withStep(1).withVisibility { itemSaver.value })
    val autoEject = register(Settings.b("AutoEject", false))
    private val fullOnly = register(Settings.booleanBuilder("OnlyAtFull").withValue(false).withVisibility { autoEject.value })
    private val pauseMovement = register(Settings.b("PauseMovement", true))
    private val ejectList = register(Settings.stringBuilder("EjectList").withValue(defaultEjectList).withVisibility { false })
    private val delay = register(Settings.integerBuilder("DelayTicks").withValue(1).withRange(0, 20).withStep(1))

    /* Eject list */
    var ejectArrayList = ejectGetArrayList(); private set

    private fun ejectGetArrayList(): ArrayList<String> {
        return ArrayList(ejectList.value.split(","))
    }

    fun ejectGetString(): String {
        return ejectArrayList.joinToString(separator = ",")
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

    private var currentState = State.IDLE
    private var paused = false
    private val timer = TickTimer(TimeUnit.TICKS)

    override fun isActive(): Boolean {
        return isEnabled && currentState != State.IDLE
    }

    override fun onEnable() {
        ejectArrayList = ejectGetArrayList()
    }

    override fun onToggle() {
        paused = false
        BaritoneUtils.unpause()
    }

    init {
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
        if (!autoRefill.value || !buildingMode.value || buildingBlockID.value.toInt() == 0) return false

        val totalCount = InventoryUtils.countItem(0, 35, buildingBlockID.value.toInt())
        val hotbarCount = InventoryUtils.countItem(0, 8, buildingBlockID.value.toInt())
        return totalCount > refillThreshold.value && (hotbarCount <= refillThreshold.value ||
                (getRefillableSlotBuilding() != null && currentState == State.REFILLING_BUILDING))

    }

    private fun refillCheck(): Boolean {
        if (!autoRefill.value) return false

        return getRefillableSlot() != null
    }

    private fun ejectCheck(): Boolean {
        if (!autoEject.value || ejectArrayList.isEmpty()) return false

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
            if (ejectArrayList.contains(currentStack.item.registryName.toString()) && autoEject.value) continue
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
                    ejectArrayList.contains(currentStack.item.registryName.toString())) {
                return slot
            }
        }
        return null
    }
}