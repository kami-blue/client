package me.zeroeightsix.kami.gui.rgui.layout;

import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by 086 on 26/06/2017.
 */
public class GridbagLayout implements Layout {

    private static final int COMPONENT_OFFSET = 10;

    int blocks;
    int maxrows = -1;

    public GridbagLayout(int blocks) {
        this.blocks = blocks;
    }

    public GridbagLayout(int blocks, int fixrows) {
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
            w += c.getWidth() + COMPONENT_OFFSET;
            h = Math.max(h, c.getHeight());
            i++;
            if (i >= blocks) {
                width = Math.max(width, w);
                height += h + COMPONENT_OFFSET;
                w = h = i = 0;
            }
        }

        int x = 0;
        int y = 0;
        for (Component c : children) {
            if (!c.doAffectLayout()) continue;
            c.setX(x + COMPONENT_OFFSET / 3);
            c.setY(y + COMPONENT_OFFSET / 3);

            h = Math.max(c.getHeight(), h);

            x += width / blocks;
            if (x >= width) {
                y += h + COMPONENT_OFFSET;
                x = 0;
            }
        }

        container.setWidth(width);
        container.setHeight(height);
    }
}
