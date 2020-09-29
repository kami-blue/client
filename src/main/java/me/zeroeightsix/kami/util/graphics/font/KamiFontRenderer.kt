package me.zeroeightsix.kami.util.graphics.font

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.modules.ClickGUI
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.*
import org.lwjgl.opengl.GL14.*
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import kotlin.math.max

/**
 * Adapted from Bobjob's edited version of Slick's TrueTypeFont.
 * http://forum.lwjgl.org/index.php?topic=2951
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
    /** Default font texture width */
    private const val TEXTURE_WIDTH = 512

    /** Default font texture height */
    private const val TEXTURE_HEIGHT = 512

    /**
     * Stores different variants (Regular, Bold, Italic) for the font
     * 0: Regular, 1: Bold, 2: Italic
     */
    private val fontVariants: Array<FontVariant>

    /** CurrentVariant being used */
    private var currentVariant: FontVariant

    val glyphTexture get() = fontVariants[0].fontTextureID

    init {
        fontVariants = Array(3) {
            val style = TextProperties.Style.values()[it]
            val font = try {
                val inputStream = KamiFontRenderer::class.java.getResourceAsStream(style.fontPath)
                Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(32f)
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                KamiMod.log.error("Font not loaded. Using serif font.")
                Font("serif", Font.PLAIN, 32)
            }
            FontVariant(font)
        }
        currentVariant = fontVariants[0]
    }

    private class FontVariant(val font: Font) {
        /** Font's size  */
        private val fontSize = font.size

        /** Font's height  */
        var fontHeight = 0f
            private set

        /** Array that holds necessary information about the font characters  */
        val charArray = arrayOfNulls<IntObject>(256)

        /** Texture used to cache the font 0-255 characters  */
        val fontTextureID: Int

        init {
            fontTextureID = try {
                val tempImage = BufferedImage(TEXTURE_WIDTH, TEXTURE_HEIGHT, BufferedImage.TYPE_INT_ARGB)
                val graphics2D = tempImage.graphics as Graphics2D
                graphics2D.color = Color(0, 0, 0, 1)
                graphics2D.fillRect(0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT)

                var rowHeight = 0
                var positionX = 0
                var positionY = 0

                for (i in 0 until 256) {
                    val char = i.toChar()
                    val fontImage = getFontImage(char)
                    if (positionX + fontImage.width >= TEXTURE_WIDTH) {
                        positionX = 0
                        positionY += rowHeight
                        rowHeight = 0
                    }
                    val intObject = IntObject(fontImage.width, fontImage.height, positionX, positionY)

                    fontHeight = max(intObject.height.toFloat(), fontHeight)
                    rowHeight = max(intObject.height, rowHeight)

                    // Draw it here
                    graphics2D.drawImage(fontImage, positionX, positionY, null)
                    positionX += intObject.width + 2

                    if (i < 256) charArray[i] = intObject
                }

                loadImage(tempImage)
            } catch (e: Exception) {
                KamiMod.log.error("Failed to create font.")
                e.printStackTrace()
                0
            }
        }

        private fun getFontImage(char: Char): BufferedImage {
            // Create a temporary image to extract the character's size
            val tempFontImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
            val tempGraphics2D = tempFontImage.graphics as Graphics2D
            tempGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            tempGraphics2D.font = font
            val fontMetrics = tempGraphics2D.fontMetrics

            var charwidth = fontMetrics.charWidth(char)
            if (charwidth <= 0) charwidth = 8

            var charheight = fontMetrics.height
            if (charheight <= 0) charheight = fontSize

            // Create another image holding the character we are creating
            val fontImage = BufferedImage(charwidth, charheight, BufferedImage.TYPE_INT_ARGB)
            val graphics2D = fontImage.graphics as Graphics2D

            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics2D.font = font
            graphics2D.color = Color.WHITE
            graphics2D.drawString(char.toString(), 0, fontMetrics.ascent - 2)
            return fontImage
        }

        private fun loadImage(image: BufferedImage): Int {
            return try {
                val dynamicTexture = DynamicTexture(image)
                dynamicTexture.loadTexture(Wrapper.minecraft.getResourceManager())
                val textureId = dynamicTexture.glTextureId

                // Tells Gl that our texture isn't a repeating texture (edges are connecting to each others)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

                // Setup texture filters
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
                glHint(GL_GENERATE_MIPMAP_HINT, GL_NICEST)

                // Setup mipmap parameters
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_LOD, 0)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LOD, 3)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 3)
                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, 0f)
                GlStateManager.bindTexture(textureId)

                // We only need 3 levels of mipmaps for 32 sized font
                // 0: 64 x 64, 1: 32 x 32, 2: 16 x 16, 3: 8 x 8
                for (mipmapLevel in 0..3) {
                    // GL_ALPHA means that the texture is a grayscale texture (black & white and alpha only)
                    glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_ALPHA, image.width shr mipmapLevel, image.height shr mipmapLevel, 0, GL_ALPHA, GL_UNSIGNED_BYTE, null as ByteBuffer?)
                }

                glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, 1)
                TextureUtil.uploadTextureImageSub(textureId, image, 0, 0, true, true)
                textureId
            } catch (e: Exception) {
                e.printStackTrace()
                return -1
            }
        }

        inner class IntObject(
                /** Character's width  */
                val width: Int,

                /** Character's height  */
                val height: Int,

                /** Character's stored x position  */
                val storedX: Int,

                /** Character's stored y position  */
                val storedY: Int
        )
    }

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
        glTranslatef(posXIn, posYIn, 0.0f)
        glScalef(0.28f * scale, 0.28f * scale, 1.0f)

        currentVariant = fontVariants[0]
        for ((index, char) in text.withIndex()) {
            if (checkStyleCode(text, index)) continue
            val intObject = currentVariant.charArray.getOrNull(char.toInt()) ?: continue

            GlStateManager.bindTexture(currentVariant.fontTextureID)
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
                val pos2 = pos1.add(intObject.width.toDouble(), intObject.height.toDouble())
                val texPos1 = Vec2d(intObject.storedX.toDouble(), intObject.storedY.toDouble()).divide(TEXTURE_WIDTH.toDouble(), TEXTURE_HEIGHT.toDouble())
                val texPos2 = texPos1.add(Vec2d(intObject.width.toDouble(), intObject.height.toDouble()).divide(TEXTURE_WIDTH.toDouble(), TEXTURE_HEIGHT.toDouble()))

                drawQuad(pos1, pos2, texPos1, texPos2)
                posX += intObject.width - 2f
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
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        buffer.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX)
        buffer.pos(pos1.x, pos1.y, 0.0).tex(texPos1.x, texPos1.y).endVertex()
        buffer.pos(pos2.x, pos1.y, 0.0).tex(texPos2.x, texPos1.y).endVertex()
        buffer.pos(pos1.x, pos2.y, 0.0).tex(texPos1.x, texPos2.y).endVertex()
        buffer.pos(pos2.x, pos2.y, 0.0).tex(texPos2.x, texPos2.y).endVertex()
        tessellator.draw()
    }

    @JvmOverloads
    fun getFontHeight(scale: Float = 1f): Float {
        return fontVariants[0].fontHeight * 0.25f * scale
    }

    @JvmOverloads
    fun getStringWidth(text: String, scale: Float = 1f): Float {
        var width = 0
        currentVariant = fontVariants[0]
        for ((index, char) in text.withIndex()) {
            if (checkStyleCode(text, index)) continue
            width += currentVariant.charArray.getOrNull(char.toInt())?.width?.minus(2) ?: 0
        }

        return width * 0.28f * scale
    }

    private fun checkStyleCode(text: String, index: Int): Boolean {
        if (text.getOrNull(index - 1) == '§') return true

        if (text.getOrNull(index) == '§') {
            when (text.getOrNull(index + 1)) {
                TextProperties.Style.REGULAR.codeChar -> currentVariant = fontVariants[0]
                TextProperties.Style.BOLD.codeChar -> currentVariant = fontVariants[1]
                TextProperties.Style.ITALIC.codeChar -> currentVariant = fontVariants[2]
            }
            return true
        }

        return false
    }
}