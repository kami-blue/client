// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami;

import java.util.Iterator;
import java.util.ArrayList;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.layout.Layout;

public class Stretcherlayout implements Layout
{
    public int COMPONENT_OFFSET_X;
    public int COMPONENT_OFFSET_Y;
    int blocks;
    int maxrows;
    
    public Stretcherlayout(final int blocks) {
        this.COMPONENT_OFFSET_X = 10;
        this.COMPONENT_OFFSET_Y = 4;
        this.maxrows = -1;
        this.blocks = blocks;
    }
    
    public Stretcherlayout(final int blocks, final int fixrows) {
        this.COMPONENT_OFFSET_X = 10;
        this.COMPONENT_OFFSET_Y = 4;
        this.maxrows = -1;
        this.blocks = blocks;
        this.maxrows = fixrows;
    }
    
    @Override
    public void organiseContainer(final Container container) {
        int width = 0;
        int height = 0;
        int i = 0;
        int w = 0;
        int h = 0;
        final ArrayList<Component> children = container.getChildren();
        for (final Component c : children) {
            if (!c.doAffectLayout()) {
                continue;
            }
            w += c.getWidth() + this.COMPONENT_OFFSET_X;
            h = Math.max(h, c.getHeight());
            if (++i < this.blocks) {
                continue;
            }
            width = Math.max(width, w);
            height += h + this.COMPONENT_OFFSET_Y;
            h = (w = (i = 0));
        }
        int x = 0;
        int y = 0;
        for (final Component c2 : children) {
            if (!c2.doAffectLayout()) {
                continue;
            }
            c2.setX(x + this.COMPONENT_OFFSET_X / 3);
            c2.setY(y + this.COMPONENT_OFFSET_Y / 3);
            h = Math.max(c2.getHeight(), h);
            x += width / this.blocks;
            if (x < width) {
                continue;
            }
            y += h + this.COMPONENT_OFFSET_Y;
            x = 0;
        }
        container.setWidth(width);
        container.setHeight(height);
        width -= this.COMPONENT_OFFSET_X;
        for (final Component c2 : children) {
            if (!c2.doAffectLayout()) {
                return;
            }
            c2.setWidth(width);
        }
    }
    
    public void setComponentOffsetWidth(final int componentOffset) {
        this.COMPONENT_OFFSET_X = componentOffset;
    }
    
    public void setComponentOffsetHeight(final int componentOffset) {
        this.COMPONENT_OFFSET_Y = componentOffset;
    }
}
