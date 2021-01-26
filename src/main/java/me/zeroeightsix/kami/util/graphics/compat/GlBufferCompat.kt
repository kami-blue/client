package me.zeroeightsix.kami.util.graphics.compat

import org.lwjgl.opengl.ARBVertexBufferObject
import org.lwjgl.opengl.GL15
import java.nio.FloatBuffer
import java.nio.IntBuffer

fun glGenBuffers(): Int {
    return if (GlCompatFlags.arbVbo) ARBVertexBufferObject.glGenBuffersARB()
    else GL15.glGenBuffers()
}

fun glBindBuffer(target: Int, buffer: Int) {
    if (GlCompatFlags.arbVbo) {
        ARBVertexBufferObject.glBindBufferARB(target, buffer)
    } else {
        GL15.glBindBuffer(target, buffer)
    }
}

fun glBufferData(target: Int, data: FloatBuffer, usage: Int) {
    if (GlCompatFlags.arbVbo) {
        ARBVertexBufferObject.glBufferDataARB(target, data, usage)
    } else {
        GL15.glBufferData(target, data, usage)
    }
}

fun glBufferData(target: Int, data: IntBuffer, usage: Int) {
    if (GlCompatFlags.arbVbo) {
        ARBVertexBufferObject.glBufferDataARB(target, data, usage)
    } else {
        GL15.glBufferData(target, data, usage)
    }
}