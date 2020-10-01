package me.zeroeightsix.kami.gui.rgui.render.theme;

import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.render.ComponentUI;

/**
 * Created by 086 on 25/06/2017.
 */
public interface Theme {
    ComponentUI getUIForComponent(Component component);
}
