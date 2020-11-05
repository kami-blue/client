package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.renderer.debug.DebugRendererChunkBorder;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DebugRendererChunkBorder.class)
public class MixinDebugRendererChunkBorder {

    @ModifyVariable(method = "render", at = @At(value = "STORE", ordinal = 0))
    public EntityPlayer render(EntityPlayer entityPlayer) {
        if (Wrapper.getMinecraft().renderViewEntity instanceof EntityPlayer) {
            return (EntityPlayer) Wrapper.getMinecraft().renderViewEntity;
        } else {
            return Wrapper.getMinecraft().player;
        }
    }

}
