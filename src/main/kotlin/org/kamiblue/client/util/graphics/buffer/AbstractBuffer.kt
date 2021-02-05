package me.zeroeightsix.kami.util.graphics.buffer

import me.zeroeightsix.kami.util.graphics.compat.bindBuffer
import me.zeroeightsix.kami.util.graphics.compat.bufferData
import me.zeroeightsix.kami.util.graphics.compat.bufferSubData
import me.zeroeightsix.kami.util.graphics.compat.genBuffers
import java.nio.ByteBuffer

abstract class AbstractBuffer {
    protected abstract val usage: Int
    protected abstract val target: Int
    protected abstract val buffer: ByteBuffer

    protected val id by lazy { genBuffers() }

    var renderSize = 0; private set
    var bufferSize = 0; private set

    protected fun upload(newSize: Int) {
        preUpload()

        if (shouldResize(newSize)) {
            allocate(newSize)
        } else {
            bufferSubData(target, 0L, buffer)
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
        bufferData(target, buffer, usage)
        bufferSize = newSize
    }

    fun bindBuffer() {
        bindBuffer(target, id)
    }

    fun unbindBuffer() {
        bindBuffer(target, 0)
    }
}