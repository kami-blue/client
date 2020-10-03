package me.zeroeightsix.kami.util.graphics.font

import me.zeroeightsix.kami.module.modules.ClickGUI
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.color.ColorHolder
import org.lwjgl.opengl.GL11.*
import kotlin.math.round

/**
 * For the sake of dumb Minecraftia simps
 */
object FontRenderAdapter {
    private val dumbMcFontRenderer = Wrapper.minecraft.fontRenderer
    private val useCustomFont get() = ClickGUI.customFont.value

    @JvmOverloads
    fun drawString(text: String, posXIn: Float = 0f, posYIn: Float = 0f, drawShadow: Boolean = true, color: ColorHolder = ColorHolder(255, 255, 255), scale: Float = 1f) {
        if (useCustomFont) {
            KamiFontRenderer.drawString(text, posXIn, posYIn, drawShadow, color, scale)
        } else {
            glPushMatrix()
            glTranslatef(round(posXIn), round(posYIn), 0f)
            glScalef(scale, scale, 1f)
            dumbMcFontRenderer.drawString(text, 0f, 0f, color.toHex(), drawShadow)
            glPopMatrix()
        }
    }

    @JvmOverloads
    fun getFontHeight(scale: Float = 1f) = if (useCustomFont) {
        KamiFontRenderer.getFontHeight(scale)
    } else {
        dumbMcFontRenderer.FONT_HEIGHT * scale
    }

    @JvmOverloads
    fun getStringWidth(text: String, scale: Float = 1f) = if (useCustomFont) {
        KamiFontRenderer.getStringWidth(text, scale)
    } else {
        dumbMcFontRenderer.getStringWidth(text) * scale
    }
}