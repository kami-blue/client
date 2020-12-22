package me.zeroeightsix.kami.gui.kami;

import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.layout.Layout;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by 086 on 27/06/2017.
 */
public class Stretcherlayout implements Layout {

    public int COMPONENT_OFFSET_X = 10;
    public int COMPONENT_OFFSET_Y = 4;

    int blocks;
    int maxrows = -1;

    public Stretcherlayout(int blocks) {
        this.blocks = blocks;
    }

    public Stretcherlayout(int blocks, int fixrows) {
        this.blocks = blocks;
        this.maxrows = fixrows;
    }

    @Override
    public void organiseContainer(Container container) {
        int width = 0;
        int height = 0;

        int i = 0;
        int w = 0;
        int h = 0;
        CopyOnWriteArrayList<Component> children = container.getChildren();
        for (Component c : children) {
            if (!c.doAffectLayout()) continue;

            w += c.getWidth() + COMPONENT_OFFSET_X;
            h = Math.max(h, c.getHeight());
            i++;
            if (i >= blocks) {
                width = Math.max(width, w);
                height += h + COMPONENT_OFFSET_Y;
                w = h = i = 0;
            }
        }

        int x = 0;
        int y = 0;
        for (Component c : children) {
            if (!c.doAffectLayout()) continue;

            c.setX(x + COMPONENT_OFFSET_X / 3);
            c.setY(y + COMPONENT_OFFSET_Y / 3);

            h = Math.max(c.getHeight(), h);

            x += width / blocks;
            if (x >= width) {
                y += h + COMPONENT_OFFSET_Y;
                x = 0;
            }
        }

        container.setWidth(width);
        container.setHeight(height);

        width -= COMPONENT_OFFSET_X;
        for (Component c : children) {
            if (!c.doAffectLayout()) return;
            c.setWidth(width);
        }
    }

    public void setComponentOffsetWidth(int componentOffset) {
        this.COMPONENT_OFFSET_X = componentOffset;
    }

    public void setComponentOffsetHeight(int componentOffset) {
        this.COMPONENT_OFFSET_Y = componentOffset;
    }
}
