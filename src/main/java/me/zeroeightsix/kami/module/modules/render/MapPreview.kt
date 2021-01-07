package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.mixin.client.gui.MixinGuiScreen
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.client.GuiColors
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.graphics.GlStateUtils.useVbo
import me.zeroeightsix.kami.util.graphics.RenderUtils2D.drawRectFilled
import me.zeroeightsix.kami.util.graphics.RenderUtils2D.drawRectOutline
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter.getFontHeight
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter.getStringWidth
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.item.ItemMap
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.MapData
import java.awt.Color

/**
 * @see MixinGuiScreen.renderToolTip
 */
object MapPreview : Module(
    name = "MapPreview",
    category = Category.RENDER,
    description = "Previews maps when hovering over them"
) {
    private val mapBackground = ResourceLocation("textures/map/map_background.png")

    private val showName = setting("ShowName", false)
    private val frame = setting("ShowFrame", false)
    val scale = setting("Scale", 5.0, 0.0..10.0, 0.1)

    @JvmStatic
    fun getMapData(itemStack: ItemStack): MapData? {
        return (itemStack.item as? ItemMap)?.getMapData(itemStack, mc.world)
    }

    @JvmStatic
    fun drawMap(stack: ItemStack, mapData: MapData) {
        drawMapFrame()
        mc.entityRenderer.mapItemRenderer.renderMap(mapData, false)
        drawMapName(stack)
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