package me.zeroeightsix.kami.gui.rgui.poof;

import me.zeroeightsix.kami.gui.rgui.component.Component;

/**
 * Created by 086 on 21/07/2017.
 */
public interface IPoof<T extends Component, S extends PoofInfo> {
    void execute(T component, S info);

    Class getComponentClass();

    Class getInfoClass();
}
