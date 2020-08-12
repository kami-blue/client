// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util;

import org.lwjgl.opengl.GL11;
import java.awt.Color;

public class HueCycler
{
    int index;
    int[] cycles;
    
    public HueCycler(final int cycles) {
        this.index = 0;
        if (cycles <= 0) {
            throw new IllegalArgumentException("cycles <= 0");
        }
        this.cycles = new int[cycles];
        double hue = 0.0;
        final double add = 1.0 / cycles;
        for (int i = 0; i < cycles; ++i) {
            this.cycles[i] = Color.HSBtoRGB((float)hue, 1.0f, 1.0f);
            hue += add;
        }
    }
    
    public void reset() {
        this.index = 0;
    }
    
    public void reset(final int index) {
        this.index = index;
    }
    
    public int next() {
        final int a = this.cycles[this.index];
        ++this.index;
        if (this.index >= this.cycles.length) {
            this.index = 0;
        }
        return a;
    }
    
    public void setNext() {
        final int rgb = this.next();
    }
    
    public void set() {
        final int rgb = this.cycles[this.index];
        final float red = (rgb >> 16 & 0xFF) / 255.0f;
        final float green = (rgb >> 8 & 0xFF) / 255.0f;
        final float blue = (rgb & 0xFF) / 255.0f;
        GL11.glColor3f(red, green, blue);
    }
    
    public void setNext(final float alpha) {
        final int rgb = this.next();
        final float red = (rgb >> 16 & 0xFF) / 255.0f;
        final float green = (rgb >> 8 & 0xFF) / 255.0f;
        final float blue = (rgb & 0xFF) / 255.0f;
        GL11.glColor4f(red, green, blue, alpha);
    }
    
    public int current() {
        return this.cycles[this.index];
    }
}
