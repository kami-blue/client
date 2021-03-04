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
    private val belowHealth by setting("Gap Health", 10, 1..20, 1)
    private val eGapOnFire by setting("EGap on fire", true)
    private val eatBadFood by setting("Eat Bad Food", false)
    private val pauseBaritone by setting("Pause Baritone", true)

    private var lastSlot = -1
    private var eating = false

    private var prefferedLevel = PreferedLevel.NORMAL

    enum class PreferedLevel {
        NORMAL {
            override fun isValid(item: ItemFood, itemStack: ItemStack): Boolean {
                return item != Items.CHORUS_FRUIT && !(GAP.isValid(item, itemStack)) && !(EGAP.isValid(item, itemStack))
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

            prefferedLevel = when {
                (player.isBurning && eGapOnFire && !player.isPotionActive(MobEffects.FIRE_RESISTANCE)) -> PreferedLevel.EGAP
                player.scaledHealth < belowHealth -> PreferedLevel.GAP
                else -> PreferedLevel.NORMAL
            }

            val hand = when {
                !shouldEat() -> {
                    null // Null = stop eating
                }
                isValid(player.heldItemOffhand, prefferedLevel) -> {
                    EnumHand.OFF_HAND
                }
                isValid(player.heldItemMainhand, prefferedLevel) -> {
                    EnumHand.MAIN_HAND
                }
                swapToFood(prefferedLevel) -> { // If we found valid food and moved
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
            || player.scaledHealth < belowHealth || (player.isBurning && eGapOnFire && !player.isPotionActive(MobEffects.FIRE_RESISTANCE))

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
    private fun SafeClientEvent.swapToFood(prefferedLevel: PreferedLevel): Boolean {
        lastSlot = player.inventory.currentItem
        val slotToSwitchTo = getSlotOfItemFood(prefferedLevel, player.hotbarSlots)?.let {
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
        val slotFrom = getSlotOfItemFood(prefferedLevel, player.storageSlots) ?: return false

        moveToHotbar(slotFrom) {
            val item = it.item
            item !is ItemTool && item !is ItemBlock
        }
        return true
    }

    fun SafeClientEvent.getSlotOfItemFood(prefferedLevel: PreferedLevel, inventory: List<Slot>): Slot? {
        return inventory.firstItem<ItemFood, Slot> {
            isValid(it, AutoEat.prefferedLevel)
        } ?: when (prefferedLevel) {
            PreferedLevel.NORMAL -> null
            PreferedLevel.GAP -> getSlotOfItemFood(PreferedLevel.NORMAL, inventory)
            else -> getSlotOfItemFood(PreferedLevel.GAP, inventory)
        }
    }

    private fun SafeClientEvent.isValid(itemStack: ItemStack, prefferedLevel: PreferedLevel): Boolean {
        val item = itemStack.item
        if ((item !is ItemFood) || (item == Items.CHORUS_FRUIT) || (isBadFood(itemStack, item) and !eatBadFood)) {
            return false
        }

        return prefferedLevel.isValid(item, itemStack)
    }

    private fun isBadFood(itemStack: ItemStack, item: ItemFood) =
        item == Items.ROTTEN_FLESH
            || item == Items.SPIDER_EYE
            || item == Items.POISONOUS_POTATO
            || item == Items.FISH && (itemStack.metadata == 3 || itemStack.metadata == 2) // Puffer fish, Clown fish
}