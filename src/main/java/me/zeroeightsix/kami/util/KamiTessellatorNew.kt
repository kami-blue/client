package me.zeroeightsix.kami.util

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
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
 * Updated by dominikaaaa on 18/02/20
 * Updated by on Afel 08/06/20
 * Updated by Xiaro on 30/07/20
 */
object KamiTessellatorNew : Tessellator(0x200000) {
    private val mc = Minecraft.getMinecraft()

    @JvmStatic
    fun prepareGL() {
        GlStateManager.pushMatrix()
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL32.GL_DEPTH_CLAMP)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        GlStateManager.disableAlpha()
        GlStateManager.shadeModel(GL_SMOOTH)
        GlStateManager.disableCull()
        GlStateManager.enableBlend()
        GlStateManager.depthMask(true)
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
    }

    @JvmStatic
    fun begin(mode: Int) {
        buffer.begin(mode, DefaultVertexFormats.POSITION_COLOR)
    }

    @JvmStatic
    fun releaseGL() {
        GlStateManager.enableLighting()
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
        GlStateManager.popMatrix()
    }

    @JvmStatic
    fun render() {
        draw()
    }

    @JvmStatic
    private fun pTicks(): Float {
        return mc.renderPartialTicks
    }

    @JvmStatic
    fun drawBox(box: AxisAlignedBB, colour: ColourHolder, a: Int, sides: Int) {
        if (sides and GeometryMasks.Quad.DOWN != 0) {
            buffer.pos(box.minX, box.minY, box.minZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.minX, box.minY, box.maxZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.maxX, box.minY, box.maxZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.maxX, box.minY, box.minZ).color(colour.r, colour.g, colour.b, a).endVertex()
        }
        if (sides and GeometryMasks.Quad.UP != 0) {
            buffer.pos(box.minX, box.maxY, box.minZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.minX, box.maxY, box.maxZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.maxX, box.maxY, box.maxZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.maxX, box.maxY, box.minZ).color(colour.r, colour.g, colour.b, a).endVertex()
        }
        if (sides and GeometryMasks.Quad.NORTH != 0) {
            buffer.pos(box.minX, box.minY, box.minZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.minX, box.maxY, box.minZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.maxX, box.maxY, box.minZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.maxX, box.minY, box.minZ).color(colour.r, colour.g, colour.b, a).endVertex()
        }
        if (sides and GeometryMasks.Quad.SOUTH != 0) {
            buffer.pos(box.minX, box.minY, box.maxZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.minX, box.maxY, box.maxZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.maxX, box.maxY, box.maxZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.maxX, box.minY, box.maxZ).color(colour.r, colour.g, colour.b, a).endVertex()
        }
        if (sides and GeometryMasks.Quad.WEST != 0) {
            buffer.pos(box.minX, box.minY, box.minZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.minX, box.minY, box.maxZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.minX, box.maxY, box.maxZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.minX, box.maxY, box.minZ).color(colour.r, colour.g, colour.b, a).endVertex()
        }
        if (sides and GeometryMasks.Quad.EAST != 0) {
            buffer.pos(box.maxX, box.minY, box.minZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.maxX, box.minY, box.maxZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.maxX, box.maxY, box.maxZ).color(colour.r, colour.g, colour.b, a).endVertex()
            buffer.pos(box.maxX, box.maxY, box.minZ).color(colour.r, colour.g, colour.b, a).endVertex()
        }
    }

    @JvmStatic
    fun drawLineTo(pos: Vec3d, colour: ColourHolder, a: Int, thickness: Float) {
        val eyePos = mc.player.getLook(pTicks())
        GlStateManager.glLineWidth(thickness)
        buffer.pos(eyePos.x, eyePos.y + mc.player.getEyeHeight(), eyePos.z).color(colour.r, colour.g, colour.b, a).endVertex()
        buffer.pos(pos.x, pos.y, pos.z).color(colour.r, colour.g, colour.b, a).endVertex()
    }

    /**
     * @author Xiaro
     *
     *
     * Draws outline of given axis aligned bounding box
     */
    @JvmStatic
    fun drawOutline(boxIn: AxisAlignedBB, colour: ColourHolder, a: Int, thickness: Float) {
        val xArray = arrayOf(boxIn.minX, boxIn.maxX)
        val yArray = arrayOf(boxIn.minY, boxIn.maxY)
        val zArray = arrayOf(boxIn.minZ, boxIn.maxZ)
        GlStateManager.glLineWidth(thickness)

        for (x in xArray) for (y in yArray) for (z in zArray) {
            buffer.pos(x, y, z).color(colour.r, colour.g, colour.b, a).endVertex()
        }
        for (x in xArray) for (z in zArray) for (y in yArray) {
            buffer.pos(x, y, z).color(colour.r, colour.g, colour.b, a).endVertex()
        }
        for (y in yArray) for (z in zArray) for (x in xArray) {
            buffer.pos(x, y, z).color(colour.r, colour.g, colour.b, a).endVertex()
        }
    }
}