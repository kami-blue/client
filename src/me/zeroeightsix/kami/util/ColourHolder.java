// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util;

import java.awt.Color;
import org.lwjgl.opengl.GL11;

public class ColourHolder
{
    int r;
    int g;
    int b;
    int a;
    
    public ColourHolder(final int r, final int g, final int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 255;
    }
    
    public ColourHolder(final int r, final int g, final int b, final int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    
    public ColourHolder brighter() {
        return new ColourHolder(Math.min(this.r + 10, 255), Math.min(this.g + 10, 255), Math.min(this.b + 10, 255), this.getA());
    }
    
    public ColourHolder darker() {
        return new ColourHolder(Math.max(this.r - 10, 0), Math.max(this.g - 10, 0), Math.max(this.b - 10, 0), this.getA());
    }
    
    public void setGLColour() {
        this.setGLColour(-1, -1, -1, -1);
    }
    
    public void setGLColour(final int dr, final int dg, final int db, final int da) {
        GL11.glColor4f(((dr == -1) ? this.r : dr) / 255.0f, ((dg == -1) ? this.g : dg) / 255.0f, ((db == -1) ? this.b : db) / 255.0f, ((da == -1) ? this.a : da) / 255.0f);
    }
    
    public void becomeGLColour() {
    }
    
    public void becomeHex(final int hex) {
        this.setR((hex & 0xFF0000) >> 16);
        this.setG((hex & 0xFF00) >> 8);
        this.setB(hex & 0xFF);
        this.setA(255);
    }
    
    public static ColourHolder fromHex(final int hex) {
        final ColourHolder n = new ColourHolder(0, 0, 0);
        n.becomeHex(hex);
        return n;
    }
    
    public static int toHex(final int r, final int g, final int b) {
        return 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }
    
    public int toHex() {
        return toHex(this.r, this.g, this.b);
    }
    
    public int getB() {
        return this.b;
    }
    
    public int getG() {
        return this.g;
    }
    
    public int getR() {
        return this.r;
    }
    
    public int getA() {
        return this.a;
    }
    
    public ColourHolder setR(final int r) {
        this.r = r;
        return this;
    }
    
    public ColourHolder setB(final int b) {
        this.b = b;
        return this;
    }
    
    public ColourHolder setG(final int g) {
        this.g = g;
        return this;
    }
    
    public ColourHolder setA(final int a) {
        this.a = a;
        return this;
    }
    
    public ColourHolder clone() {
        return new ColourHolder(this.r, this.g, this.b, this.a);
    }
    
    public Color toJavaColour() {
        return new Color(this.r, this.g, this.b, this.a);
    }
}
