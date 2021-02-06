package org.kamiblue.client.util.graphics.font

import org.kamiblue.client.util.graphics.texture.MipmapTexture
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14

class GlyphChunk(
    /** Id of this chunk */
    val chunk: Int,

    /** [MipmapTexture] object */
    val texture: MipmapTexture,

    /** Array for all characters' info in this chunk */
    val charInfoArray: Array<CharInfo>
) {
    private var lodbias = 0.0f

    fun updateLodBias(input: Float) {
        if (input != lodbias) {
            lodbias = input
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, input)
        }
    }

    override fun equals(other: Any?) =
        this === other
            || other is GlyphChunk
            && chunk == other.chunk
            && texture == other.texture

    override fun hashCode() = 31 * chunk + texture.hashCode()
}