package me.zeroeightsix.kami.mixin.client.accessor.render

import net.minecraft.client.shader.Shader
import net.minecraft.client.shader.ShaderGroup

val ShaderGroup.listShaders: List<Shader> get() = (this as AccessorShaderGroup).listShaders