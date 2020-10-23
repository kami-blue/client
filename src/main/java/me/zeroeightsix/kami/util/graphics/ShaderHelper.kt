package me.zeroeightsix.kami.util.graphics

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.event.events.ResolutionUpdateEvent
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.client.shader.ShaderLinkHelper
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

class ShaderHelper(shaderIn: ResourceLocation, vararg frameBufferNames: String) {
    private val mc = Wrapper.minecraft

    val shader: ShaderGroup?
    private val frameBufferMap = HashMap<String, Framebuffer>()

    init {
        shader = if (!OpenGlHelper.shadersSupported) {
            KamiMod.log.warn("Shaders are unsupported by OpenGL!")
            null
        } else if (isIntegratedGraphics()) {
            KamiMod.log.warn("Running on Intel Integrated Graphics!")
            null
        } else {
            println("=\n".repeat(50) + GlStateManager.glGetString(GL11.GL_VENDOR))
            try {
                ShaderLinkHelper.setNewStaticShaderLinkHelper()
                ShaderGroup(mc.textureManager, mc.resourceManager, mc.framebuffer, shaderIn).also {
                    it.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
                }
            } catch (e: Exception) {
                KamiMod.log.warn("Failed to load shaders")
                e.printStackTrace()
                null
            }?.also {
                for (name in frameBufferNames) {
                    frameBufferMap[name] = it.getFramebufferRaw(name)
                }
            }
        }
    }

    fun getFrameBuffer(name: String) = frameBufferMap[name]

    // We are putting it here so it can find the listener above
    init {
        listener<ResolutionUpdateEvent> {
            shader?.createBindFramebuffers(it.width, it.height) // this will not run if on Intel GPU or unsupported Shaders
        }

        KamiEventBus.subscribe(this)
    }

    companion object {
        private var cachedIsIntegratedGraphics: Boolean? = null

        fun isIntegratedGraphics() = cachedIsIntegratedGraphics ?: run {
            cachedIsIntegratedGraphics = GlStateManager.glGetString(GL11.GL_VENDOR).contains("Intel")
            cachedIsIntegratedGraphics!! // cannot be null, we just set the value
        }
    }
}