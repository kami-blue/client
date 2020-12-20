package me.zeroeightsix.kami.gui.rgui.component;

import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen;
import me.zeroeightsix.kami.gui.rgui.GUI;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.listen.*;
import me.zeroeightsix.kami.gui.rgui.poof.IPoof;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.render.ComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

import java.util.ArrayList;

/**
 * Created by 086 on 25/06/2017.
 */
public abstract class AbstractComponent implements Component {

    int x;
    int y;
    int width;
    int height;

    int minWidth = Integer.MIN_VALUE;
    int minHeight = Integer.MIN_VALUE;
    int maxWidth = Integer.MAX_VALUE;
    int maxHeight = Integer.MAX_VALUE;

    protected int priority = 0;
    private final Setting<Boolean> visible = Settings.b("Visible", true);
    float opacity = 1f;
    private boolean focus = false;
    ComponentUI ui;
    Theme theme;
    Container parent;

    boolean hover = false;
    boolean press = false;
    boolean drag = false;

    boolean affectlayout = true;

    ArrayList<MouseListener> mouseListeners = new ArrayList<>();
    ArrayList<RenderListener> renderListeners = new ArrayList<>();
    ArrayList<KeyListener> keyListeners = new ArrayList<>();
    ArrayList<UpdateListener> updateListeners = new ArrayList<>();
    ArrayList<TickListener> tickListeners = new ArrayList<>();

    ArrayList<IPoof> poofs = new ArrayList<>();

    public AbstractComponent() {
        addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(MouseButtonEvent event) {
                press = true;
            }

            @Override
            public void onMouseRelease(MouseButtonEvent event) {
                press = false;
                drag = false;
            }

            @Override
            public void onMouseDrag(MouseButtonEvent event) {
                drag = true;
            }

            @Override
            public void onMouseMove(MouseMoveEvent event) {

            }

            @Override
            public void onScroll(MouseScrollEvent event) {

            }
        });
    }

    public ComponentUI getUI() {
        if (ui == null)
            ui = getTheme().getUIForComponent(this);
        return ui;
    }

    @Override
    public Container getParent() {
        return parent;
    }

    @Override
    public void setParent(Container parent) {
        this.parent = parent;
    }

    @Override
    public Theme getTheme() {
        return theme;
    }

    @Override
    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    @Override
    public void setFocused(boolean focus) {
        this.focus = focus;
    }

    @Override
    public boolean isFocused() {
        return focus;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    boolean workingy = false;

    public void setY(int y) {
        final int oldX = getX();
        final int oldY = getY();
        this.y = y;
        if (!workingy) {
            workingy = true;
            getUpdateListeners().forEach(listener -> listener.updateLocation(this, oldX, oldY)); // First call components own updatelisteners
            if (getParent() != null)
                getParent().getUpdateListeners().forEach(listener -> listener.updateLocation(this, oldX, oldY)); // And then notify the parent
            workingy = false;
        }
    }

    boolean workingx = false;

    public void setX(int x) {
        final int oldX = getX();
        final int oldY = getY();
        this.x = x;
        if (!workingx) {
            workingx = true;
            getUpdateListeners().forEach(listener -> listener.updateLocation(this, oldX, oldY)); // First call components own updatelisteners
            if (getParent() != null)
                getParent().getUpdateListeners().forEach(listener -> listener.updateLocation(this, oldX, oldY)); // And then notify the parent
            workingx = false;
        }
    }

    @Override
    public void setWidth(int width) {
        width = Math.max(getMinimumWidth(), Math.min(width, getMaximumWidth()));

        final int oldWidth = getWidth();
        final int oldHeight = getHeight();
        this.width = width;
        getUpdateListeners().forEach(listener -> listener.updateSize(this, oldWidth, oldHeight)); // First call components own updatelisteners
        if (getParent() != null)
            getParent().getUpdateListeners().forEach(listener -> listener.updateSize(this, oldWidth, oldHeight)); // And then notify the parent
    }

    @Override
    public void setHeight(int height) {
        height = Math.max(getMinimumHeight(), Math.min(height, getMaximumHeight()));

        final int oldWidth = getWidth();
        final int oldHeight = getHeight();
        this.height = height;
        getUpdateListeners().forEach(listener -> listener.updateSize(this, oldWidth, oldHeight)); // First call components own updatelisteners
        if (getParent() != null)
            getParent().getUpdateListeners().forEach(listener -> listener.updateSize(this, oldWidth, oldHeight)); // And then notify the parent
    }

    @Override
    public boolean isVisible() {
        return visible.getValue();
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible.setValue(visible);
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void kill() {
        setVisible(false);
    }

    private boolean isMouseOver() {
        int[] real = GUI.calculateRealPosition(this);
        int mx = DisplayGuiScreen.mouseX;
        int my = DisplayGuiScreen.mouseY;
        return real[0] <= mx && real[1] <= my && real[0] + getWidth() >= mx && real[1] + getHeight() >= my;
    }

    @Override
    public boolean isHovered() {
        return isMouseOver() && !press;
    }

    @Override
    public boolean isPressed() {
        return press;
    }

    @Override
    public ArrayList<MouseListener> getMouseListeners() {
        return mouseListeners;
    }

    @Override
    public void addMouseListener(MouseListener listener) {
        if (!mouseListeners.contains(listener))
            mouseListeners.add(listener);
    }

    @Override
    public ArrayList<RenderListener> getRenderListeners() {
        return renderListeners;
    }

    @Override
    public void addRenderListener(RenderListener listener) {
        if (!renderListeners.contains(listener))
            renderListeners.add(listener);
    }

    @Override
    public ArrayList<KeyListener> getKeyListeners() {
        return keyListeners;
    }

    @Override
    public void addKeyListener(KeyListener listener) {
        if (!keyListeners.contains(listener))
            keyListeners.add(listener);
    }

    @Override
    public ArrayList<UpdateListener> getUpdateListeners() {
        return updateListeners;
    }

    @Override
    public void addUpdateListener(UpdateListener listener) {
        if (!updateListeners.contains(listener))
            updateListeners.add(listener);
    }

    @Override
    public ArrayList<TickListener> getTickListeners() {
        return tickListeners;
    }

    @Override
    public void addTickListener(TickListener listener) {
        if (!tickListeners.contains(listener))
            tickListeners.add(listener);
    }

    @Override
    public void addPoof(IPoof poof) {
        poofs.add(poof);
    }

    @Override
    public void callPoof(Class<? extends IPoof> target, PoofInfo info) {
        for (IPoof poof : poofs) {
            if (target.isAssignableFrom(poof.getClass())) {
                if (poof.getComponentClass().isAssignableFrom(this.getClass()))
                    poof.execute(this, info);
            }
        }
    }

    @Override
    public boolean liesIn(Component container) {
        if (container.equals(this)) return true;
        if (container instanceof Container) {
            for (Component component : ((Container) container).getChildren()) {
                if (component.equals(this)) return true;

                boolean liesin = false;
                if (component instanceof Container)
                    liesin = liesIn(component);
                if (liesin) return true;
            }
            return false;
        }
        return false;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    @Override
    public int getMaximumHeight() {
        return maxHeight;
    }

    @Override
    public int getMaximumWidth() {
        return maxWidth;
    }

    @Override
    public int getMinimumHeight() {
        return minHeight;
    }

    @Override
    public int getMinimumWidth() {
        return minWidth;
    }

    @Override
    public Component setMaximumWidth(int width) {
        maxWidth = width;
        return this;
    }

    @Override
    public Component setMaximumHeight(int height) {
        maxHeight = height;
        return this;
    }

    @Override
    public Component setMinimumWidth(int width) {
        minWidth = width;
        return this;
    }

    @Override
    public Component setMinimumHeight(int height) {
        minHeight = height;
        return this;
    }

    @Override
    public boolean doAffectLayout() {
        return affectlayout;
    }

    @Override
    public void setAffectLayout(boolean flag) {
        affectlayout = flag;
    }

}
