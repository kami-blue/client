package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Groupbox;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.util.color.ColorHolder;
import me.zeroeightsix.kami.util.graphics.GlStateUtils;
import me.zeroeightsix.kami.util.graphics.RenderUtils2D;
import me.zeroeightsix.kami.util.graphics.VertexHelper;
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter;
import me.zeroeightsix.kami.util.math.Vec2d;

/**
 * Created by 086 on 26/06/2017.
 */
public class RootGroupboxUI extends AbstractComponentUI<Groupbox> {

    @Override
    public void renderComponent(Groupbox component) {
        FontRenderAdapter.INSTANCE.drawString(component.getName(), 1, 1);

        VertexHelper vertexHelper = new VertexHelper(GlStateUtils.useVbo());
        RenderUtils2D.drawRectOutline(vertexHelper, new Vec2d(0, 0), new Vec2d(component.getWidth(), component.getHeight()), 1f, new ColorHolder(255, 0, 0));
    }

    @Override
    public void handleMouseDown(Groupbox component, int x, int y, int button) {
    }

    @Override
    public void handleAddComponent(Groupbox component, Container container) {
        component.setWidth(100);
        component.setHeight(100);
        component.setOriginOffsetY((int) (FontRenderAdapter.INSTANCE.getFontHeight() + 3f));
    }
}
