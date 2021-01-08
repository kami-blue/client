package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.manager.managers.KamiMojiManager.getEmoji
import me.zeroeightsix.kami.manager.managers.KamiMojiManager.isEmoji
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.graphics.GlStateUtils.resetTexParam
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import org.kamiblue.commons.extension.ceilToInt
import org.lwjgl.opengl.GL11
import java.util.*

object KamiMoji : Module(
    name = "KamiMoji",
    description = "Add emojis to chat using KamiMoji, courtesy of the EmojiAPI.",
    category = Category.CHAT
) {
    @JvmStatic
    fun getText(inputText: String, fontHeight: Int, shadow: Boolean, posX: Float, posY: Float, alpha: Float): String {
        var text = inputText

        for (possible in text.split(":").toTypedArray()) {
            if (isEmoji(possible)) {
                val emojiText = ":$possible:"
                if (!shadow) {
                    val index = text.indexOf(emojiText)
                    if (index == -1) continue

                    val x = mc.fontRenderer.getStringWidth(text.substring(0, index)) + fontHeight / 4
                    drawEmoji(getEmoji(possible), (posX + x).toDouble(), posY.toDouble(), fontHeight.toFloat(), alpha)
                }

                text = text.replaceFirst(emojiText.toRegex(), getReplacement(fontHeight))
            }
        }

        return text
    }

    @JvmStatic
    fun getStringWidth(inputWidth: Int, inputText: String, fontHeight: Int): Int {
        var text = inputText
        var reducedWidth = inputWidth

        for (possible in text.split(":")) {
            if (isEmoji(possible)) {
                val emojiText = ":$possible:"
                val emojiTextWidth = emojiText.sumBy { mc.fontRenderer.getCharWidth(it) }
                reducedWidth -= emojiTextWidth
                text = text.replaceFirst(emojiText, getReplacement(fontHeight))
            }
        }

        return reducedWidth
    }

    private fun getReplacement(fontHeight: Int): String {
        val emojiWidth = (fontHeight / mc.fontRenderer.getCharWidth(' ').toDouble()).ceilToInt()
        val spaces = CharArray(emojiWidth) { ' ' }
        return String(spaces)
    }

    /* This is created because vanilla one doesn't take double position input */
    private fun drawEmoji(emojiTexture: ResourceLocation?, x: Double, y: Double, size: Float, alpha: Float) {
        if (emojiTexture == null) return
        val tessellator = Tessellator.getInstance()
        val bufBuilder = tessellator.buffer

        mc.textureManager.bindTexture(emojiTexture)
        GlStateManager.color(1f, 1f, 1f, alpha)
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)

        bufBuilder.begin(7, DefaultVertexFormats.POSITION_TEX)
        bufBuilder.pos(x, y + size, 0.0).tex(0.0, 1.0).endVertex()
        bufBuilder.pos(x + size, y + size, 0.0).tex(1.0, 1.0).endVertex()
        bufBuilder.pos(x + size, y, 0.0).tex(1.0, 0.0).endVertex()
        bufBuilder.pos(x, y, 0.0).tex(0.0, 0.0).endVertex()
        tessellator.draw()

        resetTexParam()
    }
}
