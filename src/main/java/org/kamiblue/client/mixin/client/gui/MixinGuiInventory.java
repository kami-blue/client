package org.kamiblue.client.mixin.client.gui;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.EntityLivingBase;
import org.kamiblue.client.util.graphics.KamiTessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiInventory.class)
public class MixinGuiInventory {
    private static float prevRotationYaw = 0.0f;
    private static float prevRotationPitch = 0.0f;
    private static float prevRenderYawOffset = 0.0f;

    @Inject(method = "drawEntityOnScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderEntity(Lnet/minecraft/entity/Entity;DDDFFZ)V", shift = At.Shift.BEFORE))
    private static void drawEntityOnScreenInvokeRenderEntityPre(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase entity, CallbackInfo ci) {
        prevRotationYaw = entity.prevRotationYaw;
        prevRotationPitch = entity.prevRotationPitch;
        prevRenderYawOffset = entity.prevRenderYawOffset;

        entity.prevRotationYaw = entity.rotationYaw;
        entity.prevRotationPitch = entity.rotationPitch;
        entity.prevRenderYawOffset = entity.renderYawOffset;
    }

    @ModifyArg(method = "drawEntityOnScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderEntity(Lnet/minecraft/entity/Entity;DDDFFZ)V"), index = 5)
    private static float drawEntityOnScreenInvokeRenderEntityPartialTicks(float partialTicks) {
        return KamiTessellator.pTicks();
    }

    @Inject(method = "drawEntityOnScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderEntity(Lnet/minecraft/entity/Entity;DDDFFZ)V", shift = At.Shift.AFTER))
    private static void renderEntityPost(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase entity, CallbackInfo ci) {
        entity.prevRotationYaw = prevRotationYaw;
        entity.prevRotationPitch = prevRotationPitch;
        entity.prevRenderYawOffset = prevRenderYawOffset;
    }
}
