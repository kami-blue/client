package org.kamiblue.client.util.graphics

import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.Vec3d
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.math.Vec2d
import org.lwjgl.opengl.GL11.*

class VertexHelper(private val useVbo: Boolean) {
    private val tessellator = Tessellator.getInstance()
    private val buffer = tessellator.buffer

    fun begin(mode: Int) {
        if (useVbo) {
            buffer.begin(mode, DefaultVertexFormats.POSITION_COLOR)
        } else {
            glBegin(mode)
        }
    }

    fun put(pos: Vec3d, color: ColorHolder) {
        put(pos.x, pos.y, pos.z, color)
    }

    fun put(x: Double, y: Double, z: Double, color: ColorHolder) {
        if (useVbo) {
            buffer.pos(x, y, z).color(color.r, color.g, color.b, color.a).endVertex()
        } else {
            color.setGLColor()
            glVertex3d(x, y, z)
        }
    }

    fun put(pos: Vec2d, color: ColorHolder) {
        put(pos.x, pos.y, color)
    }

    fun put(x: Double, y: Double, color: ColorHolder) {
        if (useVbo) {
            buffer.pos(x, y, 0.0).color(color.r, color.g, color.b, color.a).endVertex()
        } else {
            color.setGLColor()
            glVertex2d(x, y)
        }
    }

    fun end() {
        if (useVbo) {
            tessellator.draw()
        } else {
            glEnd()
        }
    }
}