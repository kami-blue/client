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
import org.kamiblue.commons.extension.next
import java.util.function.BiPredicate

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

    enum class PreferredFood : BiPredicate<ItemStack, ItemFood> {
        GAP {
            override fun test(itemStack: ItemStack, item: ItemFood): Boolean {
                return item == Items.GOLDEN_APPLE && itemStack.metadata == 0
            }
        },
        EGAP {
            override fun test(itemStack: ItemStack, item: ItemFood): Boolean {
                return item == Items.GOLDEN_APPLE && itemStack.metadata == 1
            }
        },
        NORMAL {
            override fun test(itemStack: ItemStack, item: ItemFood): Boolean {
                return item != Items.GOLDEN_APPLE
            }
        }
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

            val preferredFood = when {
                needFireProtect() -> PreferredFood.EGAP
                player.scaledHealth < belowHealth -> PreferredFood.GAP
                else -> PreferredFood.NORMAL
            }

            val hand = when {
                !shouldEat(preferredFood) -> {
                    null // Null = stop eating
                }
                isValidAndPreferred(player.heldItemOffhand, preferredFood) -> {
                    EnumHand.OFF_HAND
                }
                isValidAndPreferred(player.heldItemMainhand, preferredFood) -> {
                    EnumHand.MAIN_HAND
                }
                swapToFood(preferredFood) -> { // If we found valid food and moved
                    // Set eating and pause then wait until next tick
                    startEating()
                    return@safeListener
                }
                // If there isn't valid food in inventory or directly valid food in hand, do recursive check on items in hand
                isValidAndPreferredRecursive(player.heldItemOffhand, preferredFood) -> {
                    EnumHand.OFF_HAND
                }
                isValidAndPreferredRecursive(player.heldItemMainhand, preferredFood) -> {
                    EnumHand.MAIN_HAND
                }
                else -> {
                    null // If we can't find any valid food then stop eating
                }
            }

            eatOrStop(hand)
        }
    }

    private fun SafeClientEvent.needFireProtect() =
        eGapOnFire
            && player.isBurning
            && !player.isPotionActive(MobEffects.FIRE_RESISTANCE)
            && player.allSlots.firstItem(Items.GOLDEN_APPLE) { it.metadata == 1 } != null

    private fun SafeClientEvent.shouldEat(preferredFood: PreferredFood) =
        player.foodStats.foodLevel < belowHunger
            || preferredFood != PreferredFood.NORMAL

    private fun SafeClientEvent.eatOrStop(hand: EnumHand?) {
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
    private fun SafeClientEvent.swapToFood(preferredFood: PreferredFood): Boolean {
        lastSlot = player.inventory.currentItem
        val slotToSwitchTo = getFoodSlot(preferredFood, player.hotbarSlots)?.let {
            swapToSlot(it as HotbarSlot)
            true
        } ?: false

        return if (slotToSwitchTo) {
            true
        } else {
            lastSlot = -1
            moveFoodToHotbar(preferredFood)
        }
    }

    /**
     * @return `true` if food found and moved
     */
    private fun SafeClientEvent.moveFoodToHotbar(preferredFood: PreferredFood): Boolean {
        val slotFrom = getFoodSlot(preferredFood, player.storageSlots) ?: return false

        moveToHotbar(slotFrom) {
            val item = it.item
            item !is ItemTool && item !is ItemBlock
        }

        return true
    }

    private fun SafeClientEvent.getFoodSlot(preferredFood: PreferredFood, inventory: List<Slot>, attempts: Int = 3): Slot? {
        return inventory.firstItem<ItemFood, Slot> {
            isValidAndPreferred(it, preferredFood)
        } ?: if (attempts > 1) {
            getFoodSlot(preferredFood.next(), inventory, attempts - 1)
        } else {
            null
        }
    }

    private fun SafeClientEvent.isValidAndPreferredRecursive(itemStack: ItemStack, preferredFood: PreferredFood): Boolean {
        val item = itemStack.item

        return item is ItemFood
            && isValid(itemStack, item)
            && isPreferredRecursive(itemStack, item, preferredFood)
    }

    private fun SafeClientEvent.isPreferredRecursive(itemStack: ItemStack, item: ItemFood, preferredFood: PreferredFood, attempts: Int = 3): Boolean {
        return preferredFood.test(itemStack, item)
            || isPreferredRecursive(itemStack, item, preferredFood.next(), attempts - 1)
    }

    private fun SafeClientEvent.isValidAndPreferred(itemStack: ItemStack, preferredFood: PreferredFood): Boolean {
        val item = itemStack.item

        return item is ItemFood
            && isValid(itemStack, item)
            && preferredFood.test(itemStack, item)
    }

    private fun SafeClientEvent.isValid(itemStack: ItemStack, item: ItemFood): Boolean {
        return item != Items.CHORUS_FRUIT
            && (eatBadFood || !isBadFood(itemStack, item))
            && player.canEat(item == Items.GOLDEN_APPLE)
    }

    private fun isBadFood(itemStack: ItemStack, item: ItemFood) =
        item == Items.ROTTEN_FLESH
            || item == Items.SPIDER_EYE
            || item == Items.POISONOUS_POTATO
            || item == Items.FISH && (itemStack.metadata == 3 || itemStack.metadata == 2) // Puffer fish, Clown fish
}