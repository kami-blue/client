// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.gui;

import me.zeroeightsix.kami.util.ColourUtils;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.gui.font.CFontRenderer;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Gui", category = Category.GUI, description = "Changes options with the gui")
public class Gui extends Module
{
    CFontRenderer cFontRenderer;
    public Setting<Boolean> Rainbow;
    public Setting<Boolean> watermark;
    public Setting<Boolean> RainbowWatermark;
    public Setting<Integer> Ared;
    public Setting<Integer> Agreen;
    public Setting<Integer> Ablue;
    public Setting<Float> Bred;
    public Setting<Float> Bgreen;
    public Setting<Float> Bblue;
    
    public Gui() {
        this.Rainbow = this.register(Settings.booleanBuilder("Rainbow").withValue(false).build());
        this.watermark = this.register(Settings.booleanBuilder("WaterMark").withValue(false).build());
        this.RainbowWatermark = this.register(Settings.booleanBuilder("Rainbow Watermark").withValue(false).build());
        this.Ared = this.register((Setting<Integer>)Settings.integerBuilder("Red").withRange(0, 255).withValue(0).build());
        this.Agreen = this.register((Setting<Integer>)Settings.integerBuilder("Green").withRange(0, 255).withValue(0).build());
        this.Ablue = this.register((Setting<Integer>)Settings.integerBuilder("Blue").withRange(0, 255).withValue(0).build());
        this.Bred = this.register((Setting<Float>)Settings.floatBuilder("Border Red").withRange(0.0f, 1.0f).withValue(0.0f).build());
        this.Bgreen = this.register((Setting<Float>)Settings.floatBuilder("Border Green").withRange(0.0f, 1.0f).withValue(0.0f).build());
        this.Bblue = this.register((Setting<Float>)Settings.floatBuilder("Border Blue").withRange(0.0f, 1.0f).withValue(1.0f).build());
    }
    
    @Override
    public void onRender() {
        final Minecraft mc = Minecraft.func_71410_x();
        final float[] hue = { System.currentTimeMillis() % 11520L / 11520.0f };
        final int rgb = Color.HSBtoRGB(hue[0], 1.0f, 1.0f);
        final String player = mc.field_71439_g.func_70005_c_();
        if (this.watermark.getValue()) {
            if (this.RainbowWatermark.getValue()) {
                this.cFontRenderer.drawStringWithShadow("Greetings " + player + "", 1.0, 10.0, rgb);
                this.cFontRenderer.drawStringWithShadow("AstraMod", 1.0, 1.0, rgb);
                final int n = 0;
                final float[] array = hue;
                final int n2 = 0;
                array[n2] += 0.02f;
            }
            else {
                this.cFontRenderer.drawStringWithShadow("Greetings " + player + "", 1.0, 10.0, ColourUtils.toRGBA(this.Bred.getValue(), this.Bgreen.getValue(), this.Bblue.getValue(), 0.0f));
                this.cFontRenderer.drawStringWithShadow("AstraMod", 1.0, 1.0, ColourUtils.toRGBA(this.Bred.getValue(), this.Bgreen.getValue(), this.Bblue.getValue(), 0.0f));
            }
        }
    }
    
    public int getArgb() {
        return ColourUtils.toRGBA(this.Ared.getValue(), this.Agreen.getValue(), this.Ablue.getValue(), 255);
    }
}
