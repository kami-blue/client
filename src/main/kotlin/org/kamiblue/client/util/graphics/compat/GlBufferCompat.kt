package me.zeroeightsix.kami.util.graphics.compat

import org.lwjgl.opengl.ARBVertexBufferObject
import org.lwjgl.opengl.GL15
import java.nio.ByteBuffer
import java.nio.FloatBuffer

fun genBuffers(): Int {
    return if (GlCompatFlags.arbVbo) ARBVertexBufferObject.glGenBuffersARB()
    else GL15.glGenBuffers()
}

fun bindBuffer(target: Int, buffer: Int) {
    if (GlCompatFlags.arbVbo) {
        ARBVertexBufferObject.glBindBufferARB(target, buffer)
    } else {
        GL15.glBindBuffer(target, buffer)
    }
}

fun bufferData(target: Int, data: ByteBuffer, usage: Int) {
    if (GlCompatFlags.arbVbo) {
        ARBVertexBufferObject.glBufferDataARB(target, data, usage)
    } else {
        GL15.glBufferData(target, data, usage)
    }
}

fun bufferSubData(target: Int, offset: Long, data: ByteBuffer) {
    if (GlCompatFlags.arbVbo) {
        ARBVertexBufferObject.glBufferSubDataARB(target,0L, data)
    } else {
        GL15.glBufferSubData(target,0L, data)
    }
}