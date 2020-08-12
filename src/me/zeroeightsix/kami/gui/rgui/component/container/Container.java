// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.rgui.component.container;

import java.util.ArrayList;
import me.zeroeightsix.kami.gui.rgui.component.Component;

public interface Container extends Component
{
    ArrayList<Component> getChildren();
    
    Component getComponentAt(final int p0, final int p1);
    
    Container addChild(final Component... p0);
    
    Container removeChild(final Component p0);
    
    boolean hasChild(final Component p0);
    
    void renderChildren();
    
    int getOriginOffsetX();
    
    int getOriginOffsetY();
    
    boolean penetrateTest(final int p0, final int p1);
}
