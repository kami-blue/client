package me.zeroeightsix.kami.gui.rgui.poof.use;


import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.poof.IPoof;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;

import java.lang.reflect.ParameterizedType;

/**
 * Created by 086 on 21/07/2017.
 */
public abstract class Poof<T extends Component, S extends PoofInfo> implements IPoof<T, S> {

    private final Class<T> componentclass;
    private final Class<S> infoclass;

    public Poof() {
        this.componentclass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0];
        this.infoclass = (Class<S>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[1];
    }

    @Override
    public Class getComponentClass() {
        return componentclass;
    }

    @Override
    public Class<S> getInfoClass() {
        return infoclass;
    }
}
