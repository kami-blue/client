/*
 * This file is adapted from 2b2t-Utilities/emoji-api which is licensed under MIT.
 * You can find a copy of the original license here: https://github.com/2b2t-Utilities/emoji-api/blob/35b0683/LICENSE
 */

package org.kamiblue.client.mixin.client.render;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.kamiblue.client.module.modules.chat.KamiMoji;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Updated by Xiaro on 10/08/20
 */
@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {

    @Shadow public int FONT_HEIGHT;
    @Shadow protected float posX;
    @Shadow protected float posY;
    @Shadow private float alpha;
    @Shadow private float red;
    @Shadow private float green;
    @Shadow private float blue;

    @Shadow protected abstract void renderStringAtPos(String text, boolean shadow);

    /**
     * @author Tiger
     */
    @Inject(method = "renderString", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;renderStringAtPos(Ljava/lang/String;Z)V", shift = At.Shift.BEFORE), cancellable = true)
    private void renderStringAtPos(String text, float x, float y, int color, boolean shadow, CallbackInfoReturnable<Integer> cir) {
        if (KamiMoji.INSTANCE.isEnabled() && text.contains(":")) {
            text = KamiMoji.renderText(text, FONT_HEIGHT, shadow, posX, posY, alpha);
            GlStateManager.color(red, blue, green, alpha); // Big Mojang meme :monkey:
            renderStringAtPos(text, shadow);
            cir.setReturnValue((int) posX);
        }
    }

    /**
     * @author cats
     */
    @Inject(method = "getStringWidth", at = @At("TAIL"), cancellable = true)
    public void getStringWidth(String text, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() != 0 && KamiMoji.INSTANCE.isEnabled() && text.contains(":")) {
            cir.setReturnValue(KamiMoji.getStringWidth(cir.getReturnValue(), text, FONT_HEIGHT));
        }
    }
}
