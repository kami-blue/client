package me.zeroeightsix.kami.gui.rgui.component.listen;

import me.zeroeightsix.kami.gui.rgui.component.Component;

/**
 * Created by 086 on 3/08/2017.
 */
public interface UpdateListener<T extends Component> {
    void updateSize(T component, int oldWidth, int oldHeight);

    void updateLocation(T component, int oldX, int oldY);
}
