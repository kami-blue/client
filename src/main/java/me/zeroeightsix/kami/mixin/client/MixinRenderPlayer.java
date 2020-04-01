package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.modules.render.Nametags;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * Created by 086 on 19/12/2017.
 */
@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {

    @Inject(method = "renderEntityName", at = @At("HEAD"), cancellable = true)
    public void renderLivingLabel(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo info) {
        if (MODULE_MANAGER.isModuleEnabled(Nametags.class)) info.cancel();
    }

}
