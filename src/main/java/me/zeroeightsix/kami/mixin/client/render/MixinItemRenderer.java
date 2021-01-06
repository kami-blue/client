package me.zeroeightsix.kami.mixin.client.render;

import me.zeroeightsix.kami.module.modules.player.Freecam;
import me.zeroeightsix.kami.module.modules.render.ItemModel;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {
    @Inject(method = "rotateArm", at = @At("HEAD"), cancellable = true)
    private void rotateArm(float partialTicks, CallbackInfo ci) {
        if (Freecam.INSTANCE.isEnabled() && Freecam.INSTANCE.getCameraGuy() != null) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V", shift = At.Shift.AFTER))
    private void transformSideFirstPerson$pushMatrix(AbstractClientPlayer player, float partialTicks, float pitch, EnumHand hand, float swingProgress, ItemStack stack, float equippedProgress, CallbackInfo ci) {
        if (ItemModel.INSTANCE.isEnabled()) {
            if (!ItemModel.INSTANCE.getModifyHand() && stack.isEmpty()) return;

            EnumHandSide enumhandside = hand == EnumHand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
            float sideMultiplier = enumhandside == EnumHandSide.RIGHT ? 1.0f : -1.0f;

            GlStateManager.translate(ItemModel.INSTANCE.getPosX() * sideMultiplier, ItemModel.INSTANCE.getPosY(), ItemModel.INSTANCE.getPosZ());
        }
    }

    @Inject(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemSide(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V"))
    private void transformSideFirstPerson$renderItemSide(AbstractClientPlayer player, float partialTicks, float pitch, EnumHand hand, float swingProgress, ItemStack stack, float equippedProgress, CallbackInfo ci) {
        if (ItemModel.INSTANCE.isEnabled()) {
            if (!ItemModel.INSTANCE.getModifyHand() && stack.isEmpty()) return;

            EnumHandSide enumhandside = hand == EnumHand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
            float sideMultiplier = enumhandside == EnumHandSide.RIGHT ? 1.0f : -1.0f;
            float scale = ItemModel.INSTANCE.getScale();

            GlStateManager.rotate(ItemModel.INSTANCE.getRotateX(), 1.0f, 0.0f, 0.0f);
            GlStateManager.rotate(ItemModel.INSTANCE.getRotateY() * sideMultiplier, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(ItemModel.INSTANCE.getRotateZ() * sideMultiplier, 0.0f, 0.0f, 1.0f);
            GlStateManager.scale(scale, scale, scale);
        }
    }
}
