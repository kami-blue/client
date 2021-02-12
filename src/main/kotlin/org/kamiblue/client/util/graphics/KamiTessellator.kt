package org.kamiblue.client.util.graphics

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ActiveRenderInfo
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import org.kamiblue.client.mixin.extension.renderPartialTicksPaused
import org.kamiblue.client.util.EntityUtils
import org.kamiblue.client.util.color.ColorHolder
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL32


/**
 * THE FOLLOWING CODE IS LICENSED UNDER MIT, AS PER the fr1kin/forgehax license
 * You can view the original code here:
 *
 *
 * https://github.com/fr1kin/ForgeHax/blob/master/src/main/java/com/matt/forgehax/util/tesselation/GeometryTessellator.java
 *
 *
 * Some is created by 086 on 9/07/2017.
 * Updated by l1ving on 18/02/20
 * Updated by on Afel 08/06/20
 * Updated by Xiaro on 06/08/20
 */
object KamiTessellator : Tessellator(0x200000) {
    private val mc = Minecraft.getMinecraft()

    /**
     * Setup Gl states
     */
    fun prepareGL() {
        GlStateManager.pushMatrix()
        glLineWidth(1f)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL32.GL_DEPTH_CLAMP)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        GlStateManager.disableAlpha()
        GlStateManager.shadeModel(GL_SMOOTH)
        GlStateManager.disableCull()
        GlStateManager.enableBlend()
        GlStateManager.depthMask(false)
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
    }

    /**
     * Reverts Gl states
     */
    fun releaseGL() {
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        GlStateManager.enableCull()
        GlStateManager.shadeModel(GL_FLAT)
        GlStateManager.enableAlpha()
        GlStateManager.depthMask(true)
        glDisable(GL32.GL_DEPTH_CLAMP)
        glDisable(GL_LINE_SMOOTH)
        GlStateManager.color(1f, 1f, 1f)
        glLineWidth(1f)
        GlStateManager.popMatrix()
    }

    /**
     * Begins VBO buffer with [mode]
     */
    fun begin(mode: Int) {
        buffer.begin(mode, DefaultVertexFormats.POSITION_COLOR)
    }

    /**
     * Draws vertexes in the buffer
     */
    fun render() {
        draw()
    }

    /**
     * @author Xiaro
     *
     * @return Render Parital Ticks
     */
    @JvmStatic
    fun pTicks(): Float {
        return if (mc.isGamePaused) mc.renderPartialTicksPaused else mc.renderPartialTicks
    }

    val camPos: Vec3d
        get() = EntityUtils.getInterpolatedPos(mc.renderViewEntity
            ?: mc.player, pTicks()).add(ActiveRenderInfo.getCameraPosition())

    /**
     * @author Xiaro
     *
     * Draws rectangles around [sides] of [box]
     *
     * @param box Box to be drawn rectangles around
     * @param color RGB
     * @param a Alpha
     * @param sides Sides to be drawn
     */
    fun drawBox(box: AxisAlignedBB, color: ColorHolder, a: Int, sides: Int) {
        val vertexList = ArrayList<Vec3d>()

        if (sides and GeometryMasks.Quad.DOWN != 0) {
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minZ, box.maxZ, box.minY, EnumFacing.DOWN).toQuad())
        }
        if (sides and GeometryMasks.Quad.UP != 0) {
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minZ, box.maxZ, box.maxY, EnumFacing.UP).toQuad())
        }
        if (sides and GeometryMasks.Quad.NORTH != 0) {
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minY, box.maxY, box.minZ, EnumFacing.NORTH).toQuad())
        }
        if (sides and GeometryMasks.Quad.SOUTH != 0) {
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minY, box.maxY, box.maxZ, EnumFacing.SOUTH).toQuad())
        }
        if (sides and GeometryMasks.Quad.WEST != 0) {
            vertexList.addAll(SquareVec(box.minY, box.maxY, box.minZ, box.maxZ, box.minX, EnumFacing.WEST).toQuad())
        }
        if (sides and GeometryMasks.Quad.EAST != 0) {
            vertexList.addAll(SquareVec(box.minY, box.maxY, box.minZ, box.maxZ, box.maxX, EnumFacing.EAST).toQuad())
        }

        for (pos in vertexList) {
            buffer.pos(pos.x, pos.y, pos.z).color(color.r, color.g, color.b, a).endVertex()
        }
    }

    /**
     * @author Xiaro
     *
     * Draws a line from player crosshair to [position]
     *
     * @param position Position to be drawn line to
     * @param color RGB
     * @param a Alpha
     * @param thickness Thickness of the line
     */
    fun drawLineTo(position: Vec3d, color: ColorHolder, a: Int, thickness: Float) {
        GlStateManager.glLineWidth(thickness)
        buffer.pos(camPos.x, camPos.y, camPos.z).color(color.r, color.g, color.b, a).endVertex()
        buffer.pos(position.x, position.y, position.z).color(color.r, color.g, color.b, a).endVertex()
    }

    /**
     * @author Xiaro
     *
     * Draws outline for [sides] of [box]
     *
     * @param box Box to be drawn outline
     * @param color RGB
     * @param a Alpha
     * @param sides Sides to draw outline
     * @param thickness Thickness of the outline
     */
    fun drawOutline(box: AxisAlignedBB, color: ColorHolder, a: Int, sides: Int, thickness: Float) {
        val vertexList = LinkedHashSet<Pair<Vec3d, Vec3d>>()
        GlStateManager.glLineWidth(thickness)

        if (sides and GeometryMasks.Quad.DOWN != 0) {
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minZ, box.maxZ, box.minY, EnumFacing.DOWN).toLines())
        }
        if (sides and GeometryMasks.Quad.UP != 0) {
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minZ, box.maxZ, box.maxY, EnumFacing.UP).toLines())
        }
        if (sides and GeometryMasks.Quad.NORTH != 0) {
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minY, box.maxY, box.minZ, EnumFacing.NORTH).toLines())
        }
        if (sides and GeometryMasks.Quad.SOUTH != 0) {
            vertexList.addAll(SquareVec(box.minX, box.maxX, box.minY, box.maxY, box.maxZ, EnumFacing.SOUTH).toLines())
        }
        if (sides and GeometryMasks.Quad.WEST != 0) {
            vertexList.addAll(SquareVec(box.minY, box.maxY, box.minZ, box.maxZ, box.minX, EnumFacing.WEST).toLines())
        }
        if (sides and GeometryMasks.Quad.EAST != 0) {
            vertexList.addAll(SquareVec(box.minY, box.maxY, box.minZ, box.maxZ, box.maxX, EnumFacing.EAST).toLines())
        }

        for ((p1, p2) in vertexList) {
            buffer.pos(p1.x, p1.y, p1.z).color(color.r, color.g, color.b, a).endVertex()
            buffer.pos(p2.x, p2.y, p2.z).color(color.r, color.g, color.b, a).endVertex()
        }
    }

    private data class SquareVec(val minX: Double, val maxX: Double, val minZ: Double, val maxZ: Double, val y: Double, val facing: EnumFacing) {
        fun toLines(): Array<Pair<Vec3d, Vec3d>> {
            val quad = this.toQuad()
            return arrayOf(
                Pair(quad[0], quad[1]),
                Pair(quad[1], quad[2]),
                Pair(quad[2], quad[3]),
                Pair(quad[3], quad[0])
            )
        }

        fun toQuad(): Array<Vec3d> {
            return if (this.facing.horizontalIndex != -1) {
                val quad = this.to2DQuad()
                Array(4) { i ->
                    val vec = quad[i]
                    if (facing.axis == EnumFacing.Axis.X) {
                        Vec3d(vec.y, vec.x, vec.z)
                    } else {
                        Vec3d(vec.x, vec.z, vec.y)
                    }
                }
            } else this.to2DQuad()
        }

        fun to2DQuad(): Array<Vec3d> {
            return arrayOf(
                Vec3d(this.minX, this.y, this.minZ),
                Vec3d(this.minX, this.y, this.maxZ),
                Vec3d(this.maxX, this.y, this.maxZ),
                Vec3d(this.maxX, this.y, this.minZ)
            )
        }
    }
}