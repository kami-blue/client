/*
 * This file is adapted from 2b2t-Utilities/emoji-api which is licensed under MIT.
 * You can find a copy of the original license here: https://github.com/2b2t-Utilities/emoji-api/blob/35b0683/LICENSE
 */

package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.manager.mangers.KamiMojiManager;
import me.zeroeightsix.kami.module.modules.chat.KamiMoji;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;

/**
 * Updated by Xiaro on 10/08/20
 */
@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {

    @Shadow public int FONT_HEIGHT;
    @Shadow public float alpha;
    @Shadow public float posX;
    @Shadow public float posY;
    @Shadow public float red;
    @Shadow public float green;
    @Shadow public float blue;

    @Shadow protected abstract void renderStringAtPos(String text, boolean shadow);
    @Shadow public abstract int getStringWidth(String s);
    @Shadow public abstract int getCharWidth(char character);

    /**
     * @author Tiger
     */
    @Redirect(method = "renderString", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;renderStringAtPos(Ljava/lang/String;Z)V"))
    private void renderStringAtPos(FontRenderer fontRenderer, String text, boolean shadow) {
        if (KamiMoji.INSTANCE.isEnabled() && text.contains(":")) {
            int size = FONT_HEIGHT;

            for (String possible : text.split(":")) {
                if (KamiMojiManager.INSTANCE.isEmoji(possible)) {
                    KamiMojiManager.Emoji emoji = new KamiMojiManager.Emoji(possible);
                    String emojiText = ":" + possible + ":";
                    if (!shadow) {
                        int index = text.indexOf(emojiText);
                        if (index == -1) continue;
                        int x = getStringWidth(text.substring(0, index)) + FONT_HEIGHT / 4;
                        drawEmoji(KamiMojiManager.INSTANCE.getEmoji(emoji), posX + x, posY, size, alpha);
                    }
                    text = text.replaceFirst(emojiText, getReplacement());
                }
            }
        }
        GlStateManager.color(red, blue, green, alpha); // Big Mojang meme :monkey:
        renderStringAtPos(text, shadow);
    }

    /**
     * @author cats
     */
    @Inject(method = "getStringWidth", at = @At("TAIL"), cancellable = true)
    public void getStringWidth(String text, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() != 0 && KamiMoji.INSTANCE.isEnabled() && text.contains(":")) {
            int reducedWidth = cir.getReturnValue();
            for (String possible : text.split(":")) {
                if (KamiMojiManager.INSTANCE.isEmoji(possible)) {
                    String emojiText = ":" + possible + ":";
                    int emojiTextWidth = emojiText.chars().map(i -> getCharWidth((char) i)).sum();
                    reducedWidth -= emojiTextWidth;
                    text = text.replaceFirst(emojiText, getReplacement());
                }
            }
            cir.setReturnValue(reducedWidth);
        }
    }

    /* This is created because vanilla one doesn't take double position input */
    private void drawEmoji(ResourceLocation emojiTexture, double x, double y, float size, float alpha) {
        if (emojiTexture == null) return;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        Wrapper.getMinecraft().getTextureManager().bindTexture(emojiTexture);
        GlStateManager.color(1f, 1f, 1f, alpha);
        GlStateManager.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, (y + size), 0.0).tex(0.0, 1.0).endVertex();
        bufferbuilder.pos(x + size, y + size, 0.0).tex(1.0, 1.0).endVertex();
        bufferbuilder.pos(x + size, y, 0.0).tex(1.0, 0.0).endVertex();
        bufferbuilder.pos(x, y, 0.0).tex(0.0, 0.0).endVertex();
        tessellator.draw();
    }

    private String getReplacement() {
        int emojiWidth = (int) Math.ceil((float) FONT_HEIGHT / (float) getCharWidth(' '));
        char[] spaces = new char[emojiWidth];
        Arrays.fill(spaces, ' ');
        return new String(spaces);
    }
}
