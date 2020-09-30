package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.use.InputField;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.util.color.ColorHolder;
import me.zeroeightsix.kami.util.graphics.GlStateUtils;
import me.zeroeightsix.kami.util.graphics.RenderUtils2D;
import me.zeroeightsix.kami.util.graphics.VertexHelper;
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter;
import me.zeroeightsix.kami.util.math.Vec2d;

/**
 * Created by 086 on 30/06/2017.
 */
public class RootInputFieldUI<T extends InputField> extends AbstractComponentUI<InputField> {

    @Override
    public void renderComponent(InputField component) {
        ColorHolder colorFilled = new ColorHolder(84, 56, 56);
        ColorHolder colorOutline = new ColorHolder(255, 84, 84, 51);
        VertexHelper vertexHelper = new VertexHelper(GlStateUtils.useVbo());
        Vec2d pos1 = new Vec2d(0, 0);
        Vec2d pos2 = new Vec2d(component.getWidth(), component.getHeight());

        RenderUtils2D.drawRectFilled(vertexHelper, pos1, pos2, colorFilled);
        RenderUtils2D.drawRectOutline(vertexHelper, pos1, pos2, 1, colorOutline);
    }

    @Override
    public void handleAddComponent(InputField component, Container container) {
        component.setWidth(200);
        component.setHeight((int) FontRenderAdapter.INSTANCE.getFontHeight());
    }
}
