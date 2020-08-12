// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util;

public class HudComponent
{
    private float x;
    private float y;
    private float w;
    private float h;
    private String name;
    private boolean visible;
    
    public HudComponent() {
    }
    
    public HudComponent(final float x, final float y, final float w, final float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
    
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
    }
    
    public void mouseClickMove(final int mouseX, final int mouseY, final int button) {
    }
    
    public void mouseClick(final int mouseX, final int mouseY, final int button) {
    }
    
    public void mouseRelease(final int mouseX, final int mouseY, final int button) {
    }
    
    public boolean collidesWith(final HudComponent other) {
        final boolean collisionX = this.x + this.w > other.x && other.x + other.w > this.x;
        final boolean collisionY = this.y + this.h > other.y && other.y + other.h > this.y;
        return collisionX && collisionY;
    }
    
    public float getX() {
        return this.x;
    }
    
    public void setX(final float x) {
        this.x = x;
    }
    
    public float getY() {
        return this.y;
    }
    
    public void setY(final float y) {
        this.y = y;
    }
    
    public float getW() {
        return this.w;
    }
    
    public void setW(final float w) {
        this.w = w;
    }
    
    public float getH() {
        return this.h;
    }
    
    public void setH(final float h) {
        this.h = h;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public boolean isVisible() {
        return this.visible;
    }
    
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }
}
