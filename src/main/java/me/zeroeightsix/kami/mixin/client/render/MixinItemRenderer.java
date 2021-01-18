package me.zeroeightsix.kami.mixin.client.render;

import me.zeroeightsix.kami.module.modules.player.Freecam;
import me.zeroeightsix.kami.module.modules.render.ItemModel;
import me.zeroeightsix.kami.util.math.Vec3f;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
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
            Vec3f vec = ItemModel.getTranslation(stack, hand, player);
            if (vec != null) {
                GlStateManager.translate(vec.getX(), vec.getY(), vec.getZ());
            }
        }
    }

    @Inject(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemSide(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V"))
    private void transformSideFirstPerson$renderItemSide(AbstractClientPlayer player, float partialTicks, float pitch, EnumHand hand, float swingProgress, ItemStack stack, float equippedProgress, CallbackInfo ci) {
        if (ItemModel.INSTANCE.isEnabled()) {
            Vec3f vec = ItemModel.getRotation(stack, hand, player);
            if (vec != null) {
                float scale = ItemModel.INSTANCE.getScale();
                GlStateManager.rotate(vec.getX(), 1.0f, 0.0f, 0.0f);
                GlStateManager.rotate(vec.getY(), 0.0f, 1.0f, 0.0f);
                GlStateManager.rotate(vec.getZ(), 0.0f, 0.0f, 1.0f);
                GlStateManager.scale(scale, scale, scale);
            }
        }
    }
}
