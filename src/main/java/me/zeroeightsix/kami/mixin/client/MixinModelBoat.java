package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.movement.EntitySpeed;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.model.ModelBoat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by 086 on 15/12/2017.
 */
@Mixin(ModelBoat.class)
public class MixinModelBoat {

    @Inject(method = "render", at = @At("HEAD"))
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo info) {
        if (Wrapper.getPlayer().getRidingEntity() == entityIn && ModuleManager.isModuleEnabled(EntitySpeed.class)) {
            GlStateManager.color(1, 1, 1, EntitySpeed.getOpacity());
            GlStateManager.enableBlend();
        }
    }

}
