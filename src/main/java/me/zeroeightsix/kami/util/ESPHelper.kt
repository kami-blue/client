package me.zeroeightsix.kami.util

import net.minecraft.client.Minecraft
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
        val pTicks = mc.renderPartialTicks
        val xOffset = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * pTicks - entity.posX
        val yOffset = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * pTicks - entity.posY
        val zOffset = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * pTicks - entity.posZ
        val box = entity.boundingBox.offset(xOffset, yOffset, zOffset)
        drawESPBox(box, filled, outline, tracer, r, g, b, aFilled, aOutline, aTracer, side, thickness)
    }
    /* End of ESP for entity */

    /* ESP for BlockPos */
    fun drawESPBlock(pos: BlockPos, filled: Boolean, outline: Boolean, tracer: Boolean, colour: Int, aFilled: Int, aOutline: Int, aTracer: Int, thickness: Float) {
        drawESPBlock(pos, filled, outline, tracer, colour, aFilled, aOutline, aTracer, GeometryMasks.Quad.ALL, thickness)
    }

    fun drawESPBlock(pos: BlockPos, filled: Boolean, outline: Boolean, tracer: Boolean, colour: Int, aFilled: Int, aOutline: Int, aTracer: Int, side: Int, thickness: Float) {
        val r = (colour shr 16 and 0xFF)
        val g = (colour shr 8 and 0xFF)
        val b = (colour and 0xFF)
        drawESPBlock(pos, filled, outline, tracer, r, g, b, aFilled, aOutline, aTracer, side, thickness)
    }

    fun drawESPBlock(pos: BlockPos, filled: Boolean, outline: Boolean, tracer: Boolean, r: Int, g: Int, b: Int, aFilled: Int, aOutline: Int, aTracer: Int, side: Int, thickness: Float) {
        drawESPBox(AxisAlignedBB(pos), filled, outline, tracer, r, g, b, aFilled, aOutline, aTracer, side, thickness)
    }
    /* End of ESP for BlockPos */

    /* ESP for AxisAlignedBB */
    fun drawESPBox(box: AxisAlignedBB, filled: Boolean, outline: Boolean, tracer: Boolean, colour: Int, aFilled: Int, aOutline: Int, aTracer: Int, side: Int, thickness: Float) {
        val r = (colour shr 16 and 0xFF)
        val g = (colour shr 8 and 0xFF)
        val b = (colour and 0xFF)
        drawESPBox(box, filled, outline, tracer, r, g, b, aFilled, aOutline, aTracer, side, thickness)
    }

    fun drawESPBox(box: AxisAlignedBB, filled: Boolean, outline: Boolean, tracer: Boolean, r: Int, g: Int, b: Int, aFilled: Int, aOutline: Int, aTracer: Int, thickness: Float) {
        drawESPBox(box, filled, outline, tracer, r, g, b, aFilled, aOutline, aTracer, GeometryMasks.Quad.ALL, thickness)
    }

    fun drawESPBox(box: AxisAlignedBB, filled: Boolean, outline: Boolean, tracer: Boolean, r: Int, g: Int, b: Int, aFilled: Int, aOutline: Int, aTracer: Int, side: Int, thickness: Float) {
        if (filled) {
            KamiTessellator.prepare(GL11.GL_QUADS)
            KamiTessellator.drawBox(box, r, g, b, aFilled, side)
            KamiTessellator.release()
        }
        if (outline) {
            KamiTessellator.drawBoundingBox(box, r, g, b, aOutline, thickness)
        }
        if (tracer) {
            KamiTessellator.drawLineToPos(box.center, r, g, b, aTracer, thickness)
        }
    }
    /* End of ESP for AxisAlignedBB */
}