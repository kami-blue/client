package org.kamiblue.client.gui.hudgui.elements.combat

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.gui.hudgui.HudElement
import org.kamiblue.client.setting.GuiConfig.setting
import org.kamiblue.client.util.color.ColorConverter
import org.kamiblue.client.util.color.ColorGradient
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.GlStateUtils
import org.kamiblue.client.util.graphics.RenderUtils2D
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.graphics.font.FontRenderAdapter
import org.kamiblue.client.util.graphics.font.HAlign
import org.kamiblue.client.util.graphics.font.VAlign
import org.kamiblue.client.util.items.allSlots
import org.kamiblue.client.util.items.countItem
import org.kamiblue.client.util.math.Vec2d
import org.kamiblue.client.util.threads.safeAsyncListener
import org.kamiblue.commons.utils.MathUtils
import kotlin.math.max
import kotlin.math.roundToInt

object Armor : HudElement(
    name = "Armor",
    category = Category.COMBAT,
    description = "Show the durability of armor and the count of them"
) {

    private val classic = setting("Classic", false)
    private val armorCount = setting("ArmorCount", true)
    private val countElytras = setting("CountElytras", false, { armorCount.value })
    private val durabilityMode by setting("Durability Mode", DurabilityMode.PERCENTAGE)

    private val percentageMode get() = durabilityMode == DurabilityMode.PERCENTAGE || durabilityMode == DurabilityMode.BOTH
    private val colorBarMode get() = durabilityMode == DurabilityMode.COLOUR_BAR || durabilityMode == DurabilityMode.BOTH

    private enum class DurabilityMode {
        COLOUR_BAR, PERCENTAGE, BOTH
    }

    override val hudWidth: Float
        get() = if (classic.value) {
            80.0f
        } else {
            stringWidth
        }

    override val hudHeight: Float
        get() = if (classic.value) {
            40.0f
        } else {
            80.0f
        }

    private var stringWidth = 120.0f

    private val armorCounts = IntArray(4)
    private val durabilityColorGradient = ColorGradient(
        0f to ColorHolder(180, 20, 20),
        50f to ColorHolder(240, 220, 20),
        100f to ColorHolder(20, 232, 20)
    )

    init {
        safeAsyncListener<TickEvent.ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END) return@safeAsyncListener

            val slots = player.allSlots

            armorCounts[0] = slots.countItem(Items.DIAMOND_HELMET)
            armorCounts[1] = slots.countItem(
                if (countElytras.value && player.inventory.getStackInSlot(38).item == Items.ELYTRA) Items.ELYTRA
                else Items.DIAMOND_CHESTPLATE
            )
            armorCounts[2] = slots.countItem(Items.DIAMOND_LEGGINGS)
            armorCounts[3] = slots.countItem(Items.DIAMOND_BOOTS)
        }
    }

    override fun renderHud(vertexHelper: VertexHelper) {
        super.renderHud(vertexHelper)
        val player = mc.player ?: return

        GlStateManager.pushMatrix()

        if (classic.value) {
            val itemY = if (dockingV != VAlign.TOP) (FontRenderAdapter.getFontHeight() + 4.0f).toInt() else 2
            val durabilityY = if (dockingV != VAlign.TOP) 2.0f else 22.0f

            for ((index, armor) in player.armorInventoryList.reversed().withIndex()) {
                drawItem(armor, index, 2, itemY)

                if (armor.isItemStackDamageable) {
                    val durabilityPercentage = MathUtils.round((armor.maxDamage - armor.itemDamage) / armor.maxDamage.toFloat() * 100.0f, 1).toFloat()
                    val string = durabilityPercentage.toInt().toString()
                    val width = FontRenderAdapter.getStringWidth(string)
                    val color = durabilityColorGradient.get(durabilityPercentage)

                    FontRenderAdapter.drawString(string, 10 - width * 0.5f, durabilityY, color = color)
                }

                GlStateManager.translate(20.0f, 0.0f, 0.0f)
            }
        } else {
            val itemX = if (dockingH != HAlign.RIGHT) 2 else (stringWidth - 18).toInt()
            val durabilityY = 10.0f - FontRenderAdapter.getFontHeight() * 0.5f
            var maxWidth = 0.0f

            for ((index, armor) in player.armorInventoryList.reversed().withIndex()) {
                drawItem(armor, index, itemX, 2)

                if (armor.isItemStackDamageable && percentageMode) {
                    val durability = armor.maxDamage - armor.itemDamage
                    val durabilityPercentage = MathUtils.round(durability / armor.maxDamage.toFloat() * 100.0f, 1).toFloat()

                    val string = "$durability/${armor.maxDamage}  ($durabilityPercentage%)"
                    val durabilityWidth = FontRenderAdapter.getStringWidth(string)
                    val durabilityX = if (dockingH != HAlign.RIGHT) 22.0f else stringWidth - 22.0f - durabilityWidth
                    val color = durabilityColorGradient.get(durabilityPercentage)
                    maxWidth = max(durabilityWidth, maxWidth)

                    FontRenderAdapter.drawString(string, durabilityX, durabilityY, color = color)
                }

                GlStateManager.translate(0.0f, 20.0f, 0.0f)
            }

            stringWidth = maxWidth + 24.0f
        }

        GlStateManager.popMatrix()
    }

    private fun drawItem(itemStack: ItemStack, index: Int, x: Int, y: Int) {
        if (itemStack.isEmpty) return

        RenderUtils2D.drawItem(itemStack, x, y, drawOverlay = false)

        if (itemStack.isItemDamaged && colorBarMode) {
            val health: Double = itemStack.item.getDurabilityForDisplay(itemStack)
            val hexRgb: Int = itemStack.item.getRGBDurabilityForDisplay(itemStack)
            val i = (13.0f - health.toFloat() * 13.0f).roundToInt()

            RenderUtils2D.drawRectFilled(VertexHelper(GlStateUtils.useVbo()), Vec2d(x + 2, y + 13), Vec2d(x + 15, y + 15), ColorHolder(0, 0, 0, 255))
            RenderUtils2D.drawRectFilled(VertexHelper(GlStateUtils.useVbo()), Vec2d(x + 2, y + 13), Vec2d(x + 2 + i, y + 14), ColorConverter.hexToRgb(hexRgb))
        }

        if (armorCount.value) {
            val string = armorCounts[index].toString()
            val width = FontRenderAdapter.getStringWidth(string)
            val height = FontRenderAdapter.getFontHeight()

            FontRenderAdapter.drawString(string, x + 16.0f - width, y + 16.0f - height)
        }
    }
}
