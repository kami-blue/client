package me.zeroeightsix.kami.util.graphics.vbo

import me.zeroeightsix.kami.util.graphics.GlCompatUtils
import org.lwjgl.opengl.ARBVertexBufferObject
import org.lwjgl.opengl.GL15
import java.nio.FloatBuffer

const val GL_ARRAY_BUFFER = 0x8892

fun glGenBuffers(): Int {
    return if (GlCompatUtils.arbVbo) ARBVertexBufferObject.glGenBuffersARB()
    else GL15.glGenBuffers()
}

fun glBindBuffer(target: Int, buffer: Int) {
    if (GlCompatUtils.arbVbo) {
        ARBVertexBufferObject.glBindBufferARB(target, buffer)
    } else {
        GL15.glBindBuffer(target, buffer)
    }
}

fun glBufferData(target: Int, data: FloatBuffer?, usage: Int) {
    if (GlCompatUtils.arbVbo) {
        ARBVertexBufferObject.glBufferDataARB(target, data, usage)
    } else {
        GL15.glBufferData(target, data, usage)
    }
}