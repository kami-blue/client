package org.kamiblue.client.mixin.extension

import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.ViewFrustum
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.Shader
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.math.BlockPos
import org.kamiblue.client.mixin.client.accessor.render.AccessorRenderGlobal
import org.kamiblue.client.mixin.client.accessor.render.AccessorRenderManager
import org.kamiblue.client.mixin.client.accessor.render.AccessorShaderGroup
import org.kamiblue.client.mixin.client.accessor.render.AccessorViewFrustum

val RenderGlobal.entityOutlineShader: ShaderGroup get() = (this as AccessorRenderGlobal).entityOutlineShader

val RenderManager.renderPosX: Double get() = (this as AccessorRenderManager).renderPosX
val RenderManager.renderPosY: Double get() = (this as AccessorRenderManager).renderPosY
val RenderManager.renderPosZ: Double get() = (this as AccessorRenderManager).renderPosZ
val RenderManager.renderOutlines: Boolean get() = (this as AccessorRenderManager).renderOutlines

val ShaderGroup.listShaders: List<Shader> get() = (this as AccessorShaderGroup).listShaders
val ShaderGroup.listFrameBuffers: List<Framebuffer> get() = (this as AccessorShaderGroup).listFramebuffers

// Unused, but kept for consistency. Java equivalent used in Mixins
fun ViewFrustum.getRenderChunk(pos: BlockPos) = (this as AccessorViewFrustum).invokeGetRenderChunk(pos)
