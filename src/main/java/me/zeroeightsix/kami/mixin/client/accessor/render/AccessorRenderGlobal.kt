package me.zeroeightsix.kami.mixin.client.accessor.render

import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.shader.ShaderGroup

val RenderGlobal.entityOutlineShader: ShaderGroup get() = (this as AccessorRenderGlobal).entityOutlineShader