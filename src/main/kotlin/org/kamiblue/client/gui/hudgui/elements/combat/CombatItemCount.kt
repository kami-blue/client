package org.kamiblue.client.gui.hudgui.elements.combat

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.util.graphics.RenderUtils2D
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.items.allSlots
import org.kamiblue.client.util.items.countItem

internal object CombatItemCount : LabelHud(
    name = "CombatItemCount",
    category = Category.COMBAT,
    description = "Counts combat items like gapples, crystal, etc"
) {

    private val arrow = setting("Arrow", true)
    private val crystal = setting("Crystal", true)
    private val gapple = setting("Gapple", true)
    private val totem = setting("Totem", true)
    private val xpBottle = setting("Xp Bottle", true)
    private val pearl = setting("Pearl", false)
    private val chorusFruit = setting("Chorus Fruit", false)
    private val showIcon by setting("Show Icon", true)
    private val horizontal by setting("Horizontal", true, { showIcon })

    private val itemSettings = linkedMapOf(
        arrow to arrayOf(Items.ARROW, Items.SPECTRAL_ARROW, Items.TIPPED_ARROW),
        crystal to arrayOf(Items.END_CRYSTAL),
        gapple to arrayOf(Items.GOLDEN_APPLE),
        totem to arrayOf(Items.TOTEM_OF_UNDYING),
        xpBottle to arrayOf(Items.EXPERIENCE_BOTTLE),
        pearl to arrayOf(Items.ENDER_PEARL),
        chorusFruit to arrayOf(Items.CHORUS_FRUIT)
    )

    private val itemStacks = arrayOf(
        ItemStack(Items.ARROW, -1),
        ItemStack(Items.END_CRYSTAL, -1),
        ItemStack(Items.GOLDEN_APPLE, -1, 1),
        ItemStack(Items.TOTEM_OF_UNDYING, -1),
        ItemStack(Items.EXPERIENCE_BOTTLE, -1),
        ItemStack(Items.ENDER_PEARL, -1),
        ItemStack(Items.CHORUS_FRUIT, -1)
    )

    override val hudWidth: Float
        get() = if (showIcon) {
            if (horizontal) 20.0f * itemSettings.keys.count { it.value }
            else 20.0f
        } else {
            displayText.getWidth()
        }

    override val hudHeight: Float
        get() = if (showIcon) {
            if (horizontal) 20.0f
            else 20.0f * itemSettings.keys.count { it.value }
        } else {
            displayText.getHeight(2)
        }

    override fun SafeClientEvent.updateText() {
        val slots = player.allSlots

        for ((index, entry) in itemSettings.entries.withIndex()) {
            val count = if (entry.key.value) entry.value.sumBy { slots.countItem(it) }
            else -1

            if (showIcon) {
                itemStacks[index].count = count + 1 // Weird way to get around Minecraft item count check
            } else if (count > -1) {
                displayText.add(entry.key.name, primaryColor)
                displayText.addLine("x$count", secondaryColor)
            }
        }
    }

    override fun renderHud(vertexHelper: VertexHelper) {
        if (showIcon) {
            GlStateManager.pushMatrix()

            for (itemStack in itemStacks) {
                if (itemStack.count == 0) continue
                RenderUtils2D.drawItem(itemStack, 2, 2, (itemStack.count - 1).toString())
                if (horizontal) GlStateManager.translate(20.0f, 0.0f, 0.0f)
                else GlStateManager.translate(0.0f, 20.0f, 0.0f)
            }

            GlStateManager.popMatrix()
        } else {
            super.renderHud(vertexHelper)
        }
    }

}