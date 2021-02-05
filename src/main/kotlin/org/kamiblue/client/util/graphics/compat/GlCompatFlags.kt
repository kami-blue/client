package org.kamiblue.client.util.graphics.compat

import org.lwjgl.opengl.ContextCapabilities
import org.lwjgl.opengl.GLContext

object GlCompatFlags {
    private val contextCapabilities: ContextCapabilities = GLContext.getCapabilities()

    val arbVbo = !contextCapabilities.OpenGL15 && contextCapabilities.GL_ARB_vertex_buffer_object
}