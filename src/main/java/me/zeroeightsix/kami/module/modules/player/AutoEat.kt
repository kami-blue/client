package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
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
 */
@Module.Info(
        name = "AutoEat",
        description = "Automatically eat when hungry",
        category = Module.Category.PLAYER
)
class AutoEat : Module() {
    private val foodLevel = register(Settings.integerBuilder("Below Hunger").withValue(15).withMinimum(1).withMaximum(20).build())
    private val healthLevel = register(Settings.integerBuilder("Below Health").withValue(8).withMinimum(1).withMaximum(20).build())

    private var lastSlot = -1
    private var eating = false

    private fun isValid(stack: ItemStack, food: Int): Boolean {
        return passItemCheck(stack.getItem()) && stack.getItem() is ItemFood && foodLevel.value - food >= (stack.getItem() as ItemFood).getHealAmount(stack) ||
               passItemCheck(stack.getItem()) && stack.getItem() is ItemFood && healthLevel.value - (mc.player.health + mc.player.absorptionAmount) > 0f
    }

    private fun passItemCheck(item: Item): Boolean {
        if (item === Items.ROTTEN_FLESH) return false
        if (item === Items.SPIDER_EYE) return false
        if (item === Items.POISONOUS_POTATO) return false
        return !(item === Items.FISH && ItemStack(Items.FISH).getItemDamage() == 3)
    }

    override fun onUpdate() {
        if (mc.player == null) return

        if (eating && !mc.player.isHandActive) {
            if (lastSlot != -1) {
                mc.player.inventory.currentItem = lastSlot
                lastSlot = -1
            }
            eating = false

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
            return
        }

        if (eating) return

        val stats = mc.player.getFoodStats()

        if (isValid(mc.player.heldItemOffhand, stats.foodLevel)) {
            mc.player.activeHand = EnumHand.OFF_HAND
            eating = true

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true)
            mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND)
        } else {
            for (i in 0..8) {
                if (isValid(mc.player.inventory.getStackInSlot(i), stats.foodLevel)) {
                    lastSlot = mc.player.inventory.currentItem
                    mc.player.inventory.currentItem = i
                    eating = true

                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true)
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND)
                    return
                }
            }
        }
    }
}