package me.zeroeightsix.kami.util.graphics.font

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.color.DyeColors
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.font.FontGlyphs.Companion.TEXTURE_WIDTH
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.text.TextFormatting
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS
import java.awt.Font
import java.awt.GraphicsEnvironment

/**
 * Adapted from Bobjob's edited version of Slick's TrueTypeFont.
 * http://forum.lwjgl.org/index.php?topic=2951
 *
 * License
 * Copyright (c) 2013, Slick2D
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of the Slick2D nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS”
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

    /** For Minecraft color code only */
    private var currentColor = ColorHolder(255, 255, 255)

    /** Available fonts on in the system */
    private val availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts.map { it.name }.toHashSet()

    /** All for the KAMI Blue kanji */
    private val fallbackFonts = arrayOf(
            "Noto Sans JP", "Noto Sans CJK JP", "Noto Sans CJK JP", "Noto Sans CJK KR", "Noto Sans CJK SC", "Noto Sans CJK TC", // Noto Sans
            "Source Han Sans", "Source Han Sans HC", "Source Han Sans SC", "Source Han Sans TC", "Source Han Sans K", // Source Sans
            "MS Gothic", "Meiryo", "Yu Gothic", // For Windows, Windows on top!
            "Hiragino Sans GB W3", "Hiragino Kaku Gothic Pro W3", "Hiragino Kaku Gothic ProN W3", "Osaka", // For stupid Mac OSX
            "TakaoPGothic", "IPAPGothic" // For cringy Linux
    )

    init {
        // Prints Slick2D's license to log as required
        KamiMod.log.info("""Slick2D's TrueTypeFont renderer code was used in this mod
            
            License
            Copyright (c) 2013, Slick2D
            
            All rights reserved.
            
            Redistribution and use in source and binary forms, with or without modification,
            are permitted provided that the following conditions are met:

            - Redistributions of source code must retain the above copyright notice,
              this list of conditions and the following disclaimer.

            - Redistributions in binary form must reproduce the above copyright notice,
              this list of conditions and the following disclaimer in the documentation
              and/or other materials provided with the distribution.

            - Neither the name of the Slick2D nor the names of its contributors may be
              used to endorse or promote products derived from this software without
              specific prior written permission.

            THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS”
            AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
            THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
            IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
            INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
            (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
            LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
            HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
            OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
            EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
            
        """.trimIndent())

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
            FontGlyphs(style, font, fallbackFont)
        }
        currentVariant = glyphArray[0]
    }

    private fun getFallbackFont() = fallbackFonts.firstOrNull { availableFonts.contains(it) }

    private fun getSansSerifFont(style: Int) = Font("SansSerif", style, 32)

    @JvmOverloads
    fun drawString(text: String, posXIn: Float = 0f, posYIn: Float = 0f, drawShadow: Boolean = true, color: ColorHolder = ColorHolder(255, 255, 255), scale: Float = 1f) {
        if (drawShadow) {
            drawString(text, posXIn + 0.7f, posYIn + 0.7f, color, scale, true)
        }
        drawString(text, posXIn, posYIn, color, scale, false)
    }

    private fun drawString(text: String, posXIn: Float, posYIn: Float, colorIn: ColorHolder, scale: Float, isShadow: Boolean) {
        var posX = 0.0
        var posY = 0.0

        val lighting = glGetBoolean(GL_LIGHTING)
        glDisable(GL_LIGHTING)
        glDisable(GL_ALPHA_TEST)
        GlStateUtils.blend(true)
        GlStateUtils.cull(false)
        glPushMatrix()
        glTranslatef(posXIn, posYIn, 0.0f)
        glScalef(0.28f * scale, 0.28f * scale, 1.0f)

        resetStyle()

        for ((index, char) in text.withIndex()) {
            if (checkStyleCode(text, index)) continue
            val charInfo = currentVariant.getCharInfo(char)
            val chunk = currentVariant.getChunk(char)

            val color = if (currentColor == DyeColors.WHITE.color) colorIn else currentColor
            if (isShadow) getShadowColor(color).setGLColor() else color.setGLColor()

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
                posX += charInfo.width - 1f
            }
        }
        resetStyle()
        glColor4f(1f, 1f, 1f, 1f)

        glPopMatrix()
        if (lighting) glEnable(GL_LIGHTING)
        glEnable(GL_ALPHA_TEST)
        GlStateUtils.cull(true)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, 0.0f)
        glColor4f(1f, 1f, 1f, 1f)
    }

    private fun getShadowColor(color: ColorHolder) = ColorHolder((color.r * 0.2f).toInt(), (color.g * 0.2f).toInt(), (color.b * 0.2f).toInt(), (color.a * 0.9f).toInt())

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
        resetStyle()
        for ((index, char) in text.withIndex()) {
            if (checkStyleCode(text, index)) continue
            width += currentVariant.getCharInfo(char).width.minus(1f)
        }
        resetStyle()
        return width * 0.28f * scale
    }

    private fun resetStyle() {

        currentVariant = glyphArray[0]
        currentColor = DyeColors.WHITE.color
    }

    private fun checkStyleCode(text: String, index: Int): Boolean {
        if (text.getOrNull(index - 1) == '§') return true

        if (text.getOrNull(index) == '§') {
            when (text.getOrNull(index + 1)) {
                TextProperties.Style.REGULAR.codeChar -> currentVariant = glyphArray[0]
                TextProperties.Style.BOLD.codeChar -> currentVariant = glyphArray[1]
                TextProperties.Style.ITALIC.codeChar -> currentVariant = glyphArray[2]
            }
            currentColor = when (text.getOrNull(index + 1)) {
                TextFormatting.BLACK.toString()[1] -> ColorHolder(0, 0, 0)
                TextFormatting.DARK_BLUE.toString()[1] -> ColorHolder(0, 0, 170)
                TextFormatting.DARK_GREEN.toString()[1] -> ColorHolder(0, 170, 0)
                TextFormatting.DARK_AQUA.toString()[1] -> ColorHolder(0, 170, 170)
                TextFormatting.DARK_RED.toString()[1] -> ColorHolder(170, 0, 0)
                TextFormatting.DARK_PURPLE.toString()[1] -> ColorHolder(170, 0, 170)
                TextFormatting.GOLD.toString()[1] -> ColorHolder(250, 170, 0)
                TextFormatting.GRAY.toString()[1] -> ColorHolder(170, 170, 170)
                TextFormatting.DARK_GRAY.toString()[1] -> ColorHolder(85, 85, 85)
                TextFormatting.BLUE.toString()[1] -> ColorHolder(85, 85, 255)
                TextFormatting.GREEN.toString()[1] -> ColorHolder(85, 255, 85)
                TextFormatting.AQUA.toString()[1] -> ColorHolder(85, 255, 255)
                TextFormatting.RED.toString()[1] -> ColorHolder(255, 85, 85)
                TextFormatting.LIGHT_PURPLE.toString()[1] -> ColorHolder(255, 85, 255)
                TextFormatting.YELLOW.toString()[1] -> ColorHolder(255, 255, 85)
                TextFormatting.WHITE.toString()[1] -> ColorHolder(255, 255, 255)
                TextFormatting.RESET.toString()[1] -> ColorHolder(255, 255, 255)
                else -> currentColor
            }
            return true
        }

        return false
    }
}