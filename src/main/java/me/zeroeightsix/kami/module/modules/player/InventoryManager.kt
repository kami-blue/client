package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.setting.settings.impl.collection.CollectionSetting
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.items.*
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.extension.ceilToInt

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

        safeListener<PlayerTravelEvent> {
            if (player.isSpectator || !pauseMovement.value || !paused) return@safeListener
            player.setVelocity(0.0, mc.player.motionY, 0.0)
            it.cancel()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START || player.isSpectator || mc.currentScreen is GuiContainer) return@safeListener
            if (!timer.tick(delay.value.toLong())) return@safeListener

            setState()

            if (currentState == State.IDLE) removeHoldingItem()

            when (currentState) {
                State.SAVING_ITEM -> saveItem()
                State.REFILLING_BUILDING -> refillBuilding()
                State.REFILLING -> refill()
                State.EJECTING -> eject()
                else -> {
                    // this is fine, Java meme
                }
            }

            playerController.updateController()
        }
    }

    private fun SafeClientEvent.setState() {
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
    private fun SafeClientEvent.saveItemCheck(): Boolean {
        return itemSaver.value && checkDamage(player.heldItemMainhand)
    }

    private fun SafeClientEvent.refillBuildingCheck(): Boolean {
        if (!autoRefill.value || !buildingMode.value || buildingBlockID.value == 0) return false

        val totalCount = player.inventorySlots.countID(buildingBlockID.value)
        val hotbarCount = player.hotbarSlots.countID(buildingBlockID.value)

        return totalCount > refillThreshold.value
            && (hotbarCount <= refillThreshold.value
            || (getRefillableSlotBuilding() != null && currentState == State.REFILLING_BUILDING))

    }

    private fun SafeClientEvent.refillCheck(): Boolean {
        if (!autoRefill.value) return false

        return getRefillableSlot() != null
    }

    private fun SafeClientEvent.ejectCheck(): Boolean {
        if (!autoEject.value || ejectList.isEmpty()) return false

        return getEjectSlot() != null && (!fullOnly.value || player.inventorySlots.firstEmpty() == null)
    }
    /* End of state checks */

    /* Tasks */
    private fun SafeClientEvent.saveItem() {
        val currentSlot = player.inventory.currentItem
        val itemStack = player.heldItemMainhand

        val undamagedItem = getUndamagedItem(itemStack.item.id)
        val emptySlot = player.inventorySlots.firstEmpty()

        when {
            autoRefill.value && undamagedItem != null -> {
                moveToHotbar(undamagedItem.slotNumber, currentSlot)
            }
            emptySlot != null -> {
                moveToHotbar(emptySlot.slotNumber, currentSlot)
            }
            else -> {
                player.dropItem(false)
            }
        }
    }

    private fun SafeClientEvent.refillBuilding() {
        player.storageSlots.firstID(buildingBlockID.value)?.let {
            quickMoveSlot(it)
        }
    }

    private fun SafeClientEvent.refill() {
        val slotTo = getRefillableSlot() ?: return
        val slotFrom = getCompatibleStack(slotTo.stack) ?: return

        moveToSlot(slotFrom, slotTo)
    }

    private fun SafeClientEvent.eject() {
        getEjectSlot()?.let {
            throwAllInSlot(it)
        }
    }
    /* End of tasks */

    /**
     * Finds undamaged item with given ID in inventory, and return its slot
     *
     * @return Full inventory slot if undamaged item found, else return null
     */
    private fun SafeClientEvent.getUndamagedItem(itemID: Int) =
        player.storageSlots.firstID(itemID) {
            !checkDamage(it)
        }

    private fun checkDamage(itemStack: ItemStack) =
        itemStack.isItemStackDamageable
            && itemStack.itemDamage > itemStack.maxDamage * (1.0f - duraThreshold.value / 100.0f)

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

    private fun SafeClientEvent.getRefillableSlotBuilding(): Slot? {
        if (player.storageSlots.firstID(buildingBlockID.value) == null) return null

        return player.hotbarSlots.firstID(buildingBlockID.value) {
            it.isStackable && it.count < it.maxStackSize
        }
    }

    private fun SafeClientEvent.getRefillableSlot(): Slot? {
        return player.hotbarSlots.firstByStack {
            !it.isEmpty
                && (!buildingMode.value || it.item.id != buildingBlockID.value)
                && (!autoEject.value || !ejectList.contains(it.item.registryName.toString()))
                && it.isStackable
                && (it.maxStackSize / 64.0f * refillThreshold.value).ceilToInt() < refillThreshold.value
                && getCompatibleStack(it) != null
        }
    }

    private fun SafeClientEvent.getCompatibleStack(stack: ItemStack): Slot? {
        return player.storageSlots.firstID(stack.item.id) {
            isCompatibleStacks(stack, it)
        }
    }

    private fun isCompatibleStacks(stack1: ItemStack, stack2: ItemStack): Boolean {
        return stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack2, stack1)
    }

    private fun SafeClientEvent.getEjectSlot(): Slot? {
        return player.inventorySlots.firstByStack {
            !it.isEmpty
                && (!buildingMode.value || it.item.id != buildingBlockID.value)
                && ejectList.contains(it.item.registryName.toString())
        }
    }
}