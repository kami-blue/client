package me.zeroeightsix.kami.util.graphics

import me.zeroeightsix.kami.util.colourUtils.ColourHolder
import me.zeroeightsix.kami.util.graphics.GlStateUtils.beginBuffer
import me.zeroeightsix.kami.util.graphics.GlStateUtils.endBuffer
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.*

class VertexHelper(private val useVbo: Boolean) {

    private val buffer = GlStateUtils.buffer

    fun begin(mode: Int) {
        if (useVbo) {
            beginBuffer(mode, DefaultVertexFormats.POSITION_COLOR)
        } else {
            glBegin(mode)
        }
    }

    fun put(pos: Vec3d, color: ColourHolder) {
        put(pos.x, pos.y, pos.z, color)
    }

    fun put(x: Double, y: Double, z: Double, color: ColourHolder) {
        if (useVbo) {
            buffer.pos(x, y, z).color(color.r, color.g, color.b, color.a).endVertex()
        } else {
            color.setGLColour()
            glVertex3d(x, y, z)
        }
    }

    fun put(pos: Vec2d, color: ColourHolder) {
        put(pos.x, pos.y, color)
    }

    fun put(x: Double, y: Double, color: ColourHolder) {
        if (useVbo) {
            buffer.pos(x, y, 0.0).color(color.r, color.g, color.b, color.a).endVertex()
        } else {
            color.setGLColour()
            glVertex2d(x, y)
        }
    }

    fun end() {
        if (useVbo) {
            endBuffer()
        } else {
            glEnd()
        }
    }
}