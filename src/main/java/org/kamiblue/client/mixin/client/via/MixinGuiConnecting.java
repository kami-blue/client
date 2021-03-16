package org.kamiblue.client.mixin.client.via;

import net.minecraft.client.multiplayer.GuiConnecting;
import org.kamiblue.client.via.viafabric.ViaFabric;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiConnecting.class)
public abstract class MixinGuiConnecting {

    @Inject(method = "connect", at = @At("HEAD"))
    public void injectConnect(String ip, int port, CallbackInfo ci) {
        ViaFabric.lastServer = ip + ":" + port;
    }
}