package org.kamiblue.client.module.modules.player

import net.minecraft.client.settings.KeyBinding
import net.minecraft.init.Items
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTool
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.module.modules.combat.CombatSetting
import org.kamiblue.client.process.PauseProcess.pauseBaritone
import org.kamiblue.client.process.PauseProcess.unpauseBaritone
import org.kamiblue.client.util.*
import org.kamiblue.client.util.combat.CombatUtils
import org.kamiblue.client.util.items.*
import org.kamiblue.client.util.threads.runSafe
import org.kamiblue.client.util.threads.safeListener

internal object AutoEat : Module(
    name = "AutoEat",
    description = "Automatically eat when hungry",
    category = Category.PLAYER
) {
    private val belowHunger by setting("Below Hunger", 15, 1..20, 1)
    private val belowHealth by setting("Below Health", 10, 1..20, 1)
    private val eatBadFood by setting("Eat Bad Food", false)
    private val pauseBaritone by setting("Pause Baritone", true)

    private val timer = TickTimer(TimeUnit.TICKS)
    private var lastSlot = -1
    private var eating = false

    init {
        onDisable {
            stopEating()
            swapBack()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START || CombatSetting.isActive()) return@safeListener

            if (shouldEat()) {
                if (isValid(player.heldItemOffhand)) {
                    eat(EnumHand.OFF_HAND)
                } else if (swapToFood()) {
                    eat(EnumHand.MAIN_HAND)
                }
            } else if (timer.tick(2)) {
                if (eating) {
                    stopEating()
                } else {
                    swapBack()
                }
            }
        }
    }

    private fun SafeClientEvent.shouldEat() =
        player.foodStats.foodLevel < belowHunger
            || CombatUtils.getHealthSmart(player) < belowHealth

    private fun SafeClientEvent.eat(hand: EnumHand) {
        if (pauseBaritone) pauseBaritone()

        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true)
        playerController.processRightClick(player, world, hand)

        timer.reset()
        eating = true
    }

    private fun stopEating() {
        unpauseBaritone()

        runSafe {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
            playerController.onStoppedUsingItem(player)
        }

        eating = false
    }

    private fun swapBack() {
        val slot = lastSlot
        if (slot == -1) return

        lastSlot = -1
        runSafe {
            swapToSlot(slot)
        }
    }

    private fun SafeClientEvent.swapToFood(): Boolean {
        if (isValid(player.heldItemMainhand)) return true

        lastSlot = player.inventory.currentItem
        val hasFoodInSlot = swapToItem<ItemFood> { isValid(it) }

        if (!hasFoodInSlot) {
            lastSlot = -1
            moveFoodToHotbar()
        }

        return false
    }

    private fun SafeClientEvent.moveFoodToHotbar() {
        val slotFrom = player.storageSlots.firstItem<ItemFood, Slot> {
            isValid(it)
        } ?: return

        moveToHotbar(slotFrom) {
            val item = it.item
            item !is ItemTool && item !is ItemBlock
        }
    }

    private fun isValid(itemStack: ItemStack): Boolean {
        val item = itemStack.item

        return item is ItemFood
            && item != Items.CHORUS_FRUIT
            && (eatBadFood || !isBadFood(itemStack, item))
    }

    private fun isBadFood(itemStack: ItemStack, item: ItemFood) =
        item == Items.ROTTEN_FLESH
            || item == Items.SPIDER_EYE
            || item == Items.POISONOUS_POTATO
            || item == Items.FISH && (itemStack.metadata == 3 || itemStack.metadata == 2) // Puffer fish, Clown fish
}