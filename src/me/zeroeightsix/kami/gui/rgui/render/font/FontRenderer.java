// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.rgui.render.font;

import java.awt.Color;

public interface FontRenderer
{
    int getFontHeight();
    
    int getStringHeight(final String p0);
    
    int getStringWidth(final String p0);
    
    void drawString(final int p0, final int p1, final String p2);
    
    void drawString(final int p0, final int p1, final int p2, final int p3, final int p4, final String p5);
    
    void drawString(final int p0, final int p1, final Color p2, final String p3);
    
    void drawString(final int p0, final int p1, final int p2, final String p3);
    
    void drawStringWithShadow(final int p0, final int p1, final int p2, final int p3, final int p4, final String p5);
}
