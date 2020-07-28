package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.util.EntityUtils.getInterpolatedAmount
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import org.lwjgl.opengl.GL11

object ESPHelper {
    val mc: Minecraft = Minecraft.getMinecraft()

    /* ESP for Entity */
    fun drawESPEntity(entity: Entity, filled: Boolean, outline: Boolean, tracer: Boolean, r: Int, g: Int, b: Int, aFilled: Int, aOutline: Int, aTracer: Int, thickness: Float) {
        drawESPEntity(entity, filled, outline, tracer, r, g, b, aFilled, aOutline, aTracer, GeometryMasks.Quad.ALL, thickness)
    }

    fun drawESPEntity(entity: Entity, filled: Boolean, outline: Boolean, tracer: Boolean, r: Int, g: Int, b: Int, aFilled: Int, aOutline: Int, aTracer: Int, side: Int, thickness: Float) {
        val box = entity.boundingBox.offset(getInterpolatedAmount(entity, mc.renderPartialTicks.toDouble()))
        drawESPBox(box, filled, outline, tracer, r, g, b, aFilled, aOutline, aTracer, side, thickness, true)
    }
    /* End of ESP for entity */

    /* ESP for BlockPos */
    fun drawESPBlock(pos: BlockPos, filled: Boolean, outline: Boolean, tracer: Boolean, colour: Int, aFilled: Int, aOutline: Int, aTracer: Int, thickness: Float, through: Boolean) {
        drawESPBlock(pos, filled, outline, tracer, colour, aFilled, aOutline, aTracer, GeometryMasks.Quad.ALL, thickness, through)
    }

    fun drawESPBlock(pos: BlockPos, filled: Boolean, outline: Boolean, tracer: Boolean, colour: Int, aFilled: Int, aOutline: Int, aTracer: Int, side: Int, thickness: Float, through: Boolean) {
        val r = (colour shr 16 and 0xFF)
        val g = (colour shr 8 and 0xFF)
        val b = (colour and 0xFF)
        drawESPBlock(pos, filled, outline, tracer, r, g, b, aFilled, aOutline, aTracer, side, thickness, through)
    }

    fun drawESPBlock(pos: BlockPos, filled: Boolean, outline: Boolean, tracer: Boolean, r: Int, g: Int, b: Int, aFilled: Int, aOutline: Int, aTracer: Int, side: Int, thickness: Float, through: Boolean) {
        drawESPBox(AxisAlignedBB(pos), filled, outline, tracer, r, g, b, aFilled, aOutline, aTracer, side, thickness, through)
    }
    /* End of ESP for BlockPos */

    /* ESP for AxisAlignedBB */
    fun drawESPBox(box: AxisAlignedBB, filled: Boolean, outline: Boolean, tracer: Boolean, colour: Int, aFilled: Int, aOutline: Int, aTracer: Int, side: Int, thickness: Float, through: Boolean) {
        val r = (colour shr 16 and 0xFF)
        val g = (colour shr 8 and 0xFF)
        val b = (colour and 0xFF)
        drawESPBox(box, filled, outline, tracer, r, g, b, aFilled, aOutline, aTracer, side, thickness, through)
    }

    fun drawESPBox(box: AxisAlignedBB, filled: Boolean, outline: Boolean, tracer: Boolean, r: Int, g: Int, b: Int, aFilled: Int, aOutline: Int, aTracer: Int, thickness: Float, through: Boolean) {
        drawESPBox(box, filled, outline, tracer, r, g, b, aFilled, aOutline, aTracer, GeometryMasks.Quad.ALL, thickness, through)
    }

    fun drawESPBox(box: AxisAlignedBB, filled: Boolean, outline: Boolean, tracer: Boolean, r: Int, g: Int, b: Int, aFilled: Int, aOutline: Int, aTracer: Int, side: Int, thickness: Float, through: Boolean) {
        if (filled) {
            KamiTessellator.prepare(GL11.GL_QUADS)
            if (!through) GlStateManager.enableDepth()
            KamiTessellator.drawBox(box, r, g, b, aFilled, side)
            KamiTessellator.release()
        }
        if (outline) {
            KamiTessellator.drawBoundingBox(box, r, g, b, aOutline, thickness, through)
        }
        if (tracer) {
            KamiTessellator.drawLineToPos(box.center, r, g, b, aTracer, thickness)
        }
    }
    /* End of ESP for AxisAlignedBB */
}