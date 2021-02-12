package org.kamiblue.client.module.modules.movement

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemElytra
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.safeListener

internal object ElytraReplace : Module(
    name = "ElytraReplace",
    description = "Automatically swap and replace your chestplate and elytra.",
    category = Category.MOVEMENT
) {
    private val inventoryMode = setting("Inventory", false)
    private val autoChest = setting("Auto Chest", false)
    private val elytraFlightCheck = setting("ElytraFlight Check", true)
    private val logToChat = setting("Missing Warning", false)
    private val playSound = setting("Play Sound", false, { logToChat.value })
    private val logThreshold = setting("Warning Threshold", 2, 1..10, 1, { logToChat.value })
    private val threshold = setting("Damage Threshold", 7, 1..50, 1)

    private var elytraCount = 0
    private var chestPlateCount = 0
    private var shouldSendFinalWarning = true

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (!inventoryMode.value && mc.currentScreen is GuiContainer) {
                return@safeListener
            }

            getElytraChestCount()

            if (elytraCount == 0 && shouldSendFinalWarning) {
                sendFinalElytraWarning()
            }

            if (player.onGround && autoChest.value) {
                swapToChest()
            } else if (shouldAttemptElytraSwap()) {
                var shouldSwap = isCurrentElytraBroken()
                if (autoChest.value) {
                    shouldSwap = shouldSwap || !(player.inventory.armorInventory[2].item === Items.ELYTRA) // if current elytra broken or no elytra found in chest area
                }

                if (shouldSwap) {
                    val success = swapToElytra()
                    if (success) {
                        sendEquipNotification()
                    }
                }
            }
        }
    }

    private fun getElytraChestCount() {
        elytraCount = 0
        chestPlateCount = 0
        for (i in 0..44) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (stack.item === Items.ELYTRA && !isItemBroken(stack)) {
                elytraCount += 1
                if (!shouldSendFinalWarning) { // if we send the final warning but gained elytras afterwards - we can send the message again
                    shouldSendFinalWarning = true
                }
            } else if (stack.item is ItemArmor && !isItemBroken(stack)) {
                val armor = stack.item as ItemArmor
                val armorType = armor.armorType.ordinal - 2
                if (armorType == 2) {
                    chestPlateCount += 1
                }
            }
        }
    }

    // if we should check elytraflight, then we will swap if it is enabled
    // if we don't need to check for elytraflight, then just swap
    private fun shouldAttemptElytraSwap(): Boolean {
        return !elytraFlightCheck.value || ElytraFlight.isEnabled
    }


    private fun swapToChest() {
        if (chestPlateCount == 0) {
            return
        }

        if (!emptySlotAvailable()) {
            return
        }

        var slot = getSlotOfBestChestPlate()
        if (slot == -1) return // no chest or current chest is better

        if (slot < 9) slot += 36 // hotbar is slots 0 to 8, convert the slot if it's hotbar

        if (mc.player.inventory.armorInventory[2].isEmpty) { // place chest into empty chest slot
            mc.playerController.windowClick(0, slot, 0, ClickType.QUICK_MOVE, mc.player)
            return
        } else { // swap chestplate from inventory with whatever you were wearing, if you're already wearing non-armor in chest slot
            mc.playerController.windowClick(0, 6, 0,
                ClickType.QUICK_MOVE, mc.player)
            mc.playerController.windowClick(0, slot, 0,
                ClickType.QUICK_MOVE, mc.player)
            return
        }
    }

    private fun swapToElytra(): Boolean { // returns success

        if (elytraCount == 0) {
            return false
        }

        if (!emptySlotAvailable()) {
            return false
        }

        var slot: Int = getSlotOfNextElytra()

        if (slot == -1) { // this shouldn't happen as we check elytra count earlier, but the check is here for peace of mind.
            return false
        }

        if (slot < 9) slot += 36 // hotbar is slots 0 to 8, convert the slot if it's hotbar

        return if (mc.player.inventory.armorInventory[2].isEmpty) { // place new elytra in empty chest slot
            mc.playerController.windowClick(0, slot, 0, ClickType.QUICK_MOVE, mc.player)
            true
        } else { // switch non-broken elytra with whatever was previously in the chest slot
            mc.playerController.windowClick(0, 6, 0,
                ClickType.QUICK_MOVE, mc.player)
            mc.playerController.windowClick(0, slot, 0,
                ClickType.QUICK_MOVE, mc.player)
            true
        }
    }

    private fun isCurrentElytraBroken(): Boolean { // (100 * damage / max damage) >= (100 - 70)
        return if (mc.player.inventory.armorInventory[2].maxDamage == 0) {
            false
        } else {
            mc.player.inventory.armorInventory[2].item === Items.ELYTRA && isItemBroken(mc.player.inventory.armorInventory[2])
        }
    }


    // snagged from AutoArmor
    private fun getSlotOfBestChestPlate(): Int {
        var bestArmorSlot = -1
        var bestArmorValue = -1

        // check armor slot first
        val chestArmor = mc.player.inventory.armorItemInSlot(2)
        if (chestArmor.item is ItemArmor) {
            bestArmorValue = (chestArmor.item as ItemArmor).damageReduceAmount
        }

        (0..35).forEach { slot ->
            val stack = mc.player.inventory.getStackInSlot(slot)
            if (stack.item !is ItemArmor) return@forEach

            val armor = stack.item as ItemArmor
            val armorType = armor.armorType.ordinal - 2

            if (armorType != 2) return@forEach // not chestplate

            if (stack.count > 1) return@forEach // should this be the case if stacked armor exists on some servers

            val armorValue = armor.damageReduceAmount

            if (armorValue > bestArmorValue) {
                bestArmorSlot = slot
                bestArmorValue = armorValue
            }
        }
        return bestArmorSlot
    }


    private fun getSlotOfNextElytra(): Int {
        (0..44).forEach { slot ->
            val stack = mc.player.inventory.getStackInSlot(slot)
            if (stack.item !is ItemElytra) return@forEach

            if (stack.count > 1) return@forEach

            if (!isItemBroken(stack)) {
                return slot
            }
        }
        return -1
    }

    private fun isItemBroken(itemStack: ItemStack): Boolean { // (100 * damage / max damage) >= (100 - 70)
        return if (itemStack.maxDamage == 0) {
            false
        } else {
            itemStack.maxDamage - itemStack.itemDamage <= threshold.value
        }
    }

    private fun emptySlotAvailable(): Boolean {
        return mc.player.inventory.firstEmptyStack != -1
    }

    override fun getHudInfo(): String {
        return elytraCount.toString()
    }

    private fun sendEquipNotification() {
        sendAlert()
        if (logToChat.value && elytraCount == 1) {
            MessageSendHelper.sendChatMessage("$chatName You equipped your last elytra.")
        } else if (logToChat.value && elytraCount <= logThreshold.value) {
            MessageSendHelper.sendChatMessage("$chatName You have $elytraCount elytra(s) left.")
        }
    }


    private fun sendFinalElytraWarning() {

        if (!isCurrentElytraBroken()) { // check to ensure there is an actual elytra in the chest slot
            return
        }

        if (logToChat.value) {
            MessageSendHelper.sendChatMessage("$chatName Your last elytra has reached your durability threshold.")
        }
        if (playSound.value) {
            sendBadAlert()
        }
        shouldSendFinalWarning = false
    }

    private fun sendAlert() {
        if (logToChat.value && playSound.value && (elytraCount <= logThreshold.value)) {
            mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
        }
    }

    private fun sendBadAlert() {
        if (logToChat.value && playSound.value && (elytraCount <= logThreshold.value)) {
            mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, 1.0f))
        }
    }


}
