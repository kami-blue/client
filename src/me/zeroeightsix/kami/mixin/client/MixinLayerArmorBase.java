// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.mixin.client;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import me.zeroeightsix.kami.module.modules.gui.ArmourHide;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({ LayerArmorBase.class })
public abstract class MixinLayerArmorBase
{
    @Inject(method = { "renderArmorLayer" }, at = { @At("HEAD") }, cancellable = true)
    public void onRenderArmorLayer(final EntityLivingBase entityLivingBaseIn, final float limbSwing, final float limbSwingAmount, final float partialTicks, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale, final EntityEquipmentSlot slotIn, final CallbackInfo ci) {
        if (ArmourHide.INSTANCE.isEnabled()) {
            if (!ArmourHide.INSTANCE.player.getValue() && entityLivingBaseIn instanceof EntityPlayer) {
                if (!ArmourHide.shouldRenderPiece(slotIn)) {
                    ci.cancel();
                }
            }
            else if (!ArmourHide.INSTANCE.armourstand.getValue() && entityLivingBaseIn instanceof EntityArmorStand) {
                if (!ArmourHide.shouldRenderPiece(slotIn)) {
                    ci.cancel();
                }
            }
            else if (!ArmourHide.INSTANCE.mobs.getValue() && entityLivingBaseIn instanceof EntityMob && !ArmourHide.shouldRenderPiece(slotIn)) {
                ci.cancel();
            }
        }
    }
}
