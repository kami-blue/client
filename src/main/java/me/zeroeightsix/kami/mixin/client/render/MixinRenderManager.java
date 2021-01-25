package me.zeroeightsix.kami.mixin.client.render;

import me.zeroeightsix.kami.event.KamiEventBus;
import me.zeroeightsix.kami.event.Phase;
import me.zeroeightsix.kami.event.events.RenderEntityEvent;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderManager.class, priority = 114514)
public class MixinRenderManager {
    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    public void renderEntityPre(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        if (entity == null) return;

        RenderEntityEvent event = new RenderEntityEvent(entity, x, y, z, yaw, partialTicks, Phase.PRE);
        KamiEventBus.INSTANCE.post(event);

        if (event.getCancelled()) ci.cancel();
    }

    @Inject(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V", shift = At.Shift.AFTER))
    public void renderEntityPeri(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        if (entity == null) return;

        RenderEntityEvent event = new RenderEntityEvent(entity, x, y, z, yaw, partialTicks, Phase.PERI);
        KamiEventBus.INSTANCE.post(event);
    }

    @Inject(method = "renderEntity", at = @At("RETURN"))
    public void renderEntityPost(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        if (entity == null) return;

        RenderEntityEvent event = new RenderEntityEvent(entity, x, y, z, yaw, partialTicks, Phase.POST);
        KamiEventBus.INSTANCE.post(event);
    }
}
