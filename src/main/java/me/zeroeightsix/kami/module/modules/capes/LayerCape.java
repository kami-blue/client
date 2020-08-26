package me.zeroeightsix.kami.module.modules.capes;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class LayerCape implements LayerRenderer<AbstractClientPlayer> {

    private final RenderPlayer playerRenderer;

    public LayerCape(RenderPlayer playerRenderer) {
        this.playerRenderer = playerRenderer;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ResourceLocation rl = Capes.getCapeResource(player);

        ItemStack itemstack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!player.hasPlayerInfo() || player.isInvisible() || !player.isWearing(EnumPlayerModelParts.CAPE) || itemstack.getItem() == Items.ELYTRA || rl == null)
            return;

        float f9 = 0.14f;
        float f10 = 0.0f;
        if (player.isSneaking()) {
            f9 = 0.1f;
            f10 = 0.09f;
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.playerRenderer.bindTexture(rl);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0f, f10, f9);
        double d0 = player.prevChasingPosX + (player.chasingPosX - player.prevChasingPosX) * new Float(partialTicks).doubleValue() - (player.prevPosX + (player.posX - player.prevPosX) * new Float(partialTicks).doubleValue());
        double d1 = player.prevChasingPosY + (player.chasingPosY - player.prevChasingPosY) * new Float(partialTicks).doubleValue() - (player.prevPosY + (player.posY - player.prevPosY) * new Float(partialTicks).doubleValue());
        double d2 = player.prevChasingPosZ + (player.chasingPosZ - player.prevChasingPosZ) * new Float(partialTicks).doubleValue() - (player.prevPosZ + (player.posZ - player.prevPosZ) * new Float(partialTicks).doubleValue());
        float f = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        double d3 = new Float(MathHelper.sin(f * 0.01745329f)).doubleValue();
        double d4 = new Float(-MathHelper.cos(f * 0.01745329f)).doubleValue();
        float f1 = new Double(d1).floatValue() * 10.0f;
        f1 = MathHelper.clamp(f1, 3.0f, 32.0f);
        float f2 = new Double(d0 * d3 + d2 * d4).floatValue() * 100.0f;
        float f3 = new Double(d0 * d4 - d2 * d3).floatValue() * 100.0f;
        if (f2 < 0.0f) {
            f2 = 0.0f;
        }
        float f4 = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
        f1 += MathHelper.sin((player.prevDistanceWalkedModified + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks) * 6.0f) * 32.0f * f4;
        if (player.isSneaking()) {
            f1 += 20.0f;
        }
        GlStateManager.rotate(5.0f + f2 / 2.0f + f1, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(f3 / 2.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager.rotate(-f3 / 2.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
        this.playerRenderer.getMainModel().renderCape(0.0625f);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}