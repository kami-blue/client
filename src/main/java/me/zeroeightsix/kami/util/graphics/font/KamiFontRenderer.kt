package me.zeroeightsix.kami.util.graphics.font

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.modules.ClickGUI
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.font.FontGlyphs.Companion.TEXTURE_WIDTH
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS
import java.awt.Font
import java.awt.GraphicsEnvironment

/**
 * Adapted from Bobjob's edited version of Slick's TrueTypeFont.
 * http://forum.lwjgl.org/index.php?topic=2951
 *
 * License: http://slick.ninjacave.com/license/
 *
 * A TrueType font implementation originally for Slick, edited for Bobjob's Engine
 *
 * @original author James Chambers (Jimmy)
 * @original author Jeremy Adams (elias4444)
 * @original author Kevin Glass (kevglass)
 * @original author Peter Korzuszek (genail)
 *
 * @new version edited by David Aaron Muhar (bobjob)
 */
object KamiFontRenderer {
    private val tessellator = Tessellator.getInstance()
    private val buffer = tessellator.buffer

    /**
     * Stores different variants (Regular, Bold, Italic) of glyphs
     * 0: Regular, 1: Bold, 2: Italic
     */
    val glyphArray: Array<FontGlyphs>

    /** CurrentVariant being used */
    private var currentVariant: FontGlyphs

    /** Available fonts on in the system */
    private val availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts.map { it.name }.toHashSet()

    /** All for the KAMI Blue kanji */
    private val fallbackFonts = arrayOf(
            "Noto Sans JP", "Noto Sans CJK JP", "Noto Sans CJK JP", "Noto Sans CJK KR", "Noto Sans CJK SC", "Noto Sans CJK TC", // Noto Sans
            "Source Han Sans", "Source Han Sans HC", "Source Han Sans SC", "Source Han Sans TC", "Source Han Sans K", // Source Sans
            "MS Gothic", "Meiryo", "Yu Gothic", // For Windows, Windows on top!
            "Hiragino Sans GB W3", "Hiragino Kaku Gothic Pro W3", "Hiragino Kaku Gothic ProN W3", "Osaka", // For stupid Mac OSX
            "IPAPGothic" // For cringy Linux
    )

    init {
        glyphArray = Array(3) {
            val style = TextProperties.Style.values()[it]

            // Load main font
            val font = try {
                val inputStream = KamiFontRenderer::class.java.getResourceAsStream(style.fontPath)
                Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(32f)
            } catch (e: Exception) {
                e.printStackTrace()
                KamiMod.log.error("Failed loading main font. Using Sans Serif font.")
                getSansSerifFont(style.styleConst)
            }

            // Load fallback font
            val fallbackFont = try {
                Font(getFallbackFont(), style.styleConst, 32)
            } catch (e: Exception) {
                e.printStackTrace()
                KamiMod.log.error("Failed loading fallback font. Using Sans Serif font")
                getSansSerifFont(style.styleConst)
            }

            println(fallbackFont.name)
            FontGlyphs(style, font, fallbackFont)
        }
        currentVariant = glyphArray[0]
    }

    private fun getFallbackFont() = fallbackFonts.firstOrNull { availableFonts.contains(it) }

    private fun getSansSerifFont(style: Int) = Font("SansSerif", style, 32)

    @JvmOverloads
    fun drawString(text: String, posXIn: Float = 0f, posYIn: Float = 0f, drawShadow: Boolean = true, color: ColorHolder = ColorHolder(255, 255, 255), scale: Float = 1f) {
        if (drawShadow) {
            val darkness = ClickGUI.darkNess.value
            val shadowColor = ColorHolder((color.r * darkness).toInt(), (color.g * darkness).toInt(), (color.b * darkness).toInt(), (color.a * ClickGUI.alpha.value).toInt())
            drawString(text, posXIn + ClickGUI.shadow.value, posYIn + ClickGUI.shadow.value, shadowColor, scale)
        }
        drawString(text, posXIn, posYIn, color, scale)
    }

    private fun drawString(text: String, posXIn: Float, posYIn: Float, color: ColorHolder, scale: Float) {
        var posX = 0.0
        var posY = 0.0

        glDisable(GL_ALPHA_TEST)
        GlStateUtils.blend(true)
        GlStateUtils.cull(false)
        color.setGLColor()
        glPushMatrix()
        glTranslatef(posXIn, posYIn - 0.5f, 0.0f)
        glScalef(0.28f * scale, 0.28f * scale, 1.0f)

        currentVariant = glyphArray[0]
        for ((index, char) in text.withIndex()) {
            if (checkStyleCode(text, index)) continue
            val charInfo = currentVariant.getCharInfo(char)
            val chunk = currentVariant.getChunk(char)

            GlStateManager.bindTexture(chunk.textureId)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, -0.333f)

            if (char == '\n') {
                posY += currentVariant.fontHeight * 0.8
                posX = 0.0
            } else {
                val pos1 = Vec2d(posX, posY)
                val pos2 = pos1.add(charInfo.width.toDouble(), charInfo.height.toDouble())
                val texPos1 = Vec2d(charInfo.posX.toDouble(), charInfo.posY.toDouble()).divide(TEXTURE_WIDTH.toDouble(), chunk.textureHeight.toDouble())
                val texPos2 = texPos1.add(Vec2d(charInfo.width.toDouble(), charInfo.height.toDouble()).divide(TEXTURE_WIDTH.toDouble(), chunk.textureHeight.toDouble()))

                drawQuad(pos1, pos2, texPos1, texPos2)
                posX += charInfo.width - 1.5f
            }
        }

        glPopMatrix()
        glEnable(GL_ALPHA_TEST)
        GlStateUtils.cull(true)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, 0.0f)
        glColor4f(1f, 1f, 1f, 1f)
    }

    private fun drawQuad(pos1: Vec2d, pos2: Vec2d, texPos1: Vec2d, texPos2: Vec2d) {
        buffer.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX)
        buffer.pos(pos1.x, pos1.y, 0.0).tex(texPos1.x, texPos1.y).endVertex()
        buffer.pos(pos2.x, pos1.y, 0.0).tex(texPos2.x, texPos1.y).endVertex()
        buffer.pos(pos1.x, pos2.y, 0.0).tex(texPos1.x, texPos2.y).endVertex()
        buffer.pos(pos2.x, pos2.y, 0.0).tex(texPos2.x, texPos2.y).endVertex()
        tessellator.draw()
    }

    @JvmOverloads
    fun getFontHeight(scale: Float = 1f): Float {
        return glyphArray[0].fontHeight * 0.25f * scale
    }

    @JvmOverloads
    fun getStringWidth(text: String, scale: Float = 1f): Float {
        var width = 0f
        currentVariant = glyphArray[0]
        for ((index, char) in text.withIndex()) {
            if (checkStyleCode(text, index)) continue
            width += currentVariant.getCharInfo(char).width.minus(1.5f)
        }

        return width * 0.28f * scale
    }

    private fun checkStyleCode(text: String, index: Int): Boolean {
        if (text.getOrNull(index - 1) == '§') return true

        if (text.getOrNull(index) == '§') {
            when (text.getOrNull(index + 1)) {
                TextProperties.Style.REGULAR.codeChar -> currentVariant = glyphArray[0]
                TextProperties.Style.BOLD.codeChar -> currentVariant = glyphArray[1]
                TextProperties.Style.ITALIC.codeChar -> currentVariant = glyphArray[2]
            }
            return true
        }

        return false
    }
}