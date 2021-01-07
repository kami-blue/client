package me.zeroeightsix.kami.gui.hudgui.elements.player

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.RenderUtils2D
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*

object InventoryViewer : HudElement(
    name = "InventoryViewer",
    category = Category.PLAYER,
    description = "Items in Inventory"
) {
    private val mcTexture = setting("MinecraftTexture", false)
    private val showIcon = setting("ShowIcon", false, { !mcTexture.value })
    private val iconScale = setting("IconScale", 0.5f, 0.1f..1.0f, 0.1f, { !mcTexture.value && showIcon.value })
    private val coloredBackground = setting("ColoredBackground", true, { !mcTexture.value })
    private val color = setting("Background", ColorHolder(155, 144, 255, 64), true, { coloredBackground.value && !mcTexture.value })

    private val containerTexture = ResourceLocation("textures/gui/container/inventory.png")
    private val kamiIcon = ResourceLocation("kamiblue/kami_icon.png")

    override val hudWidth: Float = 162.0f
    override val hudHeight: Float = 54.0f

    override fun renderHud(vertexHelper: VertexHelper) {
        super.renderHud(vertexHelper)
        drawFrame()
        drawFrameTexture()
        drawItems()
    }

    private fun drawFrame() {
        if (!mcTexture.value && coloredBackground.value) {
            val vertexHelper = VertexHelper(GlStateUtils.useVbo())
            RenderUtils2D.drawRectFilled(vertexHelper, posEnd = Vec2d(162.0, 54.0), color = color.value)
        }
    }

    private fun drawFrameTexture() {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        GlStateUtils.texture2d(true)

        if (mcTexture.value) {
            mc.renderEngine.bindTexture(containerTexture)
            buffer.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX)
            buffer.pos(0.0, 0.0, 0.0).tex(0.02734375, 0.32421875).endVertex() // (7 / 256), (83 / 256)
            buffer.pos(0.0, 54.0, 0.0).tex(0.02734375, 0.53125).endVertex() // (7 / 256), (136 / 256)
            buffer.pos(162.0, 0.0, 0.0).tex(0.65625, 0.32421875).endVertex() // (168 / 256), (83 / 256)
            buffer.pos(162.0, 54.0, 0.0).tex(0.65625, 0.53125).endVertex() // (168 / 256), (136 / 256)
            tessellator.draw()
        } else if (showIcon.value) {
            mc.renderEngine.bindTexture(kamiIcon)
            GlStateManager.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)

            val center = Vec2d(81.0, 27.0)
            val halfWidth = iconScale.value * 54.0
            val halfHeight = iconScale.value * 27.0

            buffer.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX)
            buffer.pos(center.x - halfWidth, center.y - halfHeight, 0.0).tex(0.0, 0.0).endVertex()
            buffer.pos(center.x - halfWidth, center.y + halfHeight, 0.0).tex(0.0, 1.0).endVertex()
            buffer.pos(center.x + halfWidth, center.y - halfHeight, 0.0).tex(1.0, 0.0).endVertex()
            buffer.pos(center.x + halfWidth, center.y + halfHeight, 0.0).tex(1.0, 1.0).endVertex()
            tessellator.draw()

            GlStateManager.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        }
    }

    private fun drawItems() {
        val items = mc.player.inventory.mainInventory.subList(9, 36)

        for ((index, itemStack) in items.withIndex()) {
            if (itemStack.isEmpty) continue
            val slotX = index % 9 * 18 + 1
            val slotY = index / 9 * 18 + 1
            RenderUtils2D.drawItem(itemStack, slotX, slotY)
        }
    }

}