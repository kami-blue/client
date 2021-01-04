package me.zeroeightsix.kami.gui.hudgui.elements.combat

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.graphics.RenderUtils2D
import me.zeroeightsix.kami.util.graphics.VertexHelper
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

object CombatItemCount : LabelHud(
    name = "CombatItemCount",
    category = HudElement.Category.COMBAT,
    description = "Counts combat items like gapples, crystal, etc"
) {

    private val arrow = setting("Arrow", true)
    private val crystal = setting("Crystal", true)
    private val gapple = setting("Gapple", true)
    private val xpBottle = setting("XpBottle", true)
    private val pearl = setting("Pearl", false)
    private val chorusFruit = setting("ChorusFruit", false)
    private val showIcon = setting("ShowIcon", false)
    private val horizontal = setting("Horizontal", true, { showIcon.value })

    private val itemSettings = linkedMapOf(
        arrow to arrayOf(Items.ARROW, Items.SPECTRAL_ARROW, Items.TIPPED_ARROW),
        crystal to arrayOf(Items.END_CRYSTAL),
        gapple to arrayOf(Items.GOLDEN_APPLE),
        xpBottle to arrayOf(Items.EXPERIENCE_BOTTLE),
        pearl to arrayOf(Items.ENDER_PEARL),
        chorusFruit to arrayOf(Items.CHORUS_FRUIT)
    )

    private val itemStacks = arrayOf(
        ItemStack(Items.ARROW, -1),
        ItemStack(Items.END_CRYSTAL, -1),
        ItemStack(Items.GOLDEN_APPLE, -1, 1),
        ItemStack(Items.EXPERIENCE_BOTTLE, -1),
        ItemStack(Items.ENDER_PEARL, -1),
        ItemStack(Items.CHORUS_FRUIT, -1)
    )

    override val maxWidth: Float
        get() = if (showIcon.value) {
            if (horizontal.value) 20.0f * itemSettings.keys.count { it.value }
            else 20.0f
        } else {
            displayText.getWidth()
        }

    override val maxHeight: Float
        get() = if (showIcon.value) {
            if (horizontal.value) 20.0f
            else 20.0f * itemSettings.keys.count { it.value }
        } else {
            displayText.getHeight(2)
        }

    override fun updateText() {
        for ((index, entry) in itemSettings.entries.withIndex()) {
            val count = if (entry.key.value) entry.value.sumBy { InventoryUtils.countItemAll(it) } else -1

            if (showIcon.value) {
                itemStacks[index].count = count + 1 // Weird way to get around Minecraft item count check
            } else if (count > -1) {
                displayText.add(entry.key.name, primaryColor)
                displayText.addLine("x$count", secondaryColor)
            }
        }
    }

    override fun renderHud(vertexHelper: VertexHelper) {
        if (showIcon.value) {
            GlStateManager.pushMatrix()

            for (itemStack in itemStacks) {
                if (itemStack.count == -1) continue
                RenderUtils2D.drawItem(itemStack, 2, 2, (itemStack.count - 1).toString())
                if (horizontal.value) GlStateManager.translate(20.0f, 0.0f, 0.0f)
                else GlStateManager.translate(0.0f, 20.0f, 0.0f)
            }

            GlStateManager.popMatrix()
        } else {
            super.renderHud(vertexHelper)
        }
    }

}