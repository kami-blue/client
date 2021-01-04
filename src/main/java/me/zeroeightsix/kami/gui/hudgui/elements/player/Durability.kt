package me.zeroeightsix.kami.gui.hudgui.elements.player

import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.setting.GuiConfig.setting
import net.minecraft.util.EnumHand
import org.kamiblue.commons.utils.MathUtils

object Durability : LabelHud(
    name = "Durability",
    category = Category.PLAYER,
    description = "Durability of holding items"
) {

    private val showItemName = setting("ShowItemName", true)
    private val showOffhand = setting("ShowOffhand", false)
    private val showPercentage = setting("ShowPercentage", true)

    override fun updateText() {
        if (mc.player.heldItemMainhand.isItemStackDamageable) {
            if (showOffhand.value) displayText.add("MainHand:", secondaryColor)
            addDurabilityText(EnumHand.MAIN_HAND)
        }

        if (showOffhand.value && mc.player.heldItemOffhand.isItemStackDamageable) {
            displayText.add("OffHand:", secondaryColor)
            addDurabilityText(EnumHand.OFF_HAND)
        }
    }

    private fun addDurabilityText(hand: EnumHand) {
        val itemStack = mc.player.getHeldItem(hand)
        if (showItemName.value) displayText.add(itemStack.displayName, primaryColor)

        val durability = itemStack.maxDamage - itemStack.itemDamage
        val text = if (showPercentage.value) {
            "${MathUtils.round((durability / itemStack.maxDamage.toFloat()) * 100.0f, 1)}%"
        } else {
            "$durability/${itemStack.maxDamage}"
        }

        displayText.addLine(text, primaryColor)
    }

}