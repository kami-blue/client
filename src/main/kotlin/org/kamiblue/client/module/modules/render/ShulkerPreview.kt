package org.kamiblue.client.module.modules.render

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
import org.kamiblue.client.mixin.client.gui.MixinGuiScreen
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.color.ColorConverter
import org.kamiblue.client.util.color.ColorGradient
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.GlStateUtils
import org.kamiblue.client.util.graphics.RenderUtils2D
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.math.Vec2d
import org.kamiblue.commons.extension.floorToInt

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
        val vertexHelper = VertexHelper(GlStateUtils.useVbo())

        val x = (originalX + 12).toDouble()
        val y = (originalY - 12).toDouble()
        val height = 48 + 9 // 3 * 16

        itemRenderer.zLevel = 300.0f
        // Magic numbers taken from Minecraft code

        RenderUtils2D.drawRoundedRectFilled(vertexHelper,
            Vec2d(x - 3, y - 4),
            Vec2d((x + width + 3), (y + height + 4)),
            1.0,
            segments = 1,
            color = ColorHolder(16, 0,16, 240))

        RenderUtils2D.drawGradientFilledRect(vertexHelper,
            Vec2d(x - 2, y - 3),
            Vec2d(x + width + 2, y + height + 3),
            ColorGradient(0.0f to ColorHolder(80, 0, 255, 80), 1.0f to ColorHolder(40, 0, 127, 80))
        )

        RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(x - 1, y - 2), Vec2d(x + width + 1, y + height + 2), ColorHolder(16, 0,16, 240))

        fontRenderer.drawString(stack.displayName, x.floorToInt(), y.floorToInt(), 0xffffff)
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
}
