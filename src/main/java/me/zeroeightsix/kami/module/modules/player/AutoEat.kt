package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.combat.CombatSetting
import me.zeroeightsix.kami.process.PauseProcess.pauseBaritone
import me.zeroeightsix.kami.process.PauseProcess.unpauseBaritone
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.items.*
import me.zeroeightsix.kami.util.threads.runSafe
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.settings.KeyBinding
import net.minecraft.init.Items
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTool
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.gameevent.TickEvent

object AutoEat : Module(
    name = "AutoEat",
    description = "Automatically eat when hungry",
    category = Category.PLAYER
) {
    private val belowHunger by setting("BelowHunger", 15, 1..20, 1)
    private val belowHealth by setting("BelowHealth", 10, 1..20, 1)
    private val eatBadFood by setting("EatBadFood", false)
    private val pauseBaritone by setting("PauseBaritone", true)

    private var lastSlot = -1
    var eating = false; private set

    init {
        onDisable {
            stopEating()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START || CombatSetting.isActive()) return@safeListener

            if (shouldEat()) {
                if (isValid(player.heldItemOffhand)) {
                    eat(EnumHand.OFF_HAND)
                } else if (swapToFood()) {
                    eat(EnumHand.MAIN_HAND)
                }
            } else if (eating) {
                stopEating()
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

        eating = true
    }

    private fun stopEating() {
        unpauseBaritone()

        runSafe {
            if (lastSlot != -1) {
                swapToSlot(lastSlot)
                lastSlot = -1
            }
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
            playerController.onStoppedUsingItem(player)
        }

        eating = false

    }

    private fun SafeClientEvent.swapToFood(): Boolean {
        if (isValid(player.heldItemMainhand)) return true

        lastSlot = player.inventory.currentItem
        val hasFoodInSlot = swapToItem<ItemFood> { isValid(it) }

        return if (!hasFoodInSlot) {
            lastSlot = -1
            moveFoodToHotbar()
            false
        } else {
            true
        }
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