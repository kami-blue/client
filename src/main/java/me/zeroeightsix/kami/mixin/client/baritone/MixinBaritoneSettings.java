package me.zeroeightsix.kami.mixin.client.baritone;

import baritone.api.Settings;
import me.zeroeightsix.kami.event.KamiEventBus;
import me.zeroeightsix.kami.event.events.BaritoneSettingsInitEvent;
import me.zeroeightsix.kami.util.BaritoneUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Settings.class, remap = false)
public class MixinBaritoneSettings {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void baritoneSettingsInit(CallbackInfo ci) {
        KamiEventBus.INSTANCE.post(new BaritoneSettingsInitEvent());
        BaritoneUtils.INSTANCE.setSettingsInitialized(true);
    }
}
