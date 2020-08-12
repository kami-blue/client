package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
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
}
