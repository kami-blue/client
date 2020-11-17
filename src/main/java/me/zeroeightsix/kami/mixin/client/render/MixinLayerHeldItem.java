package me.zeroeightsix.kami.mixin.client.render;

import me.zeroeightsix.kami.util.graphics.GlStateUtils;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

@Mixin(LayerHeldItem.class)
public abstract class MixinLayerHeldItem {

    private static final FloatBuffer color = GLAllocation.createDirectFloatBuffer(16);
    private static boolean texture2d = false;
    private static boolean colorLock = false;

    @Inject(method = "renderHeldItem", at = @At("HEAD"))
    public void renderHeldItemPre(EntityLivingBase entity, ItemStack itemStack, ItemCameraTransforms.TransformType transformType, EnumHandSide handSide, CallbackInfo ci) {
        colorLock = GlStateUtils.getColorLock();
        texture2d = glGetBoolean(GL_TEXTURE_2D);
        if (colorLock) {
            glGetFloat(GL_CURRENT_COLOR, color);
            GlStateUtils.colorLock(false);
        }
        if (!texture2d) {
            GlStateManager.enableTexture2D();
        }
    }

    @Inject(method = "renderHeldItem", at = @At("RETURN"))
    public void renderHeldItemPost(EntityLivingBase entity, ItemStack itemStack, ItemCameraTransforms.TransformType transformType, EnumHandSide handSide, CallbackInfo ci) {
        if (colorLock) {
            GlStateManager.color(color.get(0), color.get(1), color.get(2), color.get(3));
            GlStateUtils.colorLock(true);
        }
        if (!texture2d) {
            GlStateManager.disableTexture2D();
        }
    }

}