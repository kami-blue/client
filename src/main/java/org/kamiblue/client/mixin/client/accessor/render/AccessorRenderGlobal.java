package org.kamiblue.client.mixin.client.accessor.render;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderGlobal.class)
public interface AccessorRenderGlobal {

    @Accessor("entityOutlineShader")
    ShaderGroup getEntityOutlineShader();

}
