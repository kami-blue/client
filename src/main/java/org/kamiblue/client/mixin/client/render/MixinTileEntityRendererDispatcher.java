package org.kamiblue.client.mixin.client.render;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.kamiblue.client.module.modules.render.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher {
    @Inject(method = "render(Lnet/minecraft/tileentity/TileEntity;FI)V", at = @At("HEAD"), cancellable = true)
    public void render(TileEntity entity, float partialTicks, int destroyStage, CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled()) {
            if (NoRender.INSTANCE.tryReplaceEnchantingTable(entity) || NoRender.INSTANCE.getEntityList().contains(entity.getClass())) {
                ci.cancel();
            }
        }
    }
}
