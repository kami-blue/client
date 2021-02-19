package org.kamiblue.client.util.graphics.font

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.text.TextFormatting
import org.kamiblue.client.KamiMod
import org.kamiblue.client.module.modules.client.CustomFont
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.color.DyeColors
import org.kamiblue.client.util.graphics.GlStateUtils
import org.lwjgl.opengl.GL11.*
import java.awt.Font

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
    private val glyphArray: Array<FontGlyphs>

    /** CurrentVariant being used */
    private var currentVariant: FontGlyphs

    /** For Minecraft color code only */
    private var currentColor = ColorHolder(255, 255, 255)

    /** All for the KAMI Blue kanji */
    private val fallbackFonts = arrayOf(
        "Noto Sans JP", "Noto Sans CJK JP", "Noto Sans CJK JP", "Noto Sans CJK KR", "Noto Sans CJK SC", "Noto Sans CJK TC", // Noto Sans
        "Source Han Sans", "Source Han Sans HC", "Source Han Sans SC", "Source Han Sans TC", "Source Han Sans K", // Source Sans
        "MS Gothic", "Meiryo", "Yu Gothic", // For Windows, Windows on top!
        "Hiragino Sans GB W3", "Hiragino Kaku Gothic Pro W3", "Hiragino Kaku Gothic ProN W3", "Osaka", // For stupid Mac OSX
        "TakaoPGothic", "IPAPGothic" // For cringy Linux
    )

    init {
        glyphArray = Array(3) {
            loadFont(it)
        }
        currentVariant = glyphArray[0]
    }

    fun reloadFonts() {
        for (i in glyphArray.indices) {
            glyphArray[i].destroy()
            glyphArray[i] = loadFont(i)
        }
    }

    private fun loadFont(index: Int): FontGlyphs {
        val style = Style.values()[index]

        // Load main font
        val font = try {
            if (CustomFont.isDefaultFont) {
                val inputStream = this.javaClass.getResourceAsStream(style.fontPath)
                Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(64.0f)
            } else {
                Font(CustomFont.fontName.value, style.styleConst, 64)
            }
        } catch (e: Exception) {
            KamiMod.LOG.warn("Failed loading main font. Using Sans Serif font.", e)
            getSansSerifFont(style.styleConst)
        }

        // Load fallback font
        val fallbackFont = try {
            Font(getFallbackFont(), style.styleConst, 64)
        } catch (e: Exception) {
            KamiMod.LOG.warn("Failed loading fallback font. Using Sans Serif font", e)
            getSansSerifFont(style.styleConst)
        }

        return FontGlyphs(font, fallbackFont)
    }

    private fun getFallbackFont() = fallbackFonts.firstOrNull { CustomFont.availableFonts.contains(it) }

    private fun getSansSerifFont(style: Int) = Font("SansSerif", style, 64)

    fun drawString(text: String, posXIn: Float = 0f, posYIn: Float = 0f, drawShadow: Boolean = true, colorIn: ColorHolder = ColorHolder(255, 255, 255), scale: Float = 1f) {
        var posX = 0.0
        var posY = 0.0

        GlStateManager.disableOutlineMode() // Weird fix for black text
        GlStateUtils.texture2d(true)
        GlStateUtils.alpha(false)
        GlStateUtils.blend(true)
        glPushMatrix()
        glTranslatef(posXIn, posYIn, 0.0f)
        glScalef(CustomFont.size * scale, CustomFont.size * scale, 1.0f)
        glTranslatef(0.0f, CustomFont.baselineOffset, 0.0f)
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)

        resetStyle()
        var lastChunk: GlyphChunk? = null
        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)

        for ((index, char) in text.withIndex()) {
            if (checkFormatCode(text, index)) continue

            if (char == '\n') {
                posY += currentVariant.fontHeight * CustomFont.lineSpace
                posX = 0.0
            } else {
                val chunk = currentVariant.getChunk(char)
                val charInfo = currentVariant.getCharInfo(char)
                val color = if (currentColor == DyeColors.WHITE.color) colorIn else currentColor

                if (chunk != lastChunk) {
                    if (lastChunk != null) {
                        tessellator.draw()
                        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
                    }
                    chunk.texture.bindTexture()
                    chunk.updateLodBias(CustomFont.lodBias)
                }

                if (drawShadow) {
                    drawQuad(posX + 4.5, posY + 4.5, charInfo, getShadowColor(color))
                }

                drawQuad(posX, posY, charInfo, color)
                posX += charInfo.width + CustomFont.gap

                lastChunk = chunk
            }
        }

        tessellator.draw()
        resetStyle()

        glPopMatrix()
        GlStateUtils.alpha(true)
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
    }

    private fun getShadowColor(color: ColorHolder) = ColorHolder((color.r * 0.2f).toInt(), (color.g * 0.2f).toInt(), (color.b * 0.2f).toInt(), (color.a * 0.9f).toInt())

    private fun drawQuad(posX: Double, posY: Double, charInfo: CharInfo, color: ColorHolder) {
        buffer.pos(posX, posY, 0.0).tex(charInfo.u1, charInfo.v1).color(color.r, color.g, color.b, color.a).endVertex()
        buffer.pos(posX, posY + charInfo.height, 0.0).tex(charInfo.u1, charInfo.v2).color(color.r, color.g, color.b, color.a).endVertex()
        buffer.pos(posX + charInfo.width, charInfo.height, 0.0).tex(charInfo.u2, charInfo.v2).color(color.r, color.g, color.b, color.a).endVertex()
        buffer.pos(posX + charInfo.width, posY, 0.0).tex(charInfo.u2, charInfo.v1).color(color.r, color.g, color.b, color.a).endVertex()
    }

    fun getFontHeight(scale: Float = 1f): Float {
        return (glyphArray[0].fontHeight * CustomFont.lineSpace * scale)
    }

    fun getStringWidth(text: String, scale: Float = 1f): Float {
        var width = 0.0
        resetStyle()
        for ((index, char) in text.withIndex()) {
            if (checkFormatCode(text, index)) continue
            width += currentVariant.getCharInfo(char).width + CustomFont.gap
        }
        resetStyle()
        return (width * CustomFont.size * scale).toFloat()
    }

    private fun resetStyle() {
        currentVariant = glyphArray[0]
        currentColor = DyeColors.WHITE.color
    }

    private fun checkFormatCode(text: String, index: Int): Boolean {
        if (text.getOrNull(index - 1) == '§') return true

        if (text.getOrNull(index) == '§') {
            val nextChar = text.getOrNull(index + 1)
            checkStyleCode(nextChar)
            checkColorCode(nextChar)
            return true
        }

        return false
    }

    private fun checkStyleCode(char: Char?) {
        currentVariant = when (char) {
            Style.REGULAR.codeChar -> glyphArray[0]
            Style.BOLD.codeChar -> glyphArray[1]
            Style.ITALIC.codeChar -> glyphArray[2]
            else -> currentVariant
        }
    }

    private fun checkColorCode(char: Char?) {
        currentColor = when (char) {
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
            TextFormatting.WHITE.toString()[1] -> DyeColors.WHITE.color
            TextFormatting.RESET.toString()[1] -> DyeColors.WHITE.color
            else -> currentColor
        }
    }
}