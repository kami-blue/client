package me.zeroeightsix.kami.module.modules.player

import baritone.api.BaritoneAPI
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.modules.client.Baritone
import me.zeroeightsix.kami.module.modules.combat.Aura
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils.pause
import me.zeroeightsix.kami.util.BaritoneUtils.unpause
import net.minecraft.client.settings.KeyBinding
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand

/**
 * Created by 086 on 8/04/2018.
 * Updated by polymer on 09/03/20
 * Updated by dominikaaaa on 20/03/20
 * Updated by An-En on 24/03/20
 * Updated by Dewy on the 17th of May, 2020
 * Updated by Afel 05/25/20
 * Updated by Xiaro on 02/08/20
 */
@Module.Info(
        name = "AutoEat",
        description = "Automatically eat when hungry",
        category = Module.Category.PLAYER
)
class AutoEat : Module() {
    private val foodLevel = register(Settings.integerBuilder("BelowHunger").withValue(15).withMinimum(1).withMaximum(20).build())
    private val healthLevel = register(Settings.integerBuilder("BelowHealth").withValue(8).withMinimum(1).withMaximum(20).build())
    private var pauseBaritone = register(Settings.b("PauseBaritone", true))

    private var lastSlot = -1
    var eating = false

    private fun isValid(stack: ItemStack, food: Int): Boolean {
        return passItemCheck(stack) && stack.getItem() is ItemFood && foodLevel.value - food >= (stack.getItem() as ItemFood).getHealAmount(stack) ||
                passItemCheck(stack) && stack.getItem() is ItemFood && healthLevel.value - (mc.player.health + mc.player.absorptionAmount) > 0f
    }

    private fun passItemCheck(stack: ItemStack): Boolean {
        val item: Item = stack.getItem()
        if (item === Items.ROTTEN_FLESH
                || item === Items.SPIDER_EYE
                || item === Items.POISONOUS_POTATO
                || (item === Items.FISH && (stack.metadata == 3 || stack.metadata == 2)) // Pufferfish, Clown fish
                || item === Items.CHORUS_FRUIT) {
            return false
        }
        return true
    }

    override fun onUpdate() {
        if (mc.player == null || (ModuleManager.isModuleEnabled(Aura::class.java) && ModuleManager.getModuleT(Aura::class.java)!!.isAttacking)) return

        if (eating && !mc.player.isHandActive) {
            if (lastSlot != -1) {
                mc.player.inventory.currentItem = lastSlot
                lastSlot = -1
            }
            eating = false
            unpause()

            BaritoneAPI.getSettings().allowInventory.value = false

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
            return
        }

        if (eating) return

        val stats = mc.player.getFoodStats()

        if (isValid(mc.player.heldItemOffhand, stats.foodLevel)) {
            mc.player.activeHand = EnumHand.OFF_HAND

            if (pauseBaritone.value && !eating) {
                pause()
            }

            eating = true
            BaritoneAPI.getSettings().allowInventory.value = ModuleManager.getModuleT(Baritone::class.java)!!.allowInventory.value

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
                    return
                }
            }
        }
    }

    override fun onDisable() {
        unpause()
    }
}