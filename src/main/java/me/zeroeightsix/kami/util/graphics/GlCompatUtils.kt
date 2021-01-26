package me.zeroeightsix.kami.util.graphics

import net.minecraft.client.renderer.OpenGlHelper
import org.lwjgl.opengl.GLContext

object GlCompatUtils {
    var contextCapabilities = GLContext.getCapabilities()

    val arbVbo = !contextCapabilities.OpenGL15 && contextCapabilities.GL_ARB_vertex_buffer_object
}