// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.rgui.component.Component;
import java.util.function.Function;
import me.zeroeightsix.kami.util.ColourUtils;
import java.awt.Color;
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.Comparator;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.Module;
import java.util.List;
import me.zeroeightsix.kami.util.Wrapper;
import org.lwjgl.opengl.GL11;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.setting.Setting;
import java.awt.Font;
import me.zeroeightsix.kami.gui.font.CFontRenderer;
import me.zeroeightsix.kami.gui.kami.component.ActiveModules;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;

public class KamiActiveModulesUI extends AbstractComponentUI<ActiveModules>
{
    CFontRenderer cFontRenderer;
    public boolean rainbowBG;
    public int redForBG;
    public int greenForBG;
    public int blueForBG;
    
    public KamiActiveModulesUI() {
        this.cFontRenderer = new CFontRenderer(new Font("Comic Sans MS", 0, 18), true, false);
    }
    
    private void checkSettingGuiColour(final Setting setting) {
        final String name2;
        final String name = name2 = setting.getName();
        switch (name2) {
            case "Red": {
                this.redForBG = setting.getValue();
                break;
            }
            case "Green": {
                this.greenForBG = setting.getValue();
                break;
            }
            case "Blue": {
                this.blueForBG = setting.getValue();
                break;
            }
        }
    }
    
    private void checkRainbowSetting(final Setting setting) {
        final String name2;
        final String name = name2 = setting.getName();
        switch (name2) {
            case "Rainbow": {
                this.rainbowBG = setting.getValue();
                break;
            }
        }
    }
    
    @Override
    public void renderComponent(final ActiveModules component, final FontRenderer f) {
        GL11.glDisable(2884);
        GL11.glEnable(3042);
        GL11.glEnable(3553);
        final FontRenderer renderer = Wrapper.getFontRenderer();
        final CFontRenderer cFontRenderer;
        String string;
        final StringBuilder sb;
        final List<Module> mods = ModuleManager.getModules().stream().filter(Module::isEnabled).sorted(Comparator.comparing(module -> {
            cFontRenderer = this.cFontRenderer;
            new StringBuilder().append(module.getName());
            if (module.getHudInfo() == null) {
                string = "";
            }
            else {
                string = module.getHudInfo() + " ";
            }
            return Integer.valueOf(cFontRenderer.getStringWidth(sb.append(string).toString()) * (component.sort_up ? -1 : 1));
        })).collect((Collector<? super Object, ?, List<Module>>)Collectors.toList());
        final int[] y = { 2 };
        if (component.getParent().getY() < 26 && Wrapper.getPlayer().func_70651_bq().size() > 0 && component.getParent().getOpacity() == 0.0f) {
            y[0] = Math.max(component.getParent().getY(), 26 - component.getParent().getY());
        }
        final float[] hue = { System.currentTimeMillis() % 11520L / 11520.0f };
        final boolean lAlign = component.getAlignment() == AlignedComponent.Alignment.LEFT;
        switch (component.getAlignment()) {
            case RIGHT: {
                final Function<Integer, Integer> xFunc = (Function<Integer, Integer>)(i -> component.getWidth() - i);
                break;
            }
            case CENTER: {
                final Function<Integer, Integer> xFunc = (Function<Integer, Integer>)(i -> component.getWidth() / 2 - i / 2);
                break;
            }
            default: {
                final Function<Integer, Integer> xFunc = (Function<Integer, Integer>)(i -> 0);
                break;
            }
        }
        final Object o;
        int rgb;
        String s;
        String string2;
        final StringBuilder sb2;
        String text;
        final FontRenderer fontRenderer;
        int textwidth;
        int textheight;
        int red;
        int green;
        int blue;
        int trgb;
        final Function<Integer, Integer> function;
        final Object o2;
        mods.stream().forEach(module -> {
            if (module.getShowOnArray().equals(Module.ShowOnArray.ON)) {
                ModuleManager.getModuleByName("Gui").settingList.forEach(setting -> this.checkSettingGuiColour(setting));
                ModuleManager.getModuleByName("Gui").settingList.forEach(setting -> this.checkRainbowSetting(setting));
                rgb = Color.HSBtoRGB(o[0], 1.0f, 1.0f);
                s = module.getHudInfo();
                new StringBuilder().append(module.getName());
                if (s == null) {
                    string2 = "";
                }
                else {
                    string2 = " §4" + s;
                }
                text = sb2.append(string2).toString();
                textwidth = fontRenderer.getStringWidth(text);
                textheight = fontRenderer.getFontHeight() + 1;
                red = (rgb >> 16 & 0xFF);
                green = (rgb >> 8 & 0xFF);
                blue = (rgb & 0xFF);
                trgb = ColourUtils.toRGBA(red, green, blue, 255);
                if (this.rainbowBG) {
                    this.cFontRenderer.drawStringWithShadow(text, function.apply(textwidth), (double)o2[0], trgb);
                }
                else {
                    this.cFontRenderer.drawStringWithShadow(text, function.apply(textwidth), (double)o2[0], ColourUtils.toRGBA(this.redForBG, this.greenForBG, this.blueForBG, 255));
                }
                o[0] += 0.02f;
                o2[0] += textheight;
            }
            return;
        });
        component.setHeight(y[0]);
        GL11.glEnable(2884);
        GL11.glDisable(3042);
    }
    
    @Override
    public void handleSizeComponent(final ActiveModules component) {
        component.setWidth(100);
        component.setHeight(100);
    }
}
