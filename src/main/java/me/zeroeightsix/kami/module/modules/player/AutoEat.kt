package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.client.Baritone
import me.zeroeightsix.kami.module.modules.combat.CombatSetting
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.BaritoneUtils.pause
import me.zeroeightsix.kami.util.BaritoneUtils.unpause
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.foodValue
import me.zeroeightsix.kami.util.id
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.settings.KeyBinding
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTool
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.min

@Module.Info(
    name = "AutoEat",
    description = "Automatically eat when hungry",
    category = Module.Category.PLAYER
)
object AutoEat : Module() {
    private val foodLevel = setting("BelowHunger", 15, 1..20, 1)
    private val healthLevel = setting("BelowHealth", 8, 1..20, 1)
    private val eatBadFood = setting("EatBadFood", true)
    private val pauseBaritone = setting("PauseBaritone", true)

    private var lastSlot = -1
    var eating = false; private set

    override fun onDisable() {
        unpause()
        eating = false
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (CombatSetting.isActive()) return@safeListener

            if (eating && !player.isHandActive) {
                if (lastSlot != -1) {
                    player.inventory.currentItem = lastSlot
                    lastSlot = -1
                }
                eating = false
                unpause()

                BaritoneUtils.settings?.allowInventory?.value = false

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
                return@safeListener
            }

            if (eating) return@safeListener

            val stats = player.foodStats

            if (isValid(player.heldItemOffhand, stats.foodLevel)) {
                player.activeHand = EnumHand.OFF_HAND

                if (pauseBaritone.value && !eating) {
                    pause()
                }

                eating = true
                BaritoneUtils.settings?.allowInventory?.value = Baritone.allowInventory.value

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true)
                playerController.processRightClick(player, world, EnumHand.OFF_HAND)
            } else {
                for (i in 0..44) {
                    val itemStack = player.inventory.getStackInSlot(i)
                    if (isValid(itemStack, stats.foodLevel)) {

                        val newSlot = if (i > 8) { // not in hotbar
                            min(InventoryUtils.moveToHotbar(
                                itemStack.item.id, *avoidSlots(itemStack.item.id)
                            ) - 36, 0) // - 36 convert to hotbar
                        } else {
                            i
                        }

                        lastSlot = player.inventory.currentItem
                        player.inventory.currentItem = newSlot

                        if (pauseBaritone.value && !eating) {
                            pause()
                        }

                        eating = true
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true)
                        playerController.processRightClick(player, world, EnumHand.MAIN_HAND)
                        return@safeListener
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.avoidSlots(initialItem: Int): IntArray {
        val invalidItems = ArrayList<Int>()

        for (i in 0..6) { // first 7 slots
            val item = player.inventory.getStackInSlot(i).item

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