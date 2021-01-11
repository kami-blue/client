package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.combat.CombatSetting
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.BaritoneUtils.pause
import me.zeroeightsix.kami.util.BaritoneUtils.unpause
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.items.*
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
    private val foodLevel by setting("BelowHunger", 15, 1..20, 1)
    private val healthLevel by setting("BelowHealth", 10, 1..20, 1)
    private val eatBadFood by setting("EatBadFood", false)
    private val pauseBaritone by setting("PauseBaritone", true)

    private var lastSlot = -1
    var eating = false; private set

    init {
        onDisable {
            unpause()
            eating = false
        }

        safeListener<TickEvent.ClientTickEvent> { event ->
            if (CombatSetting.isActive()) return@safeListener

            if (eating) {
                if (!player.isHandActive && event.phase == TickEvent.Phase.END) stopEating()
                return@safeListener
            }

            if (event.phase != TickEvent.Phase.START) return@safeListener

            if (isValid(player.heldItemOffhand)) {
                startEating(EnumHand.OFF_HAND)
            } else if (swapToFood()) {
                startEating(EnumHand.MAIN_HAND)
            }
        }
    }

    private fun SafeClientEvent.swapToFood(): Boolean {
        lastSlot = player.inventory.currentItem

        val hasFoodInSlot = swapToItem<ItemFood> { isValid(it) }

        if (!hasFoodInSlot) {
            lastSlot = -1

            val slotFrom = player.storageSlots.firstItem<ItemFood, Slot> {
                isValid(it)
            } ?: return false

            moveToHotbar(slotFrom) {
                val item = it.item
                item !is ItemTool && item !is ItemBlock
            }

            return false
        }

        return true
    }

    private fun SafeClientEvent.stopEating() {
        if (lastSlot != -1) {
            swapToSlot(lastSlot)
            lastSlot = -1
        }

        eating = false
        unpause()

        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
    }

    private fun SafeClientEvent.startEating(hand: EnumHand) {
        if (pauseBaritone && !eating) {
            pause()
        }

        player.activeHand = hand

        eating = true
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true)
        playerController.processRightClick(player, world, hand)
    }

    private fun SafeClientEvent.isValid(stack: ItemStack): Boolean {
        val item = stack.item
        if (item !is ItemFood) return false

        return passItemCheck(stack) && (player.foodStats.foodLevel < foodLevel
            || CombatUtils.getHealthSmart(player) < healthLevel)
    }

    private fun passItemCheck(stack: ItemStack): Boolean {
        val item = stack.item

        // Excluded Chorus Fruit since it is mainly used to teleport the player
        if (item == Items.CHORUS_FRUIT) {
            return false
        }

        // The player will not auto eat the food below if the EatBadFood setting is disabled
        if (!eatBadFood && (item == Items.ROTTEN_FLESH
                || item == Items.SPIDER_EYE
                || item == Items.POISONOUS_POTATO
                || (item == Items.FISH && (stack.metadata == 3 || stack.metadata == 2)) // Puffer fish, Clown fish
                || item == Items.CHORUS_FRUIT)) {
            return false
        }

        // If EatBadFood is enabled, just allow them to eat it
        return true
    }
}