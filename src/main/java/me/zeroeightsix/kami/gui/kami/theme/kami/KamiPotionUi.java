package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.component.Potions;
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.util.PotionInfo;
import me.zeroeightsix.kami.util.color.ColorConverter;
import me.zeroeightsix.kami.util.color.ColorHolder;
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class KamiPotionUi extends AbstractComponentUI<Potions> {

    @Override
    public void renderComponent(Potions component) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        int y = 2;

        List<PotionInfo> potions = new ArrayList<>();
        mc.player.getActivePotionMap().forEach((potion, potionEffect) -> potions.add(new PotionInfo(
                I18n.format(potion.getName()),
                potionEffect.getAmplifier(),
                potionEffect
        )));

        Function<Float, Float> xFunc;
        switch (component.getAlignment()) {
            case RIGHT:
                xFunc = i -> component.getWidth() - i;
                break;
            case CENTER:
                xFunc = i -> component.getWidth() / 2f - i / 2f;
                break;
            case LEFT:
            default:
                xFunc = i -> 0f;
                break;
        }


        for (PotionInfo potion : potions) {
            ColorHolder color = ColorConverter.intToRgb(potion.getPotionEffect().getPotion().getLiquidColor());
            String text = potion.formattedName(component.getAlignment().equals(AlignedComponent.Alignment.RIGHT));
            float textWidth = FontRenderAdapter.INSTANCE.getStringWidth(text);
            float textHeight = FontRenderAdapter.INSTANCE.getFontHeight() + 1;
            FontRenderAdapter.INSTANCE.drawString(text, xFunc.apply(textWidth), y, true, color);
            y += textHeight;
        }

        component.setHeight(2);

    }

    @Override
    public void handleSizeComponent(Potions component) {
        component.setWidth(100);
        component.setHeight(100);
    }
}
