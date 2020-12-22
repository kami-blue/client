package me.zeroeightsix.kami.gui.rgui.component.container;

import me.zeroeightsix.kami.gui.rgui.component.Component;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by 086 on 25/06/2017.
 */
public interface Container extends Component {
    CopyOnWriteArrayList<Component> getChildren();

    Component getComponentAt(int x, int y);

    Container addChild(Component... component);

    Container removeChild(Component component);

    boolean hasChild(Component component);

    void renderChildren();

    int getOriginOffsetX();

    int getOriginOffsetY();

    boolean penetrateTest(int x, int y);
}
