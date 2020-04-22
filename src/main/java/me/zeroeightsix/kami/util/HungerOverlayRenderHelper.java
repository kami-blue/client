package me.zeroeightsix.kami.util;

import me.zeroeightsix.kami.event.ForgeEventProcessor;
import me.zeroeightsix.kami.gui.kami.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.init.MobEffects;

public class HungerOverlayRenderHelper {

    public static void drawSaturationOverlay(float saturationGained, float saturationLevel, Minecraft mc, int left, int top, float alpha) {
        if (saturationLevel + saturationGained < 0) {
            return;
        }

        int startBar = saturationGained != 0 ? Math.max(0, (int) saturationLevel / 2) : 0;
        int endBar = (int) Math.ceil(Math.min(20, saturationLevel + saturationGained) / 2f);
        int barsNeeded = endBar - startBar;

        mc.getTextureManager().bindTexture(ForgeEventProcessor.icons);

        RenderHelper.enableAlpha(alpha);

        for (int i = startBar; i < startBar + barsNeeded; ++i) {
            int x = left - i * 8 - 9;

            float effectiveSaturationOfBar = (saturationLevel + saturationGained) / 2 - i;

            if (effectiveSaturationOfBar >= 1) {
                mc.ingameGUI.drawTexturedModalRect(x, top, 27, 0, 9, 9);
            } else if (effectiveSaturationOfBar > .5) {
                mc.ingameGUI.drawTexturedModalRect(x, top, 18, 0, 9, 9);
            } else if (effectiveSaturationOfBar > .25) {
                mc.ingameGUI.drawTexturedModalRect(x, top, 9, 0, 9, 9);
            } else if (effectiveSaturationOfBar > 0) {
                mc.ingameGUI.drawTexturedModalRect(x, top, 0, 0, 9, 9);
            }
        }

        RenderHelper.disableAlpha(alpha);

        mc.getTextureManager().bindTexture(Gui.ICONS);
    }

    public static void drawHungerOverlay(int hungerRestored, int foodLevel, Minecraft mc, int left, int top, float alpha) {
        if (hungerRestored == 0) {
            return;
        }

        int startBar = foodLevel / 2;
        int endBar = (int) Math.ceil(Math.min(20, foodLevel + hungerRestored) / 2f);
        int barsNeeded = endBar - startBar;

        mc.getTextureManager().bindTexture(Gui.ICONS);

        RenderHelper.enableAlpha(alpha);

        for (int i = startBar; i < startBar + barsNeeded; ++i) {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;

            int icon = 16;
            int background = 13;

            if (mc.player.isPotionActive(MobEffects.HUNGER)) {
                icon += 36;
                background = 13;
            }

            mc.ingameGUI.drawTexturedModalRect(x, top, 16 + background * 9, 27, 9, 9);

            if (idx < foodLevel + hungerRestored) {
                mc.ingameGUI.drawTexturedModalRect(x, top, icon + 36, 27, 9, 9);
            } else if (idx == foodLevel + hungerRestored) {
                mc.ingameGUI.drawTexturedModalRect(x, top, icon + 45, 27, 9, 9);
            }
        }

        RenderHelper.disableAlpha(alpha);
    }

    public static void drawExhaustionOverlay(float exhaustion, Minecraft mc, int left, int top, float alpha) {
        mc.getTextureManager().bindTexture(ForgeEventProcessor.icons);

        float maxExhaustion = HungerOverlayUtils.getMaxExhaustion(mc.player);
        float ratio = exhaustion / maxExhaustion;

        int width = (int) (ratio * 81);
        int height = 9;

        RenderHelper.enableAlpha(.75f);
        mc.ingameGUI.drawTexturedModalRect(left - width, top, 81 - width, 18, width, height);
        RenderHelper.disableAlpha(.75f);

        mc.getTextureManager().bindTexture(Gui.ICONS);
    }

}
