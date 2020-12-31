package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.client.Baritone
import me.zeroeightsix.kami.module.modules.combat.CombatSetting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.BaritoneUtils.pause
import me.zeroeightsix.kami.util.BaritoneUtils.unpause
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.foodValue
import me.zeroeightsix.kami.util.id
import net.minecraft.client.settings.KeyBinding
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTool
import net.minecraft.util.EnumHand
import org.kamiblue.event.listener.listener
import kotlin.math.min

@Module.Info(
    name = "AutoEat",
    description = "Automatically eat when hungry",
    category = Module.Category.PLAYER
)
object AutoEat : Module() {
    private val foodLevel = register(Settings.integerBuilder("BelowHunger").withValue(15).withRange(1, 20).withStep(1))
    private val healthLevel = register(Settings.integerBuilder("BelowHealth").withValue(8).withRange(1, 20).withStep(1))
    private val pauseBaritone = register(Settings.b("PauseBaritone", true))
    private val eatBadFood = register(Settings.b("EatBadFood", false))

    private var lastSlot = -1
    var eating = false; private set

    override fun onDisable() {
        unpause()
        eating = false
    }

    init {
        listener<SafeTickEvent> {
            if (CombatSetting.isActive()) return@listener

            if (eating && !mc.player.isHandActive) {
                if (lastSlot != -1) {
                    mc.player.inventory.currentItem = lastSlot
                    lastSlot = -1
                }
                eating = false
                unpause()

                BaritoneUtils.settings?.allowInventory?.value = false

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
                return@listener
            }

            if (eating) return@listener

            val stats = mc.player.foodStats

            if (isValid(mc.player.heldItemOffhand, stats.foodLevel)) {
                mc.player.activeHand = EnumHand.OFF_HAND

                if (pauseBaritone.value && !eating) {
                    pause()
                }

                eating = true
                BaritoneUtils.settings?.allowInventory?.value = Baritone.allowInventory.value

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true)
                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND)
            } else {
                for (i in 0..44) {
                    val itemStack = mc.player.inventory.getStackInSlot(i)
                    if (isValid(itemStack, stats.foodLevel)) {

                        val newSlot = if (i > 8) { // not in hotbar
                            min(InventoryUtils.moveToHotbar(
                                itemStack.item.id, *avoidSlots(itemStack.item.id)
                            ) - 36, 0) // - 36 convert to hotbar
                        } else {
                            i
                        }

                        lastSlot = mc.player.inventory.currentItem
                        mc.player.inventory.currentItem = newSlot

                        if (pauseBaritone.value && !eating) {
                            pause()
                        }

                        eating = true
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true)
                        mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND)
                        return@listener
                    }
                }
            }
        }
    }

    private fun avoidSlots(initialItem: Int): IntArray {
        val invalidItems = ArrayList<Int>()

        for (i in 0..6) { // first 7 slots
            val item = mc.player.inventory.getStackInSlot(i).item

            if (item is ItemTool || item is ItemBlock) {
                invalidItems.add(item.id)
            }
        }

        return if (invalidItems.isEmpty()) {
            intArrayOf(initialItem)
        } else invalidItems.toIntArray()
    }

    private fun isValid(stack: ItemStack, food: Int): Boolean {
        val item = stack.item
        if (item !is ItemFood) return false

        return passItemCheck(stack) && (foodLevel.value - food >= item.foodValue
            || healthLevel.value - (mc.player.health + mc.player.absorptionAmount) > 0f)
    }

    private fun passItemCheck(stack: ItemStack): Boolean {
        val item = stack.item

        // Excluded Chorus Fruit since it is mainly used to teleport the player
        if (item == Items.CHORUS_FRUIT) {
            return false
        }

        // The player will not auto eat the food below if the EatBadFood setting is disabled
        if (!eatBadFood.value && (item == Items.ROTTEN_FLESH
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