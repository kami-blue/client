package me.zeroeightsix.kami.util

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL32
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


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
 * Updated by Xiaro on 01/08/20
 */
object KamiTessellator : Tessellator(0x200000) {
    private val mc = Minecraft.getMinecraft()

    /**
     * Setup Gl states
     */
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
        GlStateManager.depthMask(false)
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
    }

    /**
     * Reverts Gl states
     */
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

    /**
     * Begins VBO buffer with [mode]
     */
    @JvmStatic
    fun begin(mode: Int) {
        buffer.begin(mode, DefaultVertexFormats.POSITION_COLOR)
    }

    /**
     * Draws vertexes in the buffer
     */
    @JvmStatic
    fun render() {
        draw()
    }

    /**
     * @author Xiaro
     *
     * @return Render Parital Ticks
     */
    @JvmStatic
    private fun pTicks(): Float {
        return mc.renderPartialTicks
    }

    /**
     * @author Xiaro
     *
     * Draws rectangles around [sides] of [box]
     *
     * @param box Box to be drawn rectangles around
     * @param colour RGB
     * @param a Alpha
     * @param sides Sides to be drawn
     */
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

    /**
     * @author Xiaro
     *
     * Draws a line from player crosshair to [position]
     *
     * @param position Position to be drawn line to
     * @param colour RGB
     * @param a Alpha
     * @param thickness Thickness of the line
     */
    @JvmStatic
    fun drawLineTo(position: Vec3d, colour: ColourHolder, a: Int, thickness: Float) {
        var eyePos = mc.player.getLook(pTicks()).add(mc.renderManager.viewerPosX, mc.renderManager.viewerPosY, mc.renderManager.viewerPosZ)
        if (mc.gameSettings.viewBobbing) { /* This why bobbing is so annoying */
            val yawRad = Math.toRadians(mc.player.rotationYaw.toDouble())
            val pitchRad = Math.toRadians(mc.player.rotationPitch.toDouble())
            val distance = -(mc.player.distanceWalkedModified + (mc.player.distanceWalkedModified - mc.player.prevDistanceWalkedModified) * pTicks().toDouble())
            val cameraYaw = mc.player.prevCameraYaw + (mc.player.cameraYaw - mc.player.prevCameraYaw) * pTicks().toDouble()
            val cameraPitch = mc.player.prevCameraPitch + (mc.player.cameraPitch - mc.player.prevCameraPitch) * pTicks().toDouble()
            val xOffsetScreen = sin(distance * PI) * cameraYaw * 0.5
            val yOffsetScreen = (((abs(cos(distance * PI - 0.2) * cameraYaw) * 5.0) + cameraPitch) * PI / 180.0) - abs(cos(distance * PI) * cameraYaw)
            val xOffset = (-cos(yawRad) * xOffsetScreen) + (-sin(yawRad) * sin(pitchRad) * yOffsetScreen)
            val yOffset = cos(pitchRad) * yOffsetScreen
            val zOffset = (-sin(yawRad) * xOffsetScreen) + (cos(yawRad) * sin(pitchRad) * yOffsetScreen)
            eyePos = eyePos.subtract(xOffset, yOffset, zOffset)
        }
        GlStateManager.glLineWidth(thickness)
        buffer.pos(eyePos.x, eyePos.y + mc.player.getEyeHeight(), eyePos.z).color(colour.r, colour.g, colour.b, a).endVertex()
        buffer.pos(position.x, position.y, position.z).color(colour.r, colour.g, colour.b, a).endVertex()
    }

    /**
     * @author Xiaro
     *
     * Draws outline of [box]
     *
     * @param box Box to be drawn outline
     * @param colour RGB
     * @param a Alpha
     * @param thickness Thickness of the outline
     */
    @JvmStatic
    fun drawOutline(box: AxisAlignedBB, colour: ColourHolder, a: Int, thickness: Float) {
        val xArray = arrayOf(box.minX, box.maxX)
        val yArray = arrayOf(box.minY, box.maxY)
        val zArray = arrayOf(box.minZ, box.maxZ)
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