package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.manager.managers.KamiMojiManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.graphics.GlStateUtils.resetTexParam
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import org.kamiblue.commons.extension.ceilToInt
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS

object KamiMoji : Module(
    name = "KamiMoji",
    description = "Add emojis to chat using KamiMoji, courtesy of the EmojiAPI.",
    category = Category.CHAT
) {
    @JvmStatic
    fun renderText(inputText: String, fontHeight: Int, shadow: Boolean, posX: Float, posY: Float, alpha: Float): String {
        var text = inputText

        for (possible in text.split(":").toTypedArray()) {
            if (KamiMojiManager.isEmoji(possible)) {
                val emojiText = ":$possible:"
                if (!shadow) {
                    val index = text.indexOf(emojiText)
                    if (index == -1) continue

                    val x = mc.fontRenderer.getStringWidth(text.substring(0, index)) + fontHeight / 4
                    drawEmoji(KamiMojiManager.getEmoji(possible), (posX + x).toDouble(), posY.toDouble(), fontHeight.toFloat(), alpha)
                }

                text = text.replaceFirst(emojiText, getReplacement(fontHeight))
            }
        }

        return text
    }

    @JvmStatic
    fun getStringWidth(inputWidth: Int, inputText: String, fontHeight: Int): Int {
        var text = inputText
        var reducedWidth = inputWidth

        for (possible in text.split(":")) {
            if (KamiMojiManager.isEmoji(possible)) {
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

        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha)
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, 0.0f)

        bufBuilder.begin(7, DefaultVertexFormats.POSITION_TEX)
        bufBuilder.pos(x, y + size, 0.0).tex(0.0, 1.0).endVertex()
        bufBuilder.pos(x + size, y + size, 0.0).tex(1.0, 1.0).endVertex()
        bufBuilder.pos(x + size, y, 0.0).tex(1.0, 0.0).endVertex()
        bufBuilder.pos(x, y, 0.0).tex(0.0, 0.0).endVertex()
        tessellator.draw()

        resetTexParam()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }
}
