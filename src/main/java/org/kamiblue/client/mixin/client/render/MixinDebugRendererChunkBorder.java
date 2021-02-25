package org.kamiblue.client.mixin.client.render;

import net.minecraft.client.renderer.debug.DebugRendererChunkBorder;
import net.minecraft.entity.player.EntityPlayer;
import org.kamiblue.client.util.Wrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DebugRendererChunkBorder.class)
public class MixinDebugRendererChunkBorder {

    @ModifyVariable(method = "render", at = @At(value = "STORE", ordinal = 0))
    public EntityPlayer render(EntityPlayer entityPlayer) {
        if (Wrapper.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
            return (EntityPlayer) Wrapper.getMinecraft().getRenderViewEntity();
        } else {
            return Wrapper.getMinecraft().player;
        }
    }

}
