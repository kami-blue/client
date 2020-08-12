// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.rgui.component.use;

import me.zeroeightsix.kami.gui.rgui.poof.use.Poof;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.poof.IPoof;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;

public class CheckButton extends Button
{
    boolean toggled;
    
    public CheckButton(final String name) {
        this(name, 0, 0);
    }
    
    public CheckButton(final String name, final int x, final int y) {
        super(name, x, y);
        this.addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(final MouseButtonEvent event) {
                if (event.getButton() != 0) {
                    return;
                }
                CheckButton.this.toggled = !CheckButton.this.toggled;
                CheckButton.this.callPoof(CheckButtonPoof.class, new CheckButtonPoof.CheckButtonPoofInfo(CheckButtonPoof.CheckButtonPoofInfo.CheckButtonPoofInfoAction.TOGGLE));
                if (CheckButton.this.toggled) {
                    CheckButton.this.callPoof(CheckButtonPoof.class, new CheckButtonPoof.CheckButtonPoofInfo(CheckButtonPoof.CheckButtonPoofInfo.CheckButtonPoofInfoAction.ENABLE));
                }
                else {
                    CheckButton.this.callPoof(CheckButtonPoof.class, new CheckButtonPoof.CheckButtonPoofInfo(CheckButtonPoof.CheckButtonPoofInfo.CheckButtonPoofInfoAction.DISABLE));
                }
            }
            
            @Override
            public void onMouseRelease(final MouseButtonEvent event) {
            }
            
            @Override
            public void onMouseDrag(final MouseButtonEvent event) {
            }
            
            @Override
            public void onMouseMove(final MouseMoveEvent event) {
            }
            
            @Override
            public void onScroll(final MouseScrollEvent event) {
            }
        });
    }
    
    public void setToggled(final boolean toggled) {
        this.toggled = toggled;
    }
    
    public boolean isToggled() {
        return this.toggled;
    }
    
    public abstract static class CheckButtonPoof<T extends CheckButton, S extends CheckButtonPoofInfo> extends Poof<T, S>
    {
        CheckButtonPoofInfo info;
        
        public static class CheckButtonPoofInfo extends PoofInfo
        {
            CheckButtonPoofInfoAction action;
            
            public CheckButtonPoofInfo(final CheckButtonPoofInfoAction action) {
                this.action = action;
            }
            
            public CheckButtonPoofInfoAction getAction() {
                return this.action;
            }
            
            public enum CheckButtonPoofInfoAction
            {
                TOGGLE, 
                ENABLE, 
                DISABLE;
            }
        }
    }
}
