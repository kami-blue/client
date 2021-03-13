package org.kamiblue.client.gui.hudgui.elements.combat

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.gui.hudgui.HudElement
import org.kamiblue.client.util.color.ColorGradient
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.RenderUtils2D
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.graphics.font.FontRenderAdapter
import org.kamiblue.client.util.graphics.font.HAlign
import org.kamiblue.client.util.graphics.font.VAlign
import org.kamiblue.client.util.items.allSlots
import org.kamiblue.client.util.items.countItem
import org.kamiblue.client.util.math.Vec2d
import org.kamiblue.client.util.threads.runSafe
import org.kamiblue.client.util.threads.safeAsyncListener
import org.kamiblue.commons.utils.MathUtils
import kotlin.math.max

internal object Armor : HudElement(
    name = "Armor",
    category = Category.COMBAT,
    description = "Show the durability of armor and the count of them"
) {

    private val classic by setting("Classic", false)
    private val armorCount by setting("ArmorCount", true)
    private val countElytras by setting("CountElytras", false, { armorCount })
    private val durabilityPercentage by setting("Durability Percentage", true)
    private val durabilityBar by setting("Durability Bar", false)

    override val hudWidth: Float
        get() = if (classic) {
            80.0f
        } else {
            renderStringWidth
        }

    override val hudHeight: Float
        get() = if (classic) {
            40.0f
        } else {
            80.0f
        }

    private var stringWidth = 120.0f
    private var renderStringWidth = 120.0f

    private val armorCounts = IntArray(4)
    private val duraColorGradient = ColorGradient(
        0f to ColorHolder(200, 20, 20),
        50f to ColorHolder(240, 220, 20),
        100f to ColorHolder(20, 232, 20)
    )

    init {
        safeAsyncListener<TickEvent.ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END) return@safeAsyncListener

            val slots = player.allSlots

            armorCounts[0] = slots.countItem(Items.DIAMOND_HELMET)
            armorCounts[1] = slots.countItem(
                if (countElytras && player.inventory.getStackInSlot(38).item == Items.ELYTRA) Items.ELYTRA
                else Items.DIAMOND_CHESTPLATE
            )
            armorCounts[2] = slots.countItem(Items.DIAMOND_LEGGINGS)
            armorCounts[3] = slots.countItem(Items.DIAMOND_BOOTS)
        }
    }

    override fun renderHud(vertexHelper: VertexHelper) {
        super.renderHud(vertexHelper)
        stringWidth = 0.0f

        runSafe {
            GlStateManager.pushMatrix()

            for ((index, itemStack) in player.armorInventoryList.reversed().withIndex()) {
                if (classic) {
                    drawClassic(vertexHelper, index, itemStack)
                } else {
                    drawModern(vertexHelper, index, itemStack)
                }
            }

            GlStateManager.popMatrix()
        }

        renderStringWidth = stringWidth + 24.0f
    }

    private fun drawClassic(vertexHelper: VertexHelper, index: Int, itemStack: ItemStack) {
        val itemY = if (dockingV != VAlign.TOP) (FontRenderAdapter.getFontHeight() + 4.0f).toInt() else 2

        drawItem(vertexHelper, itemStack, index, 2, itemY)
        GlStateManager.translate(20.0f, 0.0f, 0.0f)
    }

    private fun drawModern(vertexHelper: VertexHelper, index: Int, itemStack: ItemStack) {
        val itemX = if (dockingH != HAlign.RIGHT) 2 else (renderStringWidth - 18.0f).toInt()

        drawItem(vertexHelper, itemStack, index, itemX, 2)
        GlStateManager.translate(0.0f, 20.0f, 0.0f)
    }

    private fun drawItem(vertexHelper: VertexHelper, itemStack: ItemStack, index: Int, x: Int, y: Int) {
        if (itemStack.isEmpty) return

        RenderUtils2D.drawItem(itemStack, x, y, drawOverlay = false)
        drawDura(vertexHelper, itemStack, x, y)

        if (armorCount) {
            val string = armorCounts[index].toString()
            val width = FontRenderAdapter.getStringWidth(string)
            val height = FontRenderAdapter.getFontHeight()

            FontRenderAdapter.drawString(string, x + 16.0f - width, y + 16.0f - height)
        }
    }

    private fun drawDura(vertexHelper: VertexHelper, itemStack: ItemStack, x: Int, y: Int) {
        if (!itemStack.isItemStackDamageable) return

        val dura = itemStack.maxDamage - itemStack.itemDamage
        val duraMultiplier = dura / itemStack.maxDamage.toFloat()
        val duraPercent = MathUtils.round(duraMultiplier * 100.0f, 1).toFloat()
        val color = duraColorGradient.get(duraPercent)

        if (durabilityBar) {
            val duraBarWidth = (16.0 * duraMultiplier).coerceAtLeast(0.0)
            RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(x.toDouble(), y + 16.0), Vec2d(x + 16.0, y + 18.0), ColorHolder(0, 0, 0))
            RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(x.toDouble(), y + 16.0), Vec2d(x + duraBarWidth, y + 18.0), color)
        }

        if (durabilityPercentage) {
            if (classic) {
                val string = duraPercent.toInt().toString()
                val width = FontRenderAdapter.getStringWidth(string)

                val duraX = 10 - width * 0.5f
                val duraY = if (dockingV != VAlign.TOP) 2.0f else 22.0f

                FontRenderAdapter.drawString(string, duraX, duraY, color = color)
            } else {
                val string = "$dura/${itemStack.maxDamage}  ($duraPercent%)"
                val width = FontRenderAdapter.getStringWidth(string)
                stringWidth = max(width, stringWidth)

                val duraX = if (dockingH != HAlign.RIGHT) 22.0f else renderStringWidth - 22.0f - width
                val duraY = 10.0f - FontRenderAdapter.getFontHeight() * 0.5f

                FontRenderAdapter.drawString(string, duraX, duraY, color = color)
            }
        }
    }
}