package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.modules.client.Capes;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.LayerDeadmau5Head;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LayerDeadmau5Head.class)
public class MixinLayerDeadmau5Head {
    @Redirect(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;getName()Ljava/lang/String;"))
    public String getName(AbstractClientPlayer abstractClientPlayer) {
        return Capes.INSTANCE.getEars().getValue() ? "deadmau5" : "";
    }
}