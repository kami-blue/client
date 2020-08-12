// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.rgui.component;

import java.util.Iterator;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen;
import me.zeroeightsix.kami.gui.rgui.GUI;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.gui.rgui.poof.IPoof;
import me.zeroeightsix.kami.gui.rgui.component.listen.TickListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.UpdateListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.KeyListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.RenderListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import java.util.ArrayList;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.gui.rgui.render.ComponentUI;
import me.zeroeightsix.kami.setting.Setting;

public abstract class AbstractComponent implements Component
{
    int x;
    int y;
    int width;
    int height;
    int minWidth;
    int minHeight;
    int maxWidth;
    int maxHeight;
    protected int priority;
    private Setting<Boolean> visible;
    float opacity;
    private boolean focus;
    ComponentUI ui;
    Theme theme;
    Container parent;
    boolean hover;
    boolean press;
    boolean drag;
    boolean affectlayout;
    ArrayList<MouseListener> mouseListeners;
    ArrayList<RenderListener> renderListeners;
    ArrayList<KeyListener> keyListeners;
    ArrayList<UpdateListener> updateListeners;
    ArrayList<TickListener> tickListeners;
    ArrayList<IPoof> poofs;
    boolean workingy;
    boolean workingx;
    
    public AbstractComponent() {
        this.minWidth = Integer.MIN_VALUE;
        this.minHeight = Integer.MIN_VALUE;
        this.maxWidth = Integer.MAX_VALUE;
        this.maxHeight = Integer.MAX_VALUE;
        this.priority = 0;
        this.visible = Settings.b("Visible", true);
        this.opacity = 1.0f;
        this.focus = false;
        this.hover = false;
        this.press = false;
        this.drag = false;
        this.affectlayout = true;
        this.mouseListeners = new ArrayList<MouseListener>();
        this.renderListeners = new ArrayList<RenderListener>();
        this.keyListeners = new ArrayList<KeyListener>();
        this.updateListeners = new ArrayList<UpdateListener>();
        this.tickListeners = new ArrayList<TickListener>();
        this.poofs = new ArrayList<IPoof>();
        this.workingy = false;
        this.workingx = false;
        this.addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(final MouseButtonEvent event) {
                AbstractComponent.this.press = true;
            }
            
            @Override
            public void onMouseRelease(final MouseButtonEvent event) {
                AbstractComponent.this.press = false;
                AbstractComponent.this.drag = false;
            }
            
            @Override
            public void onMouseDrag(final MouseButtonEvent event) {
                AbstractComponent.this.drag = true;
            }
            
            @Override
            public void onMouseMove(final MouseMoveEvent event) {
            }
            
            @Override
            public void onScroll(final MouseScrollEvent event) {
            }
        });
    }
    
    @Override
    public ComponentUI getUI() {
        if (this.ui == null) {
            this.ui = this.getTheme().getUIForComponent(this);
        }
        return this.ui;
    }
    
    @Override
    public Container getParent() {
        return this.parent;
    }
    
    @Override
    public void setParent(final Container parent) {
        this.parent = parent;
    }
    
    @Override
    public Theme getTheme() {
        return this.theme;
    }
    
    @Override
    public void setTheme(final Theme theme) {
        this.theme = theme;
    }
    
    @Override
    public void setFocussed(final boolean focus) {
        this.focus = focus;
    }
    
    @Override
    public boolean isFocussed() {
        return this.focus;
    }
    
    @Override
    public int getX() {
        return this.x;
    }
    
    @Override
    public int getY() {
        return this.y;
    }
    
    @Override
    public int getWidth() {
        return this.width;
    }
    
    @Override
    public int getHeight() {
        return this.height;
    }
    
    @Override
    public void setY(final int y) {
        final int oldX = this.getX();
        final int oldY = this.getY();
        this.y = y;
        if (!this.workingy) {
            this.workingy = true;
            this.getUpdateListeners().forEach(listener -> listener.updateLocation(this, oldX, oldY));
            if (this.getParent() != null) {
                this.getParent().getUpdateListeners().forEach(listener -> listener.updateLocation(this, oldX, oldY));
            }
            this.workingy = false;
        }
    }
    
    @Override
    public void setX(final int x) {
        final int oldX = this.getX();
        final int oldY = this.getY();
        this.x = x;
        if (!this.workingx) {
            this.workingx = true;
            this.getUpdateListeners().forEach(listener -> listener.updateLocation(this, oldX, oldY));
            if (this.getParent() != null) {
                this.getParent().getUpdateListeners().forEach(listener -> listener.updateLocation(this, oldX, oldY));
            }
            this.workingx = false;
        }
    }
    
    @Override
    public void setWidth(int width) {
        width = Math.max(this.getMinimumWidth(), Math.min(width, this.getMaximumWidth()));
        final int oldWidth = this.getWidth();
        final int oldHeight = this.getHeight();
        this.width = width;
        this.getUpdateListeners().forEach(listener -> listener.updateSize(this, oldWidth, oldHeight));
        if (this.getParent() != null) {
            this.getParent().getUpdateListeners().forEach(listener -> listener.updateSize(this, oldWidth, oldHeight));
        }
    }
    
    @Override
    public void setHeight(int height) {
        height = Math.max(this.getMinimumHeight(), Math.min(height, this.getMaximumHeight()));
        final int oldWidth = this.getWidth();
        final int oldHeight = this.getHeight();
        this.height = height;
        this.getUpdateListeners().forEach(listener -> listener.updateSize(this, oldWidth, oldHeight));
        if (this.getParent() != null) {
            this.getParent().getUpdateListeners().forEach(listener -> listener.updateSize(this, oldWidth, oldHeight));
        }
    }
    
    @Override
    public boolean isVisible() {
        return this.visible.getValue();
    }
    
    @Override
    public void setVisible(final boolean visible) {
        this.visible.setValue(visible);
    }
    
    @Override
    public int getPriority() {
        return this.priority;
    }
    
    @Override
    public void kill() {
        this.setVisible(false);
    }
    
    private boolean isMouseOver() {
        final int[] real = GUI.calculateRealPosition(this);
        final int mx = DisplayGuiScreen.mouseX;
        final int my = DisplayGuiScreen.mouseY;
        return real[0] <= mx && real[1] <= my && real[0] + this.getWidth() >= mx && real[1] + this.getHeight() >= my;
    }
    
    @Override
    public boolean isHovered() {
        return this.isMouseOver() && !this.press;
    }
    
    @Override
    public boolean isPressed() {
        return this.press;
    }
    
    @Override
    public ArrayList<MouseListener> getMouseListeners() {
        return this.mouseListeners;
    }
    
    @Override
    public void addMouseListener(final MouseListener listener) {
        if (!this.mouseListeners.contains(listener)) {
            this.mouseListeners.add(listener);
        }
    }
    
    @Override
    public ArrayList<RenderListener> getRenderListeners() {
        return this.renderListeners;
    }
    
    @Override
    public void addRenderListener(final RenderListener listener) {
        if (!this.renderListeners.contains(listener)) {
            this.renderListeners.add(listener);
        }
    }
    
    @Override
    public ArrayList<KeyListener> getKeyListeners() {
        return this.keyListeners;
    }
    
    @Override
    public void addKeyListener(final KeyListener listener) {
        if (!this.keyListeners.contains(listener)) {
            this.keyListeners.add(listener);
        }
    }
    
    @Override
    public ArrayList<UpdateListener> getUpdateListeners() {
        return this.updateListeners;
    }
    
    @Override
    public void addUpdateListener(final UpdateListener listener) {
        if (!this.updateListeners.contains(listener)) {
            this.updateListeners.add(listener);
        }
    }
    
    @Override
    public ArrayList<TickListener> getTickListeners() {
        return this.tickListeners;
    }
    
    @Override
    public void addTickListener(final TickListener listener) {
        if (!this.tickListeners.contains(listener)) {
            this.tickListeners.add(listener);
        }
    }
    
    @Override
    public void addPoof(final IPoof poof) {
        this.poofs.add(poof);
    }
    
    @Override
    public void callPoof(final Class<? extends IPoof> target, final PoofInfo info) {
        for (final IPoof poof : this.poofs) {
            if (target.isAssignableFrom(poof.getClass()) && poof.getComponentClass().isAssignableFrom(this.getClass())) {
                poof.execute(this, info);
            }
        }
    }
    
    @Override
    public boolean liesIn(final Component container) {
        if (container.equals(this)) {
            return true;
        }
        if (container instanceof Container) {
            for (final Component component : ((Container)container).getChildren()) {
                if (component.equals(this)) {
                    return true;
                }
                boolean liesin = false;
                if (component instanceof Container) {
                    liesin = this.liesIn(component);
                }
                if (liesin) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }
    
    @Override
    public float getOpacity() {
        return this.opacity;
    }
    
    @Override
    public void setOpacity(final float opacity) {
        this.opacity = opacity;
    }
    
    @Override
    public int getMaximumHeight() {
        return this.maxHeight;
    }
    
    @Override
    public int getMaximumWidth() {
        return this.maxWidth;
    }
    
    @Override
    public int getMinimumHeight() {
        return this.minHeight;
    }
    
    @Override
    public int getMinimumWidth() {
        return this.minWidth;
    }
    
    @Override
    public Component setMaximumWidth(final int width) {
        this.maxWidth = width;
        return this;
    }
    
    @Override
    public Component setMaximumHeight(final int height) {
        this.maxHeight = height;
        return this;
    }
    
    @Override
    public Component setMinimumWidth(final int width) {
        this.minWidth = width;
        return this;
    }
    
    @Override
    public Component setMinimumHeight(final int height) {
        this.minHeight = height;
        return this;
    }
    
    @Override
    public boolean doAffectLayout() {
        return this.affectlayout;
    }
    
    @Override
    public void setAffectLayout(final boolean flag) {
        this.affectlayout = flag;
    }
}
