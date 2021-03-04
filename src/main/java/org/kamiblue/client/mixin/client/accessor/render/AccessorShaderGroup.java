package org.kamiblue.client.mixin.client.accessor.render;

import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ShaderGroup.class)
public interface AccessorShaderGroup {
    @Accessor("listShaders")
    List<Shader> getListShaders();

    @Accessor("listFramebuffers")
    List<Framebuffer> getListFramebuffers();
}
