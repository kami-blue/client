package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.GlStateUtils.rescaleKami
import me.zeroeightsix.kami.util.graphics.GlStateUtils.rescaleMc
import me.zeroeightsix.kami.util.graphics.GuiFrameUtil.getFrameByName
import me.zeroeightsix.kami.util.graphics.RenderUtils2D
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import kotlin.math.roundToInt

@Module.Info(
        name = "InventoryViewer",
        category = Module.Category.CLIENT,
        description = "Configures Inventory Viewer's options",
        showOnArray = Module.ShowOnArray.OFF,
        alwaysEnabled = true
)
object InventoryViewer : Module() {
    private val mcTexture = register(Settings.b("UseMinecraftTexture", false))
    private val showIcon = register(Settings.booleanBuilder("ShowIcon").withValue(false).withVisibility { !mcTexture.value })
    private val iconScale = register(Settings.floatBuilder("IconScale").withValue(0.5f).withRange(0.1f, 1.0f).withStep(0.1f).withVisibility { !mcTexture.value && showIcon.value })
    private val coloredBackground = register(Settings.booleanBuilder("ColoredBackground").withValue(true).withVisibility { !mcTexture.value })
    private val r = register(Settings.integerBuilder("Red").withValue(155).withRange(0, 255).withStep(1).withVisibility { coloredBackground.value && !mcTexture.value })
    private val g = register(Settings.integerBuilder("Green").withValue(144).withRange(0, 255).withStep(1).withVisibility { coloredBackground.value && !mcTexture.value })
    private val b = register(Settings.integerBuilder("Blue").withValue(255).withRange(0, 255).withStep(1).withVisibility { coloredBackground.value && !mcTexture.value })
    private val a = register(Settings.integerBuilder("Alpha").withValue(32).withRange(0, 255).withStep(1).withVisibility { coloredBackground.value && !mcTexture.value })

    private val containerTexture = ResourceLocation("textures/gui/container/inventory.png")
    private val kamiIcon = ResourceLocation("kamiblue/kami_icon.png")

    fun renderInventoryViewer() {
        if (mc.player == null || mc.world == null) return
        drawFrame()
        drawFrameTexture()
        drawItems()
    }

    private fun drawFrame() {
        val vertexHelper = VertexHelper(GlStateUtils.useVbo())

        if (!mcTexture.value && coloredBackground.value) {
            RenderUtils2D.drawRectFilled(vertexHelper, posEnd = Vec2d(162.0, 54.0), color = ColorHolder(r.value, g.value, b.value, a.value))
        }
    }

    private fun drawFrameTexture() {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        GlStateUtils.texture2d(true)

        if (mcTexture.value) {
            mc.renderEngine.bindTexture(containerTexture)
            buffer.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX)
            buffer.pos(0.0, 0.0, 0.0).tex(0.02734375, 0.32421875).endVertex() // (1.75 / 64), (20.75 / 64)
            buffer.pos(0.0, 54.0, 0.0).tex(0.02734375, 0.53125).endVertex() // (1.75 / 64), (34 / 64)
            buffer.pos(162.0, 0.0, 0.0).tex(0.65625, 0.32421875).endVertex() // (42 / 64), (20.75 / 64)
            buffer.pos(162.0, 54.0, 0.0).tex(0.65625, 0.53125).endVertex() // (42 / 64), (34 / 64)
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
            val slotX = index % 9 * 18 + 1
            val slotY = index / 9 * 18 + 1

            GlStateUtils.blend(true)
            GlStateUtils.depth(true)
            RenderHelper.enableGUIStandardItemLighting()
            mc.renderItem.renderItemAndEffectIntoGUI(itemStack, slotX, slotY)
            mc.renderItem.renderItemOverlays(mc.fontRenderer, itemStack, slotX, slotY)
            RenderHelper.disableStandardItemLighting()
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            GlStateUtils.lighting(false)
        }
    }

}