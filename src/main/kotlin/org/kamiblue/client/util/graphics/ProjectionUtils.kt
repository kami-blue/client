package org.kamiblue.client.util.graphics

import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.ActiveRenderInfo
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.util.math.Vec3d
import org.kamiblue.client.util.Wrapper
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.util.vector.Vector4f

/**
 * This file is adapted from forgehax/util/math/VectorUtils.java, which is licensed under MIT.
 *
 * You can find a copy of the original license here:
 * https://github.com/fr1kin/ForgeHax/blob/2011740/src/main/java/com/matt/forgehax/util/math/VectorUtils.java
 *
 * @author Seth
 * 4/16/2019 @ 3:28 AM.
 *
 * Adapted by Xiaro on 17/08/20
 */
object ProjectionUtils {
    private val mc = Wrapper.minecraft
    private val modelMatrix = Matrix4f()
    private val projectionMatrix = Matrix4f()
    private var resolution = ScaledResolution(mc)
    var camPos = Vec3d(0.0, 0.0, 0.0)

    fun updateMatrix() {
        if (mc.renderViewEntity == null) return
        val viewerPos = ActiveRenderInfo.projectViewFromEntity(mc.renderViewEntity!!, KamiTessellator.pTicks().toDouble())
        val relativeCamPos = ActiveRenderInfo.getCameraPosition()

        loadMatrix(modelMatrix, GL_MODELVIEW_MATRIX)
        loadMatrix(projectionMatrix, GL_PROJECTION_MATRIX)
        camPos = viewerPos.add(relativeCamPos)
        resolution = ScaledResolution(mc)
    }

    private fun loadMatrix(matrix: Matrix4f, glBit: Int) {
        val floatBuffer = GLAllocation.createDirectFloatBuffer(16)
        glGetFloat(glBit, floatBuffer)
        matrix.load(floatBuffer)
    }

    fun toScaledScreenPos(posIn: Vec3d): Vec3d {
        val vector4f = getTransformedMatrix(posIn)

        val scaledResolution = ScaledResolution(mc)
        val width = scaledResolution.scaledWidth
        val height = scaledResolution.scaledHeight

        vector4f.x = width / 2f + (0.5f * vector4f.x * width + 0.5f)
        vector4f.y = height / 2f - (0.5f * vector4f.y * height + 0.5f)
        val posZ = if (isVisible(vector4f, width, height)) 0.0 else -1.0

        return Vec3d(vector4f.x.toDouble(), vector4f.y.toDouble(), posZ)
    }

    fun toScreenPos(posIn: Vec3d): Vec3d {
        val vector4f = getTransformedMatrix(posIn)

        val width = mc.displayWidth
        val height = mc.displayHeight

        vector4f.x = width / 2f + (0.5f * vector4f.x * width + 0.5f)
        vector4f.y = height / 2f - (0.5f * vector4f.y * height + 0.5f)
        val posZ = if (isVisible(vector4f, width, height)) 0.0 else -1.0

        return Vec3d(vector4f.x.toDouble(), vector4f.y.toDouble(), posZ)
    }

    private fun getTransformedMatrix(posIn: Vec3d): Vector4f {
        val relativePos = camPos.subtract(posIn)
        val vector4f = Vector4f(relativePos.x.toFloat(), relativePos.y.toFloat(), relativePos.z.toFloat(), 1f)

        transform(vector4f, modelMatrix)
        transform(vector4f, projectionMatrix)

        if (vector4f.w > 0.0f) {
            vector4f.x *= -100000
            vector4f.y *= -100000
        } else {
            val invert = 1f / vector4f.w
            vector4f.x *= invert
            vector4f.y *= invert
        }

        return vector4f
    }

    private fun transform(vec: Vector4f, matrix: Matrix4f) {
        val x = vec.x
        val y = vec.y
        val z = vec.z
        vec.x = x * matrix.m00 + y * matrix.m10 + z * matrix.m20 + matrix.m30
        vec.y = x * matrix.m01 + y * matrix.m11 + z * matrix.m21 + matrix.m31
        vec.z = x * matrix.m02 + y * matrix.m12 + z * matrix.m22 + matrix.m32
        vec.w = x * matrix.m03 + y * matrix.m13 + z * matrix.m23 + matrix.m33
    }

    private fun isVisible(pos: Vector4f, width: Int, height: Int): Boolean {
        return pos.x in 0.0..width.toDouble() && pos.y in 0.0..height.toDouble()
    }
}