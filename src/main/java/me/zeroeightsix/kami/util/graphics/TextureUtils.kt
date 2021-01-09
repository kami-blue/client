package me.zeroeightsix.kami.util.graphics

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.TextureUtil
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.*
import org.lwjgl.opengl.GL14.*
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

object TextureUtils {
    fun genTextureWithMipmaps(bufferedImage: BufferedImage, level: Int, textureFormat: Int) : DynamicTexture {
        val dynamicTexture = DynamicTexture(bufferedImage)
        val textureId = dynamicTexture.glTextureId

        val depth = glGetBoolean(GL_DEPTH_TEST)
        val blend = glGetBoolean(GL_BLEND)
        GlStateUtils.depth(false)
        GlStateUtils.blend(true)
        GlStateManager.tryBlendFuncSeparate(GL_ONE, GL_ZERO, GL_SRC_ALPHA, GL_ZERO)

        // Tells Gl that our texture isn't a repeating texture (edges are not connecting to each others)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        // Setup texture filters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glHint(GL_GENERATE_MIPMAP_HINT, GL_NICEST)

        // Setup mipmap parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_LOD, 0)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LOD, level)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, level)
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, 0.0f)
        GlStateManager.bindTexture(textureId)

        for (mipmapLevel in 0..level) {
            // GL_ALPHA means that the texture is a grayscale texture (black & white and alpha only)
            glTexImage2D(GL_TEXTURE_2D, mipmapLevel, textureFormat, bufferedImage.width shr mipmapLevel, bufferedImage.height shr mipmapLevel, 0, textureFormat, GL_UNSIGNED_BYTE, null as ByteBuffer?)
        }

        glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, 1)
        TextureUtil.uploadTextureImageSub(textureId, bufferedImage, 0, 0, true, true)

        GlStateUtils.resetTexParam()
        GlStateUtils.depth(depth)
        GlStateUtils.blend(blend)
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        return dynamicTexture
    }
}