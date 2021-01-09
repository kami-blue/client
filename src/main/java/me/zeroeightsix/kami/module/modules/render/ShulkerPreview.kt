package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.mixin.client.gui.MixinGuiScreen
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.inventory.ItemStackHelper
import net.minecraft.item.ItemShulkerBox
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.NonNullList

/**
 * @see MixinGuiScreen.renderToolTip
 */
internal object ShulkerPreview : Module(
    name = "ShulkerPreview",
    category = Category.RENDER,
    description = "Previews shulkers in the game GUI"
) {

    private val itemRenderer = Minecraft.getMinecraft().renderItem
    private val fontRenderer = Minecraft.getMinecraft().fontRenderer

    @JvmStatic
    fun renderShulkerAndItems(stack: ItemStack, originalX: Int, originalY: Int, tagCompound: NBTTagCompound) {

        val shulkerInventory = NonNullList.withSize(27, ItemStack.EMPTY)
        ItemStackHelper.loadAllItems(tagCompound, shulkerInventory)

        GlStateManager.enableBlend()
        GlStateManager.disableRescaleNormal()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()

        renderShulker(stack, originalX, originalY)

        GlStateManager.enableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
        RenderHelper.enableGUIStandardItemLighting()

        renderShulkerItems(shulkerInventory, originalX, originalY)

        RenderHelper.disableStandardItemLighting()
        itemRenderer.zLevel = 0.0f

        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
        RenderHelper.enableStandardItemLighting()
        GlStateManager.enableRescaleNormal()
    }

    @JvmStatic
    fun getShulkerData(stack: ItemStack): NBTTagCompound? {
        val tagCompound = if (stack.item is ItemShulkerBox) stack.tagCompound else return null

        if (tagCompound != null && tagCompound.hasKey("BlockEntityTag", 10)) {
            val blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag")
            if (blockEntityTag.hasKey("Items", 9)) {
                return blockEntityTag
            }
        }

        return null
    }

    private fun renderShulker(stack: ItemStack, originalX: Int, originalY: Int) {
        val width = 144.coerceAtLeast(fontRenderer.getStringWidth(stack.displayName) + 3) // 9 * 16

        val x = originalX + 12
        val y = originalY - 12
        val height = 48 + 9 // 3 * 16

        itemRenderer.zLevel = 300.0f
        // Magic numbers taken from Minecraft code
        drawGradientRect(x - 3, y - 4, x + width + 3, y - 3, -267386864, -267386864)
        drawGradientRect(x - 3, y + height + 3, x + width + 3, y + height + 4, -267386864, -267386864)
        drawGradientRect(x - 3, y - 3, x + width + 3, y + height + 3, -267386864, -267386864)
        drawGradientRect(x - 4, y - 3, x - 3, y + height + 3, -267386864, -267386864)
        drawGradientRect(x + width + 3, y - 3, x + width + 4, y + height + 3, -267386864, -267386864)
        drawGradientRect(x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, 1347420415, 1344798847)
        drawGradientRect(x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, 1347420415, 1344798847)
        drawGradientRect(x - 3, y - 3, x + width + 3, y - 3 + 1, 1347420415, 1347420415)
        drawGradientRect(x - 3, y + height + 2, x + width + 3, y + height + 3, 1344798847, 1344798847)

        fontRenderer.drawString(stack.displayName, x, y, 0xffffff)
    }

    private fun renderShulkerItems(shulkerInventory: NonNullList<ItemStack>, originalX: Int, originalY: Int) {
        for (i in 0 until shulkerInventory.size) {
            val x = originalX + i % 9 * 16 + 11
            val y = originalY + i / 9 * 16 - 11 + 8
            val itemStack: ItemStack = shulkerInventory[i]
            itemRenderer.renderItemAndEffectIntoGUI(itemStack, x, y)
            itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, itemStack, x, y, null)
        }
    }

    private fun drawGradientRect(left: Int, top: Int, right: Int, bottom: Int, startColor: Int, endColor: Int) {
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        )
        GlStateManager.shadeModel(7425)

        val tessellator = Tessellator.getInstance()
        val bufBuilder = tessellator.buffer

        bufBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
        bufBuilder.colorVertex(right, top, startColor)
        bufBuilder.colorVertex(left, top, startColor)
        bufBuilder.colorVertex(left, bottom, endColor)
        bufBuilder.colorVertex(right, bottom, endColor)
        tessellator.draw()

        GlStateManager.shadeModel(7424)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    private fun BufferBuilder.colorVertex(x: Int, y: Int, color: Int) {
        this.pos(x.toDouble(), y.toDouble(), 300.0)
            .color(
                (color shr 16 and 255) / 255f,
                (color shr 8 and 255) / 255f,
                (color and 255) / 255f,
                (color shr 24 and 255) / 255f
            )
            .endVertex()
    }

}
