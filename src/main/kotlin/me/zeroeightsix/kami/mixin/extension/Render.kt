package me.zeroeightsix.kami.mixin.extension

import me.zeroeightsix.kami.mixin.client.accessor.render.AccessorRenderGlobal
import me.zeroeightsix.kami.mixin.client.accessor.render.AccessorRenderManager
import me.zeroeightsix.kami.mixin.client.accessor.render.AccessorShaderGroup
import me.zeroeightsix.kami.mixin.client.accessor.render.AccessorViewFrustum
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.ViewFrustum
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.shader.Shader
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.math.BlockPos

val RenderGlobal.entityOutlineShader: ShaderGroup get() = (this as AccessorRenderGlobal).entityOutlineShader

val RenderManager.renderPosX: Double get() = (this as AccessorRenderManager).renderPosX
val RenderManager.renderPosY: Double get() = (this as AccessorRenderManager).renderPosY
val RenderManager.renderPosZ: Double get() = (this as AccessorRenderManager).renderPosZ
val RenderManager.renderOutlines: Boolean get() = (this as AccessorRenderManager).renderOutlines

val ShaderGroup.listShaders: List<Shader> get() = (this as AccessorShaderGroup).listShaders

// Unused, but kept for consistency. Java equivalent used in Mixins
fun ViewFrustum.getRenderChunk(pos: BlockPos) = (this as AccessorViewFrustum).invokeGetRenderChunk(pos)
