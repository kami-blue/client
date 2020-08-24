package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.component.Potions;
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.util.PotionInfo;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.util.colourUtils.ColourConverter;
import me.zeroeightsix.kami.util.colourUtils.ColourHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.glDisable;

public class KamiPotionUi extends AbstractComponentUI<Potions> {

    @Override
    public void renderComponent(Potions component, FontRenderer f) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        FontRenderer renderer = Wrapper.getFontRenderer();
        int y = 2;
        GlStateManager.pushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        List<PotionInfo> potions = new ArrayList<>();
        mc.player.getActivePotionMap().forEach((potion, potionEffect) -> potions.add(new PotionInfo(
                I18n.format(potion.getName()),
                potionEffect.getAmplifier(),
                potionEffect
        )));

        Collection<PotionInfo> sortedPotions = potions.stream().sorted(Comparator.comparing(potion -> renderer.getStringWidth(potion.formattedName(true)) * (component.sort_up ? -1 : 1))).collect(Collectors.toList());

        Function<Integer, Integer> xFunc;
        switch (component.getAlignment()) {
            case RIGHT:
                xFunc = i -> component.getWidth() - i;
                break;
            case CENTER:
                xFunc = i -> component.getWidth() / 2 - i / 2;
                break;
            case LEFT:
            default:
                xFunc = i -> 0;
                break;
        }


        for (PotionInfo potion : potions) {
            int color = potion.getPotionEffect().getPotion().getLiquidColor();
            ColourHolder ch = ColourConverter.intToRgb(color);
            String text = potion.formattedName(component.getAlignment().equals(AlignedComponent.Alignment.RIGHT));
            int textWidth = renderer.getStringWidth(text);
            int textHeight = renderer.getFontHeight() + 1;
            renderer.drawStringWithShadow(xFunc.apply(textWidth), y, ch.getR(), ch.getG(), ch.getB(), text);
            y += textHeight;
        }

        component.setHeight(2);

        GL11.glEnable(GL11.GL_CULL_FACE);
        glDisable(GL_BLEND);
        GlStateManager.popMatrix();

    }

    @Override
    public void handleSizeComponent(Potions component) {
        component.setWidth(100);
        component.setHeight(100);
    }
}
