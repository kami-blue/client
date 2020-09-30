package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.RenderUtils2D
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.KamiFontRenderer
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11.*

@Module.Info(
        name = "TextureTest",
        category = Module.Category.RENDER,
        description = "Texture testing"
)
object TextureTest : Module() {
    override fun onRender() {
        glColor4f(1f, 1f, 1f, 1f)


        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        val vertexHelper = VertexHelper(GlStateUtils.useVbo())

        val chunk1 = KamiFontRenderer.glyphArray[0].getChunk(0)
        GlStateManager.bindTexture(chunk1.textureId)
        glColor4f(1f, 1f, 1f, 1f)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)

        GlStateUtils.blend(true)
        GlStateUtils.cull(false)
        buffer.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX)
        buffer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).endVertex()
        buffer.pos(256.0, 0.0, 0.0).tex(1.0, 0.0).endVertex()
        buffer.pos(0.0, chunk1.textureHeight / 4.0, 0.0).tex(0.0, 1.0).endVertex()
        buffer.pos(256.0, chunk1.textureHeight / 4.0, 0.0).tex(1.0, 1.0).endVertex()
        tessellator.draw()

        RenderUtils2D.drawRectOutline(vertexHelper, Vec2d(0.0, 0.0), Vec2d(256.0, chunk1.textureHeight / 4.0), color = ColorHolder(255, 0, 255))

        val chunk2 = KamiFontRenderer.glyphArray[0].getChunk(48)
        GlStateManager.bindTexture(chunk2.textureId)
        glColor4f(1f, 1f, 1f, 1f)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)

        GlStateUtils.blend(true)
        GlStateUtils.cull(false)
        buffer.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX)
        buffer.pos(256.0, 0.0, 0.0).tex(0.0, 0.0).endVertex()
        buffer.pos(512.0, 0.0, 0.0).tex(1.0, 0.0).endVertex()
        buffer.pos(256.0, chunk2.textureHeight / 4.0, 0.0).tex(0.0, 1.0).endVertex()
        buffer.pos(512.0, chunk2.textureHeight / 4.0, 0.0).tex(1.0, 1.0).endVertex()
        tessellator.draw()

        RenderUtils2D.drawRectOutline(vertexHelper, Vec2d(256.0, 0.0), Vec2d(512.0, chunk2.textureHeight / 4.0), color = ColorHolder(255, 0, 255))

        val chunk3 = KamiFontRenderer.glyphArray[0].getChunk(49)
        GlStateManager.bindTexture(chunk3.textureId)
        glColor4f(1f, 1f, 1f, 1f)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)

        GlStateUtils.blend(true)
        GlStateUtils.cull(false)
        buffer.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX)
        buffer.pos(512.0, 0.0, 0.0).tex(0.0, 0.0).endVertex()
        buffer.pos(768.0, 0.0, 0.0).tex(1.0, 0.0).endVertex()
        buffer.pos(512.0, chunk3.textureHeight / 4.0, 0.0).tex(0.0, 1.0).endVertex()
        buffer.pos(768.0, chunk3.textureHeight / 4.0, 0.0).tex(1.0, 1.0).endVertex()
        tessellator.draw()

        RenderUtils2D.drawRectOutline(vertexHelper, Vec2d(512.0, 0.0), Vec2d(768.0, chunk2.textureHeight / 4.0), color = ColorHolder(255, 0, 255))

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    }
}