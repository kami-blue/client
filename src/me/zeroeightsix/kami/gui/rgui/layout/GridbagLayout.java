// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.rgui.layout;

import java.util.Iterator;
import java.util.ArrayList;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;

public class GridbagLayout implements Layout
{
    private static final int COMPONENT_OFFSET = 10;
    int blocks;
    int maxrows;
    
    public GridbagLayout(final int blocks) {
        this.maxrows = -1;
        this.blocks = blocks;
    }
    
    public GridbagLayout(final int blocks, final int fixrows) {
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
            w += c.getWidth() + 10;
            h = Math.max(h, c.getHeight());
            if (++i < this.blocks) {
                continue;
            }
            width = Math.max(width, w);
            height += h + 10;
            h = (w = (i = 0));
        }
        int x = 0;
        int y = 0;
        for (final Component c2 : children) {
            if (!c2.doAffectLayout()) {
                continue;
            }
            c2.setX(x + 3);
            c2.setY(y + 3);
            h = Math.max(c2.getHeight(), h);
            x += width / this.blocks;
            if (x < width) {
                continue;
            }
            y += h + 10;
            x = 0;
        }
        container.setWidth(width);
        container.setHeight(height);
    }
}
