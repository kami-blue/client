package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.util.graphics.KamiTessellator;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiInventory.class)
public class MixinGuiInventory {
    @Redirect(method = "drawGuiContainerBackgroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiInventory;drawEntityOnScreen(IIIFFLnet/minecraft/entity/EntityLivingBase;)V"))
    private void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        GuiInventory.drawEntityOnScreen(posX, posY, scale, mouseX, mouseY, ent);
        GlStateManager.disableDepth();
        GlStateManager.popMatrix();
    }

    @Redirect(method = "drawEntityOnScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderEntity(Lnet/minecraft/entity/Entity;DDDFFZ)V"))
    private static void renderEntity(RenderManager renderManager, Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase e = (EntityLivingBase) entity;
            float prevRotationYaw = e.prevRotationYaw;
            float prevRotationPitch = e.prevRotationPitch;
            float prevRenderYawOffset = e.prevRenderYawOffset;
            e.prevRotationYaw = e.rotationYaw;
            e.prevRotationPitch = e.rotationPitch;
            e.prevRenderYawOffset = e.renderYawOffset;
            renderManager.renderEntity(e, x, y, z, yaw, KamiTessellator.pTicks(), debug);
            e.prevRotationYaw = prevRotationYaw;
            e.prevRotationPitch = prevRotationPitch;
            e.prevRenderYawOffset = prevRenderYawOffset;
        } else {
            renderManager.renderEntity(entity, x, y, z, yaw, KamiTessellator.pTicks(), debug);
        }
    }
}
