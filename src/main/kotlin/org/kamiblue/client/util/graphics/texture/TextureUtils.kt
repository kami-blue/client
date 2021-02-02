package org.kamiblue.client.util.graphics.texture

import net.minecraft.client.renderer.GLAllocation
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.glTexImage2D
import org.lwjgl.opengl.GL12.GL_BGRA
import org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV
import java.awt.RenderingHints
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.nio.IntBuffer
import kotlin.math.roundToInt
import kotlin.math.sqrt

object TextureUtils {
    private val buffer: IntBuffer = GLAllocation.createDirectIntBuffer(0x400000)

    fun uploadImage(bufferedImage: BufferedImage, level: Int, format: Int, width: Int, height: Int) {
        val data = IntArray(width * height)
        bufferedImage.getRGB(0, 0, width, height, data, 0, width)
        buffer.put(data)

        buffer.flip()
        glTexImage2D(GL_TEXTURE_2D, level, format, width, height, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer)
        buffer.clear()
    }

    fun BufferedImage.scaleDownPretty(targetWidth: Int, targetHeight: Int): BufferedImage {
        val type = if (this.transparency == Transparency.OPAQUE) {
            BufferedImage.TYPE_INT_RGB
        } else {
            BufferedImage.TYPE_INT_ARGB
        }

        var bufferedImage: BufferedImage = this

        var width = this.width
        var height = this.height

        val divisorX = sqrt((width / targetWidth).toDouble())
        val divisorY = sqrt((height / targetHeight).toDouble())

        do {
            if (width > targetWidth) {
                width = (width / divisorX).roundToInt().coerceAtLeast(targetWidth)
            }

            if (height > targetHeight) {
                height = (height / divisorY).roundToInt().coerceAtLeast(targetHeight)
            }

            val tempImage = BufferedImage(width, height, type)
            val graphics2D = tempImage.createGraphics()

            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            graphics2D.drawImage(bufferedImage, 0, 0, width, height, null)
            graphics2D.dispose()

            bufferedImage = tempImage
        } while (width != targetWidth || height != targetHeight)

        return bufferedImage
    }
}