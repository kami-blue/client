package org.kamiblue.client.util.graphics.font

import org.kamiblue.client.module.modules.client.CustomFont
import org.kamiblue.client.util.Wrapper
import org.kamiblue.client.util.color.ColorHolder
import org.lwjgl.opengl.GL11.*
import kotlin.math.round

/**
 * For the sake of dumb Minecraftia simps
 */
object FontRenderAdapter {
    private val dumbMcFontRenderer = Wrapper.minecraft.fontRenderer
    val useCustomFont get() = CustomFont.isEnabled

    fun drawString(text: String, posXIn: Float = 0f, posYIn: Float = 0f, drawShadow: Boolean = true, color: ColorHolder = ColorHolder(255, 255, 255), scale: Float = 1f, customFont: Boolean = useCustomFont) {
        if (customFont) {
            KamiFontRenderer.drawString(text, posXIn, posYIn, drawShadow, color, scale)
        } else {
            glPushMatrix()
            glTranslatef(round(posXIn), round(posYIn), 0f)
            glScalef(scale, scale, 1f)
            dumbMcFontRenderer.drawString(text, 0f, 2.0f, color.toHex(), drawShadow)
            glPopMatrix()
        }
    }

    fun getFontHeight(scale: Float = 1f, customFont: Boolean = useCustomFont) = if (customFont) {
        KamiFontRenderer.getFontHeight(scale)
    } else {
        dumbMcFontRenderer.FONT_HEIGHT * scale
    }

    fun getStringWidth(text: String, scale: Float = 1f, customFont: Boolean = useCustomFont) = if (customFont) {
        KamiFontRenderer.getStringWidth(text, scale)
    } else {
        dumbMcFontRenderer.getStringWidth(text) * scale
    }
}