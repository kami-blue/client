package me.zeroeightsix.kami.util.graphics

import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.vertex.VertexFormat
import org.lwjgl.opengl.GL11.*

object GlStateUtils : Tessellator(0x200000) {

    fun useVbo(): Boolean {
        return Wrapper.getMinecraft().gameSettings.useVbo
    }

    fun blend(state: Boolean) {
        if (state) {
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        } else {
            glDisable(GL_BLEND)
        }
    }

    fun smooth(state: Boolean) {
        if (state) {
            glShadeModel(GL_SMOOTH)
        } else {
            glShadeModel(GL_FLAT)
        }
    }

    fun lineSmooth(state: Boolean) {
        if (state) {
            glEnable(GL_LINE_SMOOTH)
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        } else {
            glDisable(GL_LINE_SMOOTH)
        }
    }

    fun depth(state: Boolean) {
        if (state) {
            glEnable(GL_DEPTH_TEST)
        } else {
            glEnable(GL_DEPTH_TEST)
        }
    }

    fun beginBuffer(mode: Int, vertexFormats: VertexFormat = DefaultVertexFormats.POSITION_COLOR) {
        buffer.begin(mode, vertexFormats)
    }

    fun endBuffer() {
        draw()
    }
}