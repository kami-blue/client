package me.zeroeightsix.kami.util.graphics.render

import me.zeroeightsix.kami.util.graphics.buffer.VertexBuffer
import me.zeroeightsix.kami.util.graphics.buffer.ShortIndexBuffer
import me.zeroeightsix.kami.util.graphics.buffer.newBufferGroup
import me.zeroeightsix.kami.util.math.corners
import net.minecraft.util.math.AxisAlignedBB
import org.kamiblue.commons.tuples.operations.VEC3D_ZERO
import org.kamiblue.commons.tuples.operations.Vec3d
import org.kamiblue.commons.tuples.operations.Vec4i
import org.kamiblue.commons.tuples.operations.toFloat
import org.kamiblue.commons.tuples.x
import org.kamiblue.commons.tuples.y
import org.kamiblue.commons.tuples.z
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW

class BoxRenderer(
    private val capacity: Int,
    block: VertexBuffer.Builder.() -> Unit
) {

    private val vertexBuffer = newBufferGroup(GL_DYNAMIC_DRAW, capacity * 16, block)
    private val indexBuffer = ShortIndexBuffer(GL_DYNAMIC_DRAW, capacity * 60)

    private val filledIndices = ArrayList<Short>()
    private val outlineIndices = ArrayList<Short>()

    private var translate: Vec3d = VEC3D_ZERO
    private var boxVertexCount = 0
    private var boxSize = 0

    private var renderTranslate: Vec3d = VEC3D_ZERO
    var renderBoxSize = 0; private set
    private var renderFilledSize = 0
    private var renderOutlineSize = 0

    fun begin(translate: Vec3d = VEC3D_ZERO) {
        this.translate = translate
    }

    fun put(box: AxisAlignedBB, block: Builder.() -> Unit) {
        if (boxVertexCount >= capacity) return
        Builder(box).apply(block)
        boxSize++
    }

    fun end(): UploadInfo {
        boxVertexCount = capacity

        val boxSizeCache = boxSize
        boxSize = 0

        val translateCache = translate
        translate = VEC3D_ZERO

        val filledSizeCache = filledIndices.size
        filledIndices.forEach(indexBuffer::put)
        filledIndices.clear()

        val outlineSizeCache = outlineIndices.size
        outlineIndices.forEach(indexBuffer::put)
        outlineIndices.clear()

        return UploadInfo(translateCache, boxSizeCache, filledSizeCache, outlineSizeCache)
    }

    fun upload(uploadInfo: UploadInfo) {
        vertexBuffer.upload()
        indexBuffer.upload()

        renderTranslate = uploadInfo.translate
        renderBoxSize = uploadInfo.boxSize
        renderFilledSize = uploadInfo.filledSize
        renderOutlineSize = uploadInfo.outlineSize

        boxVertexCount = 0
    }

    fun render(translate: Vec3d = VEC3D_ZERO) {
        glPushMatrix()
        glTranslated(renderTranslate.x - translate.x, renderTranslate.y - translate.y, renderTranslate.z - translate.z)

        vertexBuffer.render(indexBuffer, GL_TRIANGLES, 0, renderFilledSize)
        vertexBuffer.render(indexBuffer, GL_LINES, renderFilledSize, renderOutlineSize)

        glPopMatrix()
    }

    class UploadInfo(
        val translate: Vec3d,
        val boxSize: Int,
        val filledSize: Int,
        val outlineSize: Int
    )

    inner class Builder(private val box: AxisAlignedBB) {
        fun filled(color: Vec4i? = null) {
            putVertices(color)

            quadsIndices.forEach {
                filledIndices.add((it + boxVertexCount * 8).toShort())
            }

            boxVertexCount++
        }

        fun outline(color: Vec4i? = null) {
            putVertices(color)

            linesIndices.forEach {
                outlineIndices.add((it + boxVertexCount * 8).toShort())
            }

            boxVertexCount++
        }

        private fun putVertices(color: Vec4i?) {
            vertexBuffer.apply {
                box.corners().forEach {
                    put {
                        pos(Vec3d(it.x - translate.x, it.y - translate.y, it.z - translate.z).toFloat())
                        if (color != null) color(color)
                    }
                }
            }
        }
    }

    private companion object {
        // Size 36
        val quadsIndices = byteArrayOf(
            // -Y
            0, 1, 5, 0, 5, 4,
            // +Y
            3, 2, 6, 3, 6, 7,
            // -X
            3, 1, 0, 3, 0, 2,
            // +X
            6, 4, 5, 6, 5, 7,
            // -Z
            2, 0, 4, 2, 4, 6,
            // +Z
            7, 5, 1, 7, 1, 3,
        )

        // Size 24
        val linesIndices = byteArrayOf(
            // Bottom rect
            0, 1,
            1, 5,
            5, 4,
            4, 0,
            // Top rect
            2, 3,
            3, 7,
            7, 6,
            6, 2,
            // Vertical lines
            0, 2,
            1, 3,
            4, 6,
            5, 7,
        )
    }
}