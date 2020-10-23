package me.zeroeightsix.kami.gui.kami.theme.kami;

import kotlin.Pair;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.client.ActiveModules;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.util.color.ColorGradient;
import me.zeroeightsix.kami.util.color.ColorHolder;
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static me.zeroeightsix.kami.util.color.ColorConverter.toF;

/**
 * Created by 086 on 4/08/2017.
 * Updated by l1ving on 20/03/19
 */
public class KamiActiveModulesUI extends AbstractComponentUI<me.zeroeightsix.kami.gui.kami.component.ActiveModules> {

    final ColorGradient transRights = new ColorGradient(
            new Pair<>(0f, new ColorHolder(91, 207, 250)), new Pair<>(19.9999999999f, new ColorHolder(91, 207, 250)),
            new Pair<>(20f, new ColorHolder(245, 170, 185)), new Pair<>(39.9999999999f, new ColorHolder(245, 170, 185)),
            new Pair<>(40f, new ColorHolder(255, 255, 255)), new Pair<>(59.9999999999f, new ColorHolder(255, 255, 255)),
            new Pair<>(60f, new ColorHolder(245, 170, 185)), new Pair<>(79.9999999999f, new ColorHolder(245, 170, 185)),
            new Pair<>(80f, new ColorHolder(91, 207, 250)), new Pair<>(100f, new ColorHolder(91, 207, 250))
    );

    @Override
    public void renderComponent(me.zeroeightsix.kami.gui.kami.component.ActiveModules component) {

        List<Module> modules = ModuleManager.getModules().stream()
                .filter(module -> module.isEnabled() && (ActiveModules.INSTANCE.getHidden().getValue() || module.isOnArray()))
                .sorted(Comparator.comparing(module -> FontRenderAdapter.INSTANCE.getStringWidth(module.getName().getValue() + (module.getHudInfo() == null ? "" : module.getHudInfo() + " ")) * (component.sort_up ? -1 : 1)))
                .collect(Collectors.toList());


        final int[] y = {2};

        if (Wrapper.getPlayer() != null) {
            if (ActiveModules.INSTANCE.getPotion().getValue() && component.getParent().getY() < 26 && Wrapper.getPlayer().getActivePotionEffects().size() > 0 && component.getParent().getOpacity() == 0)
                y[0] = Math.max(component.getParent().getY(), 26 - component.getParent().getY());
        }

        final float[] hue = {(System.currentTimeMillis() % (360 * ActiveModules.INSTANCE.getRainbowSpeed())) / (360f * ActiveModules.INSTANCE.getRainbowSpeed())};

        Function<Float, Float> xFunc;
        switch (component.getAlignment()) {
            case RIGHT:
                xFunc = i -> component.getWidth() - i;
                break;
            case CENTER:
                xFunc = i -> component.getWidth() / 2 - i / 2;
                break;
            case LEFT:
            default:
                xFunc = i -> 0f;
                break;
        }

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int rgb;

            switch (ActiveModules.INSTANCE.getMode().getValue()) {
                case RAINBOW:
                    rgb = Color.HSBtoRGB(hue[0], toF(ActiveModules.INSTANCE.getSaturationR().getValue()), toF(ActiveModules.INSTANCE.getBrightnessR().getValue()));
                    break;
                case CATEGORY:
                    rgb = ActiveModules.INSTANCE.getCategoryColour(module);
                    break;
                case CUSTOM:
                    rgb = Color.HSBtoRGB(toF(ActiveModules.INSTANCE.getHueC().getValue()), toF(ActiveModules.INSTANCE.getSaturationC().getValue()), toF(ActiveModules.INSTANCE.getBrightnessC().getValue()));
                    break;
                case INFO_OVERLAY:
                    rgb = ActiveModules.INSTANCE.getInfoColour(i);
                    break;
                case TRANS_RIGHTS:
                    float value = ((float) i + 1.0f) / (float) modules.size();
                    rgb = transRights.get(value * 100f).toHex();
                    break;
                default:
                    rgb = 0;
            }

            String hudInfo = module.getHudInfo();
            String text = ActiveModules.INSTANCE.getAlignedText(module.getName().getValue(), (hudInfo == null ? "" : KamiMod.color + "7" + hudInfo + KamiMod.color + "r"), component.getAlignment().equals(AlignedComponent.Alignment.RIGHT));
            float textWidth = FontRenderAdapter.INSTANCE.getStringWidth(text);
            float textHeight = FontRenderAdapter.INSTANCE.getFontHeight() + 1;
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;

            FontRenderAdapter.INSTANCE.drawString(text, xFunc.apply(textWidth), y[0], true, new ColorHolder(red, green, blue));
            hue[0] += .02f;
            y[0] += textHeight;
        }

        component.setHeight(y[0]);
    }

    @Override
    public void handleSizeComponent(me.zeroeightsix.kami.gui.kami.component.ActiveModules component) {
        component.setWidth(100);
        component.setHeight(100);
    }
}
