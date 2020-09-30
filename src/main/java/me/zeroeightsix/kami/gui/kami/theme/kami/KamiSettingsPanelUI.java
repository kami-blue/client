package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.component.SettingsPanel;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.util.color.ColorHolder;
import me.zeroeightsix.kami.util.graphics.GlStateUtils;
import me.zeroeightsix.kami.util.graphics.RenderUtils2D;
import me.zeroeightsix.kami.util.graphics.VertexHelper;
import me.zeroeightsix.kami.util.math.Vec2d;

/**
 * Created by 086 on 16/12/2017.
 */
public class KamiSettingsPanelUI extends AbstractComponentUI<SettingsPanel> {

    @Override
    public void renderComponent(SettingsPanel component) {
        super.renderComponent(component);

        VertexHelper vertexHelper = new VertexHelper(GlStateUtils.useVbo());
        Vec2d pos1 = new Vec2d(0, 0);
        Vec2d pos2 = new Vec2d(component.getWidth(), component.getHeight());

        RenderUtils2D.drawRectFilled(vertexHelper, pos1, pos2, new ColorHolder(KamiGuiColors.GuiC.windowFilled.color));
        RenderUtils2D.drawRectOutline(vertexHelper, pos1, pos2, 1.5f, new ColorHolder(KamiGuiColors.GuiC.windowOutline.color));
    }
}
