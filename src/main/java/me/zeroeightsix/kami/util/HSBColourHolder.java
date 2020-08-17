package me.zeroeightsix.kami.util;

import org.lwjgl.opengl.GL11;

import java.awt.Color;

import static me.zeroeightsix.kami.util.ColourConverter.toF;

/**
 * Created by Guac on 14/08/2020.
 * Adapted from Gebruiker's ColorHolder.
 */
public class HSBColourHolder {
    float h;
    float s;
    float b;
    float a;

    //All values range from 0f to 1f

    public HSBColourHolder(float h, float s, float b) {
        this.h = h;
        this.s = s;
        this.b = b;
        this.a = 1f;
    }

    public HSBColourHolder(float h, float s, float b, float a) {
        this.h = h;
        this.s = s;
        this.b = b;
        this.a = a;
    }

    public void setGLColour() {
        GL11.glColor4f(toF(getRed()), toF(getGreen()), toF(getBlue()), this.a);
    }
    //All get functions return rgba values, ex. 196
    public int getRed() {
        return Color.HSBtoRGB(this.h, this.s, this.b) >> 16 & 255;
    }

    public int getGreen() {
        return Color.HSBtoRGB(this.h, this.s, this.b) >> 8 & 255;
    }

    public int getBlue() {
        return Color.HSBtoRGB(this.h, this.s, this.b) >> 0 & 255;
    }

    public float getH() {
        return this.h;
    }

    public float getS() {
        return this.s;
    }

    public float getB() {
        return this.b;
    }

    public int getAlpha() {
        return (int)this.a * 255;
    }

    public HSBColourHolder setH(float h) {
        this.h = h;
        return this;
    }

    public HSBColourHolder setS(float s) {
        this.s = s;
        return this;
    }

    public HSBColourHolder setB(float b) {
        this.b = b;
        return this;
    }

    public HSBColourHolder setA(float a) {
        this.a = a;
        return this;
    }

    public HSBColourHolder getRaw() {
        return new HSBColourHolder(this.h, 1f, 1f);
    }

    public Color toJavaColour() {
        return Color.getHSBColor(this.a, this.s, this.b);
    }
    //Does not include the alpha value
}
