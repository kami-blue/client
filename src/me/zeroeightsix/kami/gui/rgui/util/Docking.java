// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.rgui.util;

public enum Docking
{
    TOPLEFT(true, true, false, false), 
    TOP(true, false, false, false), 
    TOPRIGHT(true, false, false, true), 
    RIGHT(false, false, false, true), 
    BOTTOMRIGHT(false, false, true, true), 
    BOTTOM(false, false, true, false), 
    BOTTOMLEFT(false, true, true, false), 
    LEFT(false, true, false, false), 
    CENTER(true, true, true, true), 
    NONE(false, false, false, false), 
    CENTERTOP(true, true, false, true), 
    CENTERBOTTOM(false, false, true, false), 
    CENTERVERTICAL(false, true, false, true), 
    CENTERHOIZONTAL(true, false, true, false), 
    CENTERLEFT(true, true, true, false), 
    CENTERRIGHT(true, false, true, true);
    
    boolean isTop;
    boolean isLeft;
    boolean isBottom;
    boolean isRight;
    
    private Docking(final boolean isTop, final boolean isLeft, final boolean isBottom, final boolean isRight) {
        this.isTop = isTop;
        this.isLeft = isLeft;
        this.isBottom = isBottom;
        this.isRight = isRight;
    }
    
    public boolean isBottom() {
        return this.isBottom && !this.isTop;
    }
    
    public boolean isLeft() {
        return this.isLeft && !this.isRight;
    }
    
    public boolean isRight() {
        return this.isRight && !this.isLeft;
    }
    
    public boolean isTop() {
        return this.isTop && !this.isBottom;
    }
    
    public boolean isCenterHorizontal() {
        return this.isLeft && this.isRight;
    }
    
    public boolean isCenterVertical() {
        return this.isTop && this.isBottom;
    }
}
