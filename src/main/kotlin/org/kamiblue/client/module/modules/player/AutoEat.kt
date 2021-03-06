package org.kamiblue.client.module.modules.player

import net.minecraft.client.settings.KeyBinding
import net.minecraft.init.Items
import net.minecraft.init.MobEffects
import net.minecraft.inventory.Slot
import net.minecraft.item.*
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.process.PauseProcess.pauseBaritone
import org.kamiblue.client.process.PauseProcess.unpauseBaritone
import org.kamiblue.client.util.*
import org.kamiblue.client.util.combat.CombatUtils.scaledHealth
import org.kamiblue.client.util.items.*
import org.kamiblue.client.util.threads.runSafe
import org.kamiblue.client.util.threads.safeListener

internal object AutoEat : Module(
    name = "AutoEat",
    description = "Automatically eat when hungry",
    category = Category.PLAYER
) {
    private val belowHunger by setting("Below Hunger", 15, 1..20, 1)
    private val belowHealth by setting("Below Health", 10, 1..20, 1, description = "When to eat a golden apple")
    private val eGapOnFire by setting("Fire Prot", false, description = "Eats an enchanted golden apple whilst on fire")
    private val eatBadFood by setting("Eat Bad Food", false)
    private val pauseBaritone by setting("Pause Baritone", true)

    private var lastSlot = -1
    private var eating = false

    private var preferredLevel = PreferredFood.NORMAL

    enum class PreferredFood {
        NORMAL {
            override fun isValid(item: ItemFood, itemStack: ItemStack): Boolean {
                return item != Items.CHORUS_FRUIT && item != Items.GOLDEN_APPLE
            }
        },
        GAP {
            override fun isValid(item: ItemFood, itemStack: ItemStack): Boolean {
                return item == Items.GOLDEN_APPLE && item.getMetadata(itemStack) == 0
            }
        },
        EGAP {
            override fun isValid(item: ItemFood, itemStack: ItemStack): Boolean {
                return item == Items.GOLDEN_APPLE && item.getMetadata(itemStack) > 0
            }
        };

        abstract fun isValid(item: ItemFood, itemStack: ItemStack): Boolean
    }

    override fun isActive(): Boolean {
        return isEnabled && eating
    }

    init {
        onDisable {
            stopEating()
            swapBack()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@safeListener

            if (!player.isEntityAlive) {
                if (eating) stopEating()
                return@safeListener
            }

            preferredLevel = when {
                shouldFireProtect() -> PreferredFood.EGAP
                player.scaledHealth < belowHealth -> PreferredFood.GAP
                else -> PreferredFood.NORMAL
            }

            val hand = when {
                !shouldEat() -> {
                    null // Null = stop eating
                }
                isValid(player.heldItemOffhand, preferredLevel) -> {
                    EnumHand.OFF_HAND
                }
                isValid(player.heldItemMainhand, preferredLevel) -> {
                    EnumHand.MAIN_HAND
                }
                swapToFood(preferredLevel) -> { // If we found valid food and moved
                    // Set eating and pause then return and wait until next tick
                    startEating()
                    return@safeListener
                }
                else -> {
                    null // If we can't find any valid food then stop eating
                }
            }

            if (hand != null) {
                eat(hand)
            } else {
                // Stop eating first and swap back in the next tick
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
            || player.scaledHealth < belowHealth || shouldFireProtect()

    private fun SafeClientEvent.eat(hand: EnumHand) {
        if (!eating || !player.isHandActive || player.activeHand != hand) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true)

            // Vanilla Minecraft prioritize offhand so we need to force it using the specific hand
            playerController.processRightClick(player, world, hand)
        }

        startEating()
    }

    private fun startEating() {
        if (pauseBaritone) pauseBaritone()
        eating = true
    }

    private fun stopEating() {
        unpauseBaritone()

        runSafe {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
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

    /**
     * @return `true` if food found and moved
     */
    private fun SafeClientEvent.swapToFood(preferredLevel: PreferredFood): Boolean {
        lastSlot = player.inventory.currentItem
        val slotToSwitchTo = getSlotOfItemFood(preferredLevel, player.hotbarSlots)?.let {
            swapToSlot(it as HotbarSlot)
            true
        } ?: false

        return if (slotToSwitchTo) {
            true
        } else {
            lastSlot = -1
            moveFoodToHotbar()
        }
    }

    /**
     * @return `true` if food found and moved
     */
    private fun SafeClientEvent.moveFoodToHotbar(): Boolean {
        val slotFrom = getSlotOfItemFood(preferredLevel, player.storageSlots) ?: return false

        moveToHotbar(slotFrom) {
            val item = it.item
            item !is ItemTool && item !is ItemBlock
        }
        return true
    }

    fun SafeClientEvent.getSlotOfItemFood(preferredLevel: PreferredFood, inventory: List<Slot>): Slot? {
        return inventory.firstItem<ItemFood, Slot> {
            isValid(it, AutoEat.preferredLevel)
        } ?: when (preferredLevel) {
            PreferredFood.NORMAL -> null
            PreferredFood.GAP -> getSlotOfItemFood(PreferredFood.NORMAL, inventory)
            else -> getSlotOfItemFood(PreferredFood.GAP, inventory)
        }
    }

    private fun SafeClientEvent.isValid(itemStack: ItemStack, preferredLevel: PreferredFood): Boolean {
        val item = itemStack.item
        if ((item !is ItemFood) || (item == Items.CHORUS_FRUIT) || (isBadFood(itemStack, item) and !eatBadFood)) {
            return false
        }

        return preferredLevel.isValid(item, itemStack)
    }

    private fun SafeClientEvent.shouldFireProtect() = player.isBurning && eGapOnFire && !player.isPotionActive(MobEffects.FIRE_RESISTANCE)

    private fun isBadFood(itemStack: ItemStack, item: ItemFood) =
        item == Items.ROTTEN_FLESH
            || item == Items.SPIDER_EYE
            || item == Items.POISONOUS_POTATO
            || item == Items.FISH && (itemStack.metadata == 3 || itemStack.metadata == 2) // Puffer fish, Clown fish
}