package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorConverter.rgbToInt
import me.zeroeightsix.kami.util.graphics.GlStateUtils.rescaleKami
import me.zeroeightsix.kami.util.graphics.GlStateUtils.rescaleMc
import me.zeroeightsix.kami.util.graphics.GuiFrameUtil.getFrameByName
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

/**
 * Updated by dominikaaaa on 21/02/20
 * Slight updates by 20kdc, 19/02/20
 * Everything except somethingRender() methods was written by dominikaaaa
 */
@Module.Info(
        name = "InventoryViewer",
        category = Module.Category.CLIENT,
        description = "Configures Inventory Viewer's options",
        showOnArray = Module.ShowOnArray.OFF,
        alwaysEnabled = true
)
class InventoryViewer : Module() {
    private val mcTexture = register(Settings.b("UseResourcePack", false))
    private val showIcon = register(Settings.booleanBuilder("ShowIcon").withValue(false).withVisibility { !mcTexture.value }.build())
    private val viewSizeSetting = register(Settings.enumBuilder(ViewSize::class.java).withName("IconSize").withValue(ViewSize.LARGE).withVisibility { showIcon.value && !mcTexture.value }.build())
    private val coloredBackground = register(Settings.booleanBuilder("ColoredBackground").withValue(true).withVisibility { !mcTexture.value }.build())
    private val a = register(Settings.integerBuilder("Transparency").withMinimum(0).withValue(32).withMaximum(255).withVisibility { coloredBackground.value && !mcTexture.value }.build())
    private val r = register(Settings.integerBuilder("Red").withMinimum(0).withValue(155).withMaximum(255).withVisibility { coloredBackground.value && !mcTexture.value }.build())
    private val g = register(Settings.integerBuilder("Green").withMinimum(0).withValue(144).withMaximum(255).withVisibility { coloredBackground.value && !mcTexture.value }.build())
    private val b = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).withVisibility { coloredBackground.value && !mcTexture.value }.build())

    private enum class ViewSize {
        LARGE, MEDIUM, SMALL
    }

    private val box: ResourceLocation
        get() = if (mcTexture.value) {
            ResourceLocation("textures/gui/container/generic_54.png")
        } else if (!showIcon.value) {
            ResourceLocation("kamiblue/clear.png")
        } else if (viewSizeSetting.value == ViewSize.LARGE) {
            ResourceLocation("kamiblue/large.png")
        } else if (viewSizeSetting.value == ViewSize.SMALL) {
            ResourceLocation("kamiblue/small.png")
        } else if (viewSizeSetting.value == ViewSize.MEDIUM) {
            ResourceLocation("kamiblue/medium.png")
        } else {
            ResourceLocation("null")
        }

    private fun boxRender(x: Int, y: Int) {
        // SET UNRELIABLE DEFAULTS (Don't restore these) {
//        GlStateManager.enableAlpha(); // when PlayerModel is disabled, this causes InventoryViewer to turn the chat and everything gray
//        GlStateManager.disableBlend(); // when PlayerModel is disabled, this causes InventoryViewer to turn the chat and everything gray
        // }

        // ENABLE LOCAL CHANGES {
        GlStateManager.disableDepth()
        // }
        if (coloredBackground.value) { // 1 == 2 px in game
            Gui.drawRect(x, y, x + 162, y + 54, rgbToInt(r.value, g.value, b.value, a.value))
        }
        mc.renderEngine.bindTexture(box)
        GlStateManager.color(1f, 1f, 1f, 1f)
        mc.ingameGUI.drawTexturedModalRect(x, y, 7, 17, 162, 54) // 164 56 // width and height of inventory
        // DISABLE LOCAL CHANGES {
        GlStateManager.enableDepth()
        // }
    }

    override fun onRender() {
        val frame = getFrameByName("inventory viewer") ?: return
        if (frame.isPinned && !frame.isMinimized) {
            rescaleKami()
            val items = mc.player.inventory.mainInventory
            boxRender(frame.x, frame.y)
            itemRender(items, frame.x, frame.y)
            rescaleMc()
        }
    }

    private fun itemRender(items: NonNullList<ItemStack>, x: Int, y: Int) {
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT)
        val size = items.size
        var item = 9
        while (item < size) {
            val slotX = x + 1 + item % 9 * 18
            val slotY = y + 1 + (item / 9 - 1) * 18
            preItemRender()
            mc.getRenderItem().renderItemAndEffectIntoGUI(items[item], slotX, slotY)
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, items[item], slotX, slotY)
            postItemRender()
            ++item
        }
    }

    // These methods should apply and clean up in pairs.
    // That means that if a pre* has to disableAlpha, the post* function should enableAlpha.
    //  - 20kdc
    private fun preItemRender() {
        GlStateManager.pushMatrix()
        GlStateManager.enableDepth()
        //        GlStateManager.depthMask(true); // when PlayerModel is disabled, this causes InventoryViewer to turn the chat and everything gray
        // Yes, this is meant to be paired with disableStandardItemLighting - 20kdc
        RenderHelper.enableGUIStandardItemLighting()
    }

    private fun postItemRender() {
        RenderHelper.disableStandardItemLighting()
        //        GlStateManager.depthMask(false); // when PlayerModel is disabled, this causes InventoryViewer to turn the chat and everything gray
        GlStateManager.disableDepth()
        GlStateManager.popMatrix()
    }
}