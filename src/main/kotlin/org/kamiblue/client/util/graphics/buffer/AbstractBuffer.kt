package org.kamiblue.client.util.graphics.buffer

import org.kamiblue.client.util.graphics.compat.glBindBufferC
import org.kamiblue.client.util.graphics.compat.glBufferDataC
import org.kamiblue.client.util.graphics.compat.glBufferSubDataC
import org.kamiblue.client.util.graphics.compat.glGenBuffersC
import java.nio.ByteBuffer

abstract class AbstractBuffer {
    protected abstract val usage: Int
    protected abstract val target: Int
    protected abstract val buffer: ByteBuffer

    protected val id by lazy { glGenBuffersC() }

    var renderSize = 0; private set
    var bufferSize = 0; private set

    protected fun upload(newSize: Int) {
        preUpload()

        if (shouldResize(newSize)) {
            allocate(newSize)
        } else {
            glBufferSubDataC(target, 0L, buffer)
        }

        postUpload()
        renderSize = newSize
    }

    fun preUpload() {
        bindBuffer()
        buffer.flip()
    }

    fun postUpload() {
        unbindBuffer()
        buffer.clear()
    }

    fun shouldResize(newSize: Int) =
        newSize > bufferSize || bufferSize - newSize > 1024

    fun allocate(newSize: Int) {
        glBufferDataC(target, buffer, usage)
        bufferSize = newSize
    }

    fun bindBuffer() {
        glBindBufferC(target, id)
    }

    fun unbindBuffer() {
        glBindBufferC(target, 0)
    }
}