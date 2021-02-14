package org.kamiblue.client.util.graphics.font

import org.kamiblue.client.KamiMod
import org.kamiblue.client.util.graphics.texture.MipmapTexture
import org.kamiblue.commons.utils.MathUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min

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
class FontGlyphs(private val font: Font, private val fallbackFont: Font) {

    /** HashMap for storing all the glyph chunks, each chunk contains 256 glyphs mapping to characters */
    private val chunkMap = HashMap<Int, GlyphChunk>()

    /** Font height */
    val fontHeight: Float

    init {
        // Loads the basic 256 characters on init
        fontHeight = loadGlyphChunk(0)?.let { chunk ->
            chunkMap[0] = chunk
            chunk.charInfoArray.maxByOrNull { it.height }?.height?.toFloat() ?: 64.0f
        } ?: 64.0f
    }

    /** @return CharInfo of [char] */
    fun getCharInfo(char: Char): CharInfo {
        val charInt = char.toInt()
        val chunk = charInt shr 8
        val chunkStart = chunk shl 8
        return getChunk(chunk).charInfoArray[charInt - chunkStart]
    }

    /** @return the chunk the [char] is in */
    fun getChunk(char: Char) = getChunk(char.toInt() shr 8)

    /** @return the chunk */
    fun getChunk(chunk: Int): GlyphChunk = chunkMap.getOrPut(chunk) {
        // Try to load the glyph chunk if absent
        // Return an empty char if failed to load glyph
        // And let it fix itself next time
        loadGlyphChunk(chunk) ?: return chunkMap[0]!!
    }

    /** Delete all textures */
    fun destroy() {
        for (chunk in chunkMap.values) {
            chunk.texture.deleteTexture()
        }
        chunkMap.clear()
    }

    private fun loadGlyphChunk(chunk: Int): GlyphChunk? {
        return try {
            val chunkStart = chunk shl 8
            val bufferedImage = BufferedImage(TEXTURE_WIDTH, MAX_TEXTURE_HEIGHT, BufferedImage.TYPE_INT_ARGB)
            val graphics2D = bufferedImage.graphics as Graphics2D
            graphics2D.background = Color(0, 0, 0, 0)

            var rowHeight = 0
            var positionX = 1
            var positionY = 1

            val builderArray = Array(256) {
                val char = (chunkStart + it).toChar() // Plus 1 because char starts at 1 not 0
                val charImage = getCharImage(char) // Get image for this character

                if (positionX + charImage.width >= TEXTURE_WIDTH) { // Move to the next line if we reach the texture width
                    positionX = 1
                    positionY += rowHeight
                    rowHeight = 0
                }

                val builder = CharInfoBuilder(positionX, positionY, charImage.width, charImage.height) // Create char info for this character
                rowHeight = max(charImage.height, rowHeight) // Update row height
                graphics2D.drawImage(charImage, positionX, positionY, null) // Draw it here

                positionX += charImage.width + 2 // Move right for next character
                builder // Return the char info builder to lambda
            }

            val textureHeight = min(MathUtils.ceilToPOT(positionY + rowHeight), MAX_TEXTURE_HEIGHT)
            val textureImage = BufferedImage(TEXTURE_WIDTH, textureHeight, BufferedImage.TYPE_INT_ARGB)
            (textureImage.graphics as Graphics2D).drawImage(bufferedImage, 0, 0, null)

            val texture = createTexture(textureImage)
            val charInfoArray = builderArray.map { it.build(textureHeight.toDouble()) }.toTypedArray()
            GlyphChunk(chunk, texture, charInfoArray)

        } catch (e: Exception) {
            KamiMod.LOG.error("Failed to load glyph chunk $chunk.", e)
            null
        }
    }

    private fun getCharImage(char: Char): BufferedImage {
        val font = when {
            font.canDisplay(char.toInt()) -> font
            fallbackFont.canDisplay(char.toInt()) -> fallbackFont
            else -> font
        }

        // Create a temporary image to extract the character's size
        val tempGraphics2D = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics()
        tempGraphics2D.font = font
        val fontMetrics = tempGraphics2D.fontMetrics
        tempGraphics2D.dispose()

        val charWidth = if (fontMetrics.charWidth(char) > 0) fontMetrics.charWidth(char) else 8
        val charHeight = if (fontMetrics.height > 0) fontMetrics.height else font.size

        // Create another image holding the character we are creating
        val charImage = BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics2D = charImage.createGraphics()

        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics2D.font = font
        graphics2D.color = Color.WHITE
        graphics2D.drawString(char.toString(), 0, fontMetrics.ascent)
        graphics2D.dispose()

        return charImage
    }

    private fun createTexture(bufferedImage: BufferedImage) = MipmapTexture(bufferedImage, GL_ALPHA, 4).apply {
        bindTexture()
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, 0.0f)
        unbindTexture()
    }

    class CharInfoBuilder(val posX: Int, val posY: Int, val width: Int, val height: Int) {
        fun build(textureHeight: Double): CharInfo {
            return CharInfo(
                width.toDouble(),
                height.toDouble(),
                posX / TEXTURE_WIDTH_DOUBLE,
                posY / textureHeight,
                (posX + width) / TEXTURE_WIDTH_DOUBLE,
                (posY + height) / textureHeight
            )
        }
    }

    companion object {
        /** Default font texture width */
        const val TEXTURE_WIDTH = 1024

        /** Default font texture width */
        const val TEXTURE_WIDTH_DOUBLE = 1024.0

        /** Max font texture height */
        const val MAX_TEXTURE_HEIGHT = 4096
    }
}