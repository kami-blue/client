package org.kamiblue.client.util.graphics.texture

import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.glGenTextures

abstract class AbstractTexture {

    var textureID: Int = -1; private set

    abstract val width: Int
    abstract val height: Int

    fun genTexture() {
        textureID = glGenTextures()
    }

    fun bindTexture() {
        if (textureID != -1) {
            GlStateManager.bindTexture(textureID)
        }
    }

    fun unbindTexture() {
        GlStateManager.bindTexture(0)
    }

    fun deleteTexture() {
        if (textureID != -1) {
            GlStateManager.deleteTexture(textureID)
            textureID = -1
        }
    }

    override fun equals(other: Any?) =
        this === other
            || other is AbstractTexture
            && this.textureID == other.textureID

    override fun hashCode() = textureID

}