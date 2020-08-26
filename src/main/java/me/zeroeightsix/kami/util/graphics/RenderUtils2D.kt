package me.zeroeightsix.kami.util.graphics

import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.GlStateUtils.blend
import me.zeroeightsix.kami.util.graphics.GlStateUtils.cull
import me.zeroeightsix.kami.util.graphics.GlStateUtils.depth
import me.zeroeightsix.kami.util.graphics.GlStateUtils.lineSmooth
import me.zeroeightsix.kami.util.graphics.GlStateUtils.smooth
import me.zeroeightsix.kami.util.math.Vec2d
import org.lwjgl.opengl.GL11.*
import kotlin.math.*

/**
 * @author Xiaro
 *
 * Created by Xiaro on 25/08/20
 */
object RenderUtils2D {

    @JvmStatic
    @JvmOverloads
    fun drawRoundedRectOutline(vertexHelper: VertexHelper, posBegin: Vec2d = Vec2d(0.0, 0.0), posEnd: Vec2d, radius: Double, lineWidth: Float, color: ColorHolder) {
        val pos2 = Vec2d(posEnd.x, posBegin.y) // Top right
        val pos4 = Vec2d(posBegin.x, posEnd.y) // Bottom left

        drawArcOutline(vertexHelper, posBegin.add(radius), radius, Pair(270f, 0f), color = color) // Top left
        drawArcOutline(vertexHelper, pos2.add(-radius, radius), radius, Pair(0f, 90f), color = color) // Top right
        drawArcOutline(vertexHelper, posEnd.subtract(radius), radius, Pair(90f, 180f), color = color) // Bottom right
        drawArcOutline(vertexHelper, pos4.add(radius, -radius), radius, Pair(180f, 270f), color = color) // Bottom left

        drawLine(vertexHelper, posBegin.add(radius, 0.0), pos2.subtract(radius, 0.0), lineWidth, color) // Top
        drawLine(vertexHelper, posBegin.add(0.0, radius), pos4.subtract(0.0, radius), lineWidth, color) // Left
        drawLine(vertexHelper, pos2.add(0.0, radius), posEnd.subtract(0.0, radius), lineWidth, color) // Right
        drawLine(vertexHelper, pos4.add(radius, 0.0), posEnd.subtract(radius, 0.0), lineWidth, color) // Bottom
    }

    @JvmStatic
    @JvmOverloads
    fun drawRoundedRectFilled(vertexHelper: VertexHelper, posBegin: Vec2d = Vec2d(0.0, 0.0), posEnd: Vec2d, radius: Double, color: ColorHolder) {
        val pos2 = Vec2d(posEnd.x, posBegin.y) // Top right
        val pos4 = Vec2d(posBegin.x, posEnd.y) // Bottom left

        drawArcFilled(vertexHelper, posBegin.add(radius), radius, Pair(270f, 0f), color = color) // Top left
        drawArcFilled(vertexHelper, pos2.add(-radius, radius), radius, Pair(0f, 90f), color = color) // Top right
        drawArcFilled(vertexHelper, posEnd.subtract(radius), radius, Pair(90f, 180f), color = color) // Bottom right
        drawArcFilled(vertexHelper, pos4.add(radius, -radius), radius, Pair(180f, 270f), color = color) // Bottom left

        drawRectFilled(vertexHelper, posBegin.add(radius), posEnd.subtract(radius), color) // Center
        drawRectFilled(vertexHelper, posBegin.add(radius, 0.0), pos2.add(-radius, radius), color) // Top
        drawRectFilled(vertexHelper, posBegin.add(0.0, radius), pos4.add(radius), color) // Left
        drawRectFilled(vertexHelper, pos2.subtract(radius), posEnd.subtract(0.0, radius), color) // Right
        drawRectFilled(vertexHelper, pos4.add(radius), posEnd.subtract(radius, 0.0), color) // Bottom
    }

    @JvmStatic
    @JvmOverloads
    fun drawCircleOutline(vertexHelper: VertexHelper, center: Vec2d = Vec2d(0.0, 0.0), radius: Double, segments: Int = 0, lineWidth: Float = 1f, color: ColorHolder) {
        drawArcOutline(vertexHelper, center, radius, Pair(0f, 360f), segments, lineWidth, color)
    }

    @JvmStatic
    @JvmOverloads
    fun drawCircleFilled(vertexHelper: VertexHelper, center: Vec2d = Vec2d(0.0, 0.0), radius: Double, segments: Int = 0, color: ColorHolder) {
        drawArcFilled(vertexHelper, center, radius, Pair(0f, 360f), segments, color)
    }

    @JvmStatic
    @JvmOverloads
    fun drawArcOutline(vertexHelper: VertexHelper, center: Vec2d = Vec2d(0.0, 0.0), radius: Double, angleRange: Pair<Float, Float>, segments: Int = 0, lineWidth: Float = 1f, color: ColorHolder) {
        val arcVertices = getArcVertices(center, radius, angleRange, segments)
        drawLineLoop(vertexHelper, arcVertices, lineWidth, color)
    }

    @JvmStatic
    @JvmOverloads
    fun drawArcFilled(vertexHelper: VertexHelper, center: Vec2d = Vec2d(0.0, 0.0), radius: Double, angleRange: Pair<Float, Float>, segments: Int = 0, color: ColorHolder) {
        val arcVertices = getArcVertices(center, radius, angleRange, segments)
        drawTriangleFan(vertexHelper, center, arcVertices, color)
    }

    @JvmStatic
    @JvmOverloads
    fun drawRectOutline(vertexHelper: VertexHelper, posBegin: Vec2d = Vec2d(0.0, 0.0), posEnd: Vec2d, lineWidth: Float = 1f, color: ColorHolder) {
        val pos2 = Vec2d(posEnd.x, posBegin.y) // Top right
        val pos4 = Vec2d(posBegin.x, posEnd.y) // Bottom left
        val vertices = arrayOf(posBegin, pos2, posEnd, pos4)
        drawLineLoop(vertexHelper, vertices, lineWidth, color)
    }

    @JvmStatic
    @JvmOverloads
    fun drawRectFilled(vertexHelper: VertexHelper, posBegin: Vec2d = Vec2d(0.0, 0.0), posEnd: Vec2d, color: ColorHolder) {
        val pos2 = Vec2d(posEnd.x, posBegin.y) // Top right
        val pos4 = Vec2d(posBegin.x, posEnd.y) // Bottom left
        drawQuad(vertexHelper, posBegin, pos2, posEnd, pos4, color)
    }

    @JvmStatic
    fun drawQuad(vertexHelper: VertexHelper, pos1: Vec2d, pos2: Vec2d, pos3: Vec2d, pos4: Vec2d, color: ColorHolder) {
        val vertices = arrayOf(pos1, pos2, pos4, pos3)
        drawTriangleStrip(vertexHelper, vertices, color)
    }

    @JvmStatic
    @JvmOverloads
    fun drawTriangleOutline(vertexHelper: VertexHelper, pos1: Vec2d, pos2: Vec2d, pos3: Vec2d, lineWidth: Float = 1f, color: ColorHolder) {
        val vertices = arrayOf(pos1, pos2, pos3)
        drawLineLoop(vertexHelper, vertices, lineWidth, color)
    }

    @JvmStatic
    fun drawTriangleFilled(vertexHelper: VertexHelper, pos1: Vec2d, pos2: Vec2d, pos3: Vec2d, color: ColorHolder) {
        prepareGl()

        vertexHelper.begin(GL_TRIANGLES)
        vertexHelper.put(pos1, color)
        vertexHelper.put(pos2, color)
        vertexHelper.put(pos3, color)
        vertexHelper.end()

        releaseGl()
    }

    @JvmStatic
    fun drawTriangleFan(vertexHelper: VertexHelper, center: Vec2d, vertices: Array<Vec2d>, color: ColorHolder) {
        prepareGl()

        vertexHelper.begin(GL_TRIANGLE_FAN)
        vertexHelper.put(center, color)
        for (vertex in vertices) {
            vertexHelper.put(vertex, color)
        }
        vertexHelper.end()

        releaseGl()
    }

    @JvmStatic
    fun drawTriangleStrip(vertexHelper: VertexHelper, vertices: Array<Vec2d>, color: ColorHolder) {
        prepareGl()

        vertexHelper.begin(GL_TRIANGLE_STRIP)
        for (vertex in vertices) {
            vertexHelper.put(vertex, color)
        }
        vertexHelper.end()

        releaseGl()
    }

    @JvmStatic
    @JvmOverloads
    fun drawLineLoop(vertexHelper: VertexHelper, vertices: Array<Vec2d>, lineWidth: Float = 1f, color: ColorHolder) {
        prepareGl()
        glLineWidth(lineWidth)

        vertexHelper.begin(GL_LINE_LOOP)
        for (vertex in vertices) {
            vertexHelper.put(vertex, color)
        }
        vertexHelper.end()


        releaseGl()
        glLineWidth(1f)
    }

    @JvmStatic
    @JvmOverloads
    fun drawLine(vertexHelper: VertexHelper, posBegin: Vec2d, posEnd: Vec2d, lineWidth: Float = 1f, color: ColorHolder) {
        prepareGl()
        glLineWidth(lineWidth)

        vertexHelper.begin(GL_LINES)
        vertexHelper.put(posBegin, color)
        vertexHelper.put(posEnd, color)
        vertexHelper.end()

        releaseGl()
        glLineWidth(1f)
    }

    @JvmStatic
    private fun getArcVertices(center: Vec2d, radius: Double, angleRange: Pair<Float, Float>, segments: Int): Array<Vec2d> {
        val angleBegin = min(angleRange.first, angleRange.second).toDouble()
        val angleEnd = max(angleRange.first, angleRange.second).toDouble()
        val range = angleEnd - angleBegin
        val seg = calcSegments(segments, radius, range)

        return Array(seg + 1) { index ->
            val angle = Math.toRadians(index * (range / seg.toDouble()) + angleBegin)
            Vec2d(sin(angle), -cos(angle)).multiply(radius).add(center)
        }
    }

    @JvmStatic
    private fun calcSegments(segmentsIn: Int, radius: Double, range: Double): Int {
        if (segmentsIn != -0) return segmentsIn
        val segments = radius * 0.5 * PI * (range / 360.0)
        return max(segments.roundToInt(), 16)
    }

    private fun prepareGl() {
        blend(true)
        smooth(true)
        lineSmooth(true)
        depth(false)
        cull(false)
    }

    private fun releaseGl() {
        blend(false)
        smooth(false)
        lineSmooth(false)
        depth(true)
        cull(true)
    }

}