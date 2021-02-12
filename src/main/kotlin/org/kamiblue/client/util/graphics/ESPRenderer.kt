package org.kamiblue.client.util.graphics

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import org.kamiblue.client.util.EntityUtils
import org.kamiblue.client.util.EntityUtils.getInterpolatedAmount
import org.kamiblue.client.util.Wrapper
import org.kamiblue.client.util.color.ColorHolder
import org.lwjgl.opengl.GL11.GL_LINES
import org.lwjgl.opengl.GL11.GL_QUADS

/**
 * @author Xiaro
 *
 * Created by Xiaro on 30/07/20
 */
class ESPRenderer {
    private val frustumCamera: ICamera = Frustum()
    private var toRender: MutableList<Triple<AxisAlignedBB, ColorHolder, Int>> = ArrayList()

    var aFilled = 0
    var aOutline = 0
    var aTracer = 0
    var thickness = 2f
    var through = true
    var tracerOffset = 50
    var fullOutline = false

    val size: Int
        get() = toRender.size

    fun add(entity: Entity, color: ColorHolder) {
        add(entity, color, GeometryMasks.Quad.ALL)
    }

    fun add(entity: Entity, color: ColorHolder, sides: Int) {
        val interpolatedBox = entity.renderBoundingBox.offset(getInterpolatedAmount(entity, KamiTessellator.pTicks()))
        add(interpolatedBox, color, sides)
    }

    fun add(pos: BlockPos, color: ColorHolder) {
        add(pos, color, GeometryMasks.Quad.ALL)
    }

    fun add(pos: BlockPos, color: ColorHolder, sides: Int) {
        add(AxisAlignedBB(pos), color, sides)
    }

    fun add(box: AxisAlignedBB, color: ColorHolder) {
        add(box, color, GeometryMasks.Quad.ALL)
    }

    fun add(box: AxisAlignedBB, color: ColorHolder, sides: Int) {
        add(Triple(box, color, sides))
    }

    fun add(triple: Triple<AxisAlignedBB, ColorHolder, Int>) {
        toRender.add(triple)
    }

    fun replaceAll(list: MutableList<Triple<AxisAlignedBB, ColorHolder, Int>>) {
        toRender = list
    }

    fun clear() {
        toRender.clear()
    }

    fun render(clear: Boolean, cull: Boolean = true) {
        if (toRender.isEmpty() && (aFilled == 0 && aOutline == 0 && aTracer == 0)) return

        val entity = Wrapper.minecraft.renderViewEntity ?: Wrapper.player ?: return
        val interpolatedPos = EntityUtils.getInterpolatedPos(entity, KamiTessellator.pTicks())
        frustumCamera.setPosition(interpolatedPos.x, interpolatedPos.y, interpolatedPos.z)

        if (through) GlStateManager.disableDepth()

        if (aFilled != 0) drawList(Type.FILLED, cull)
        if (aOutline != 0) drawList(Type.OUTLINE, cull)
        if (aTracer != 0) drawList(Type.TRACER, cull)

        if (clear) clear()
        GlStateManager.enableDepth()
    }

    private fun drawList(type: Type, cull: Boolean = false) {
        KamiTessellator.begin(if (type == Type.FILLED) GL_QUADS else GL_LINES)

        for ((box, color, sides) in toRender) when (type) {
            Type.FILLED -> drawFilled(cull, box, color, sides)
            Type.OUTLINE -> drawOutline(cull, box, color, sides)
            Type.TRACER -> drawTracer(box, color)
        }

        KamiTessellator.render()
    }

    private fun drawFilled(cull: Boolean, box: AxisAlignedBB, color: ColorHolder, sides: Int) {
        val a = (aFilled * (color.a / 255f)).toInt()

        if (!cull || frustumCamera.isBoundingBoxInFrustum(box)) {
            KamiTessellator.drawBox(box, color, a, sides)
        }
    }

    private fun drawOutline(cull: Boolean, box: AxisAlignedBB, color: ColorHolder, sides: Int) {
        val a = (aOutline * (color.a / 255f)).toInt()
        val side = if (fullOutline) GeometryMasks.Quad.ALL else sides

        if (!cull || frustumCamera.isBoundingBoxInFrustum(box)) {
            KamiTessellator.drawOutline(box, color, a, side, thickness)
        }
    }

    private fun drawTracer(box: AxisAlignedBB, color: ColorHolder) {
        val a = (aTracer * (color.a / 255f)).toInt()
        val offset = (tracerOffset - 50) / 100.0 * (box.maxY - box.minY)
        val offsetBox = box.center.add(0.0, offset, 0.0)

        KamiTessellator.drawLineTo(offsetBox, color, a, thickness)
    }

    private enum class Type {
        FILLED, OUTLINE, TRACER
    }

}