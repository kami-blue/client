package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.component.SettingsPanel;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.util.color.ColorHolder;
import me.zeroeightsix.kami.util.graphics.GlStateUtils;
import me.zeroeightsix.kami.util.graphics.RenderUtils2D;
import me.zeroeightsix.kami.util.graphics.VertexHelper;
import me.zeroeightsix.kami.util.math.Vec2d;

/**
 * Created by 086 on 6/08/2017.
 */
public class RootSettingsPanelUI extends AbstractComponentUI<SettingsPanel> {

    @Override
    public void renderComponent(SettingsPanel component) {
        ColorHolder colorFilled = new ColorHolder(36, 36, 36, (int) (component.getOpacity() * 255f));
        ColorHolder colorOutline = new ColorHolder(255, 84, 84, 51);
        VertexHelper vertexHelper = new VertexHelper(GlStateUtils.useVbo());
        Vec2d pos1 = new Vec2d(0, 0);
        Vec2d pos2 = new Vec2d(component.getWidth(), component.getHeight());

        RenderUtils2D.drawRoundedRectFilled(vertexHelper, pos1, pos2, 6, 8, colorFilled);
        RenderUtils2D.drawRoundedRectOutline(vertexHelper, pos1, pos2, 6, 8, 1, colorOutline);
    }

}
