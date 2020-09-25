package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.RenderEntityEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Xiaro
 * <p>
 * Created by Xiaro on 05/09/20
 */
@Mixin(value = RenderManager.class, priority = 114514)
public class MixinRenderManager {

    @Shadow
    public boolean renderOutlines;
    @Shadow
    public boolean debugBoundingBox;

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    public void renderEntityPre(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        RenderEntityEvent.Pre event = new RenderEntityEvent.Pre(entity, x, y, z, yaw, partialTicks, debug);
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) ci.cancel();
    }

    // Weird way around because don't wanna mess up with shadow and hitbox rendering
    @Inject(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;doRenderShadowAndFire(Lnet/minecraft/entity/Entity;DDDFF)V"))
    public void renderEntityPostShadow(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        RenderEntityEvent.Post event = new RenderEntityEvent.Post(entity, x, y, z, yaw, partialTicks, debug);
        KamiMod.EVENT_BUS.post(event);
    }

    @Inject(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderDebugBoundingBox(Lnet/minecraft/entity/Entity;DDDFF)V"))
    public void renderEntityPostDebugBB(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        if (!this.renderOutlines) return;
        RenderEntityEvent.Post event = new RenderEntityEvent.Post(entity, x, y, z, yaw, partialTicks, debug);
        KamiMod.EVENT_BUS.post(event);
    }

    @Inject(method = "renderEntity", at = @At("RETURN"))
    public void renderEntityPostReturn(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        RenderEntityEvent.Final eventFinal = new RenderEntityEvent.Final(entity, x, y, z, yaw, partialTicks, debug);
        KamiMod.EVENT_BUS.post(eventFinal);

        if (!this.renderOutlines || (this.debugBoundingBox && !entity.isInvisible() && !debug && !Minecraft.getMinecraft().isReducedDebug()))
            return;
        RenderEntityEvent.Post event = new RenderEntityEvent.Post(entity, x, y, z, yaw, partialTicks, debug);
        KamiMod.EVENT_BUS.post(event);
    }

}
