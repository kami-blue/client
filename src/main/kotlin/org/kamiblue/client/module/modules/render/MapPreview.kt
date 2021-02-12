package org.kamiblue.client.module.modules.render

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.item.ItemMap
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.MapData
import org.kamiblue.client.mixin.client.gui.MixinGuiScreen
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.module.modules.client.GuiColors
import org.kamiblue.client.util.graphics.GlStateUtils.useVbo
import org.kamiblue.client.util.graphics.RenderUtils2D.drawRectFilled
import org.kamiblue.client.util.graphics.RenderUtils2D.drawRectOutline
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.graphics.font.FontRenderAdapter.getFontHeight
import org.kamiblue.client.util.graphics.font.FontRenderAdapter.getStringWidth
import org.kamiblue.client.util.math.Vec2d
import java.awt.Color

/**
 * @see MixinGuiScreen.renderToolTip
 */
internal object MapPreview : Module(
    name = "MapPreview",
    category = Category.RENDER,
    description = "Previews maps when hovering over them"
) {
    private val mapBackground = ResourceLocation("textures/map/map_background.png")

    private val showName = setting("Show Name", true)
    private val frame = setting("Show Frame", true)
    val scale = setting("Scale", 5.0, 0.0..10.0, 0.1)

    @JvmStatic
    fun getMapData(itemStack: ItemStack): MapData? {
        return (itemStack.item as? ItemMap)?.getMapData(itemStack, mc.world)
    }

    @JvmStatic
    fun drawMap(stack: ItemStack, mapData: MapData, originalX: Int, originalY: Int) {
        val x = originalX + 6.0
        val y = originalY + 6.0
        val scale = scale.value / 5.0

        GlStateManager.pushMatrix()
        GlStateManager.color(1f, 1f, 1f)
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.disableDepth()

        GlStateManager.translate(x, y, 0.0)
        GlStateManager.scale(scale, scale, 0.0)

        drawMapFrame()
        mc.entityRenderer.mapItemRenderer.renderMap(mapData, false)
        drawMapName(stack)

        GlStateManager.enableDepth()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.popMatrix()
    }

    private fun drawMapFrame() {
        if (!frame.value) return

        val tessellator = Tessellator.getInstance()
        val bufBuilder = tessellator.buffer
        mc.textureManager.bindTexture(mapBackground)

        // Magic numbers taken from Minecraft code
        bufBuilder.begin(7, DefaultVertexFormats.POSITION_TEX)
        bufBuilder.pos(-7.0, 135.0, 0.0).tex(0.0, 1.0).endVertex()
        bufBuilder.pos(135.0, 135.0, 0.0).tex(1.0, 1.0).endVertex()
        bufBuilder.pos(135.0, -7.0, 0.0).tex(1.0, 0.0).endVertex()
        bufBuilder.pos(-7.0, -7.0, 0.0).tex(0.0, 0.0).endVertex()
        tessellator.draw()
    }

    private fun drawMapName(stack: ItemStack) {
        if (!showName.value) return

        val vertexHelper = VertexHelper(useVbo())
        val backgroundX = Vec2d(-2.0, -18.0)
        val backgroundY = Vec2d(
            (getStringWidth(stack.displayName, 1f, false) + 4).toDouble(),
            (getFontHeight(1f, false) - 14).toDouble()
        )

        // Draw the background
        drawRectFilled(vertexHelper, backgroundX, backgroundY, GuiColors.backGround)
        drawRectOutline(vertexHelper, backgroundX, backgroundY, 1.5f, GuiColors.outline)

        // Draw the name
        mc.fontRenderer.drawStringWithShadow(stack.displayName, 2f, -15f, Color.WHITE.rgb)
    }
}