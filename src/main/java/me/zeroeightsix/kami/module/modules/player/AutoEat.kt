package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.client.Baritone
import me.zeroeightsix.kami.module.modules.combat.CombatSetting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.BaritoneUtils.pause
import me.zeroeightsix.kami.util.BaritoneUtils.unpause
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.client.settings.KeyBinding
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand

@Module.Info(
        name = "AutoEat",
        description = "Automatically eat when hungry",
        category = Module.Category.PLAYER
)
object AutoEat : Module() {
    private val foodLevel = register(Settings.integerBuilder("BelowHunger").withValue(15).withRange(1, 20).withStep(1))
    private val healthLevel = register(Settings.integerBuilder("BelowHealth").withValue(8).withRange(1, 20).withStep(1))
    private val pauseBaritone = register(Settings.b("PauseBaritone", true))

    private var lastSlot = -1
    var eating = false

    private fun isValid(stack: ItemStack, food: Int): Boolean {
        return passItemCheck(stack) && stack.getItem() is ItemFood && foodLevel.value - food >= (stack.getItem() as ItemFood).getHealAmount(stack) ||
                passItemCheck(stack) && stack.getItem() is ItemFood && healthLevel.value - (mc.player.health + mc.player.absorptionAmount) > 0f
    }

    private fun passItemCheck(stack: ItemStack): Boolean {
        val item: Item = stack.getItem()
        if (item == Items.ROTTEN_FLESH
                || item == Items.SPIDER_EYE
                || item == Items.POISONOUS_POTATO
                || (item == Items.FISH && (stack.metadata == 3 || stack.metadata == 2)) // Pufferfish, Clown fish
                || item == Items.CHORUS_FRUIT) {
            return false
        }
        return true
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

                BaritoneUtils.settings()?.allowInventory?.value = false

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
                return@listener
            }

            if (eating) return@listener

            val stats = mc.player.getFoodStats()

            if (isValid(mc.player.heldItemOffhand, stats.foodLevel)) {
                mc.player.activeHand = EnumHand.OFF_HAND

                if (pauseBaritone.value && !eating) {
                    pause()
                }

                eating = true
                BaritoneUtils.settings()?.allowInventory?.value = Baritone.allowInventory.value

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true)
                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND)
            } else {
                for (i in 0..8) {
                    if (isValid(mc.player.inventory.getStackInSlot(i), stats.foodLevel)) {
                        lastSlot = mc.player.inventory.currentItem
                        mc.player.inventory.currentItem = i

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

    override fun onDisable() {
        unpause()
    }
}