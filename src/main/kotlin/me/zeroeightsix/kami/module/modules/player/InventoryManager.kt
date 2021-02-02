package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.mixin.extension.syncCurrentPlayItem
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.process.PauseProcess.pauseBaritone
import me.zeroeightsix.kami.process.PauseProcess.unpauseBaritone
import me.zeroeightsix.kami.setting.settings.impl.collection.CollectionSetting
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.items.*
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.extension.ceilToInt

internal object InventoryManager : Module(
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

    private val autoRefill by setting("AutoRefill", true)
    private val buildingMode by setting("BuildingMode", false, { autoRefill })
    var buildingBlockID by setting("BuildingBlockID", 0, 0..1000, 1, { false })
    private val refillThreshold by setting("RefillThreshold", 16, 1..63, 1, { autoRefill })
    private val itemSaver by setting("ItemSaver", false)
    private val duraThreshold by setting("DurabilityThreshold", 5, 1..50, 1, { itemSaver })
    private val autoEject by setting("AutoEject", false)
    private val fullOnly by setting("OnlyAtFull", false, { autoEject })
    private val pauseMovement by setting("PauseMovement", true)
    private val delay by setting("DelayTicks", 1, 0..20, 1)
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
        onDisable {
            paused = false
            unpauseBaritone()
        }

        safeListener<PlayerTravelEvent> {
            if (player.isSpectator || !pauseMovement || !paused) return@safeListener
            player.setVelocity(0.0, mc.player.motionY, 0.0)
            it.cancel()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START || player.isSpectator || mc.currentScreen is GuiContainer) return@safeListener

            if (!timer.tick(delay.toLong())) return@safeListener

            setState()

            when (currentState) {
                State.SAVING_ITEM -> saveItem()
                State.REFILLING_BUILDING -> refillBuilding()
                State.REFILLING -> refill()
                State.EJECTING -> eject()
                State.IDLE -> removeHoldingItem()
            }

            playerController.syncCurrentPlayItem()
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

        paused = if (currentState != State.IDLE && pauseMovement) {
            pauseBaritone()
            true
        } else {
            unpauseBaritone()
            false
        }
    }

    /* State checks */
    private fun SafeClientEvent.saveItemCheck(): Boolean {
        return itemSaver && checkDamage(player.heldItemMainhand)
    }

    private fun SafeClientEvent.refillBuildingCheck(): Boolean {
        if (!autoRefill || !buildingMode || buildingBlockID == 0) return false

        val totalCount = player.inventorySlots.countID(buildingBlockID)
        val hotbarCount = player.hotbarSlots.countID(buildingBlockID)

        return totalCount >= refillThreshold
            && (hotbarCount < refillThreshold
            || (getRefillableSlotBuilding() != null && currentState == State.REFILLING_BUILDING))
    }

    private fun SafeClientEvent.refillCheck(): Boolean {
        return autoRefill && getRefillableSlot() != null
    }

    private fun SafeClientEvent.ejectCheck(): Boolean {
        return autoEject && ejectList.isNotEmpty()
            && (!fullOnly || player.inventorySlots.firstEmpty() == null)
            && getEjectSlot() != null
    }
    /* End of state checks */

    /* Tasks */
    private fun SafeClientEvent.saveItem() {
        val currentSlot = player.inventory.currentItem
        val itemStack = player.heldItemMainhand

        val undamagedItem = getUndamagedItem(itemStack.item.id)
        val emptySlot = player.inventorySlots.firstEmpty()

        when {
            autoRefill && undamagedItem != null -> {
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
        player.storageSlots.firstID(buildingBlockID)?.let {
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
            && itemStack.itemDamage > itemStack.maxDamage * (1.0f - duraThreshold / 100.0f)

    private fun SafeClientEvent.getRefillableSlotBuilding(): Slot? {
        if (player.storageSlots.firstID(buildingBlockID) == null) return null

        return player.hotbarSlots.firstID(buildingBlockID) {
            it.isStackable && it.count < it.maxStackSize
        }
    }

    private fun SafeClientEvent.getRefillableSlot(): Slot? {
        return player.hotbarSlots.firstByStack {
            !it.isEmpty
                && (!buildingMode || it.item.id != buildingBlockID)
                && (!autoEject || !ejectList.contains(it.item.registryName.toString()))
                && it.isStackable
                && it.count < (it.maxStackSize / 64.0f * refillThreshold).ceilToInt()
                && getCompatibleStack(it) != null
        }
    }

    private fun SafeClientEvent.getCompatibleStack(stack: ItemStack): Slot? {
        return player.storageSlots.firstByStack {
            stack.isItemEqual(it) && ItemStack.areItemStackTagsEqual(stack, it)
        }
    }

    private fun SafeClientEvent.getEjectSlot(): Slot? {
        return player.inventorySlots.firstByStack {
            !it.isEmpty
                && (!buildingMode || it.item.id != buildingBlockID)
                && ejectList.contains(it.item.registryName.toString())
        }
    }
}