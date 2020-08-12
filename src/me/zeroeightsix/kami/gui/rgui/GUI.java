// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.rgui;

import me.zeroeightsix.kami.gui.rgui.component.listen.TickListener;
import me.zeroeightsix.kami.gui.rgui.util.Docking;
import org.lwjgl.opengl.Display;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import org.lwjgl.input.Mouse;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import org.lwjgl.input.Keyboard;
import java.util.Iterator;
import me.zeroeightsix.kami.gui.rgui.component.listen.KeyListener;
import java.util.ArrayList;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.AbstractContainer;

public abstract class GUI extends AbstractContainer
{
    Component focus;
    boolean press;
    int x;
    int y;
    int button;
    int mx;
    int my;
    long lastMS;
    
    public GUI(final Theme theme) {
        super(theme);
        this.focus = null;
        this.press = false;
        this.x = 0;
        this.y = 0;
        this.button = 0;
        this.mx = 0;
        this.my = 0;
        this.lastMS = System.currentTimeMillis();
    }
    
    public abstract void initializeGUI();
    
    public abstract void destroyGUI();
    
    public void updateGUI() {
        this.catchMouse();
        this.catchKey();
    }
    
    public void handleKeyDown(final int key) {
        if (this.focus == null) {
            return;
        }
        this.focus.getTheme().getUIForComponent(this.focus).handleKeyDown(this.focus, key);
        final ArrayList<Component> l = new ArrayList<Component>();
        for (Component p = this.focus; p != null; p = p.getParent()) {
            l.add(0, p);
        }
        final KeyListener.KeyEvent event = new KeyListener.KeyEvent(key);
        for (final Component a : l) {
            a.getKeyListeners().forEach(keyListener -> keyListener.onKeyDown(event));
        }
    }
    
    public void handleKeyUp(final int key) {
        if (this.focus == null) {
            return;
        }
        this.focus.getTheme().getUIForComponent(this.focus).handleKeyUp(this.focus, key);
        final ArrayList<Component> l = new ArrayList<Component>();
        for (Component p = this.focus; p != null; p = p.getParent()) {
            l.add(0, p);
        }
        final KeyListener.KeyEvent event = new KeyListener.KeyEvent(key);
        for (final Component a : l) {
            a.getKeyListeners().forEach(keyListener -> keyListener.onKeyUp(event));
        }
    }
    
    public void catchKey() {
        if (this.focus == null) {
            return;
        }
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                this.handleKeyDown(Keyboard.getEventKey());
            }
            else {
                this.handleKeyUp(Keyboard.getEventKey());
            }
        }
    }
    
    public void handleMouseDown(final int x, final int y) {
        final Component c = this.getComponentAt(x, y);
        final int[] real = calculateRealPosition(c);
        if (this.focus != null) {
            this.focus.setFocussed(false);
        }
        this.focus = c;
        if (!c.equals(this)) {
            Component upperParent;
            for (upperParent = c; !this.hasChild(upperParent); upperParent = upperParent.getParent()) {}
            this.children.remove(upperParent);
            this.children.add(upperParent);
            Collections.sort(this.children, new Comparator<Component>() {
                @Override
                public int compare(final Component o1, final Component o2) {
                    return o1.getPriority() - o2.getPriority();
                }
            });
        }
        this.focus.setFocussed(true);
        this.press = true;
        this.x = x;
        this.y = y;
        this.button = Mouse.getEventButton();
        this.getTheme().getUIForComponent(c).handleMouseDown(c, x - real[0], y - real[1], Mouse.getEventButton());
        final ArrayList<Component> l = new ArrayList<Component>();
        for (Component p = this.focus; p != null; p = p.getParent()) {
            l.add(0, p);
        }
        final int ex = x;
        final int ey = y;
        final MouseListener.MouseButtonEvent event = new MouseListener.MouseButtonEvent(ex, ey, this.button, this.focus);
        for (final Component a : l) {
            event.setX(event.getX() - a.getX());
            event.setY(event.getY() - a.getY());
            if (a instanceof Container) {
                event.setX(event.getX() - ((Container)a).getOriginOffsetX());
                event.setY(event.getY() - ((Container)a).getOriginOffsetY());
            }
            a.getMouseListeners().forEach(listener -> listener.onMouseDown(event));
            if (event.isCancelled()) {
                break;
            }
        }
    }
    
    public void handleMouseRelease(final int x, final int y) {
        final int button = Mouse.getEventButton();
        if (this.focus != null && button != -1) {
            final int[] real = calculateRealPosition(this.focus);
            this.getTheme().getUIForComponent(this.focus).handleMouseRelease(this.focus, x - real[0], y - real[1], button);
            final ArrayList<Component> l = new ArrayList<Component>();
            for (Component p = this.focus; p != null; p = p.getParent()) {
                l.add(0, p);
            }
            final int ex = x;
            final int ey = y;
            final MouseListener.MouseButtonEvent event = new MouseListener.MouseButtonEvent(ex, ey, button, this.focus);
            for (final Component a : l) {
                event.setX(event.getX() - a.getX());
                event.setY(event.getY() - a.getY());
                if (a instanceof Container) {
                    event.setX(event.getX() - ((Container)a).getOriginOffsetX());
                    event.setY(event.getY() - ((Container)a).getOriginOffsetY());
                }
                a.getMouseListeners().forEach(listener -> listener.onMouseRelease(event));
                if (event.isCancelled()) {
                    break;
                }
            }
            this.press = false;
            return;
        }
        if (button != -1) {
            final Component c = this.getComponentAt(x, y);
            final int[] real2 = calculateRealPosition(c);
            this.getTheme().getUIForComponent(c).handleMouseRelease(c, x - real2[0], y - real2[1], button);
            final ArrayList<Component> i = new ArrayList<Component>();
            for (Component p2 = c; p2 != null; p2 = p2.getParent()) {
                i.add(0, p2);
            }
            final int ex2 = x;
            final int ey2 = y;
            final MouseListener.MouseButtonEvent event2 = new MouseListener.MouseButtonEvent(ex2, ey2, button, c);
            for (final Component a2 : i) {
                event2.setX(event2.getX() - a2.getX());
                event2.setY(event2.getY() - a2.getY());
                if (a2 instanceof Container) {
                    event2.setX(event2.getX() - ((Container)a2).getOriginOffsetX());
                    event2.setY(event2.getY() - ((Container)a2).getOriginOffsetY());
                }
                a2.getMouseListeners().forEach(listener -> listener.onMouseRelease(event2));
                if (event2.isCancelled()) {
                    break;
                }
            }
            this.press = false;
        }
    }
    
    public void handleWheel(final int x, final int y, final int step) {
        final int intMouseMovement = step;
        if (intMouseMovement == 0) {
            return;
        }
        final Component c = this.getComponentAt(x, y);
        final int[] real = calculateRealPosition(c);
        this.getTheme().getUIForComponent(c).handleScroll(c, x - real[0], y - real[1], intMouseMovement, intMouseMovement > 0);
        final ArrayList<Component> l = new ArrayList<Component>();
        for (Component p = c; p != null; p = p.getParent()) {
            l.add(0, p);
        }
        final int ex = x;
        final int ey = y;
        final MouseListener.MouseScrollEvent event = new MouseListener.MouseScrollEvent(ex, ey, intMouseMovement > 0, c);
        for (final Component a : l) {
            event.setX(event.getX() - a.getX());
            event.setY(event.getY() - a.getY());
            if (a instanceof Container) {
                event.setX(event.getX() - ((Container)a).getOriginOffsetX());
                event.setY(event.getY() - ((Container)a).getOriginOffsetY());
            }
            a.getMouseListeners().forEach(listener -> listener.onScroll(event));
            if (event.isCancelled()) {
                break;
            }
        }
    }
    
    public void handleMouseDrag(final int x, final int y) {
        final int[] real = calculateRealPosition(this.focus);
        int ex = x - real[0];
        int ey = y - real[1];
        this.getTheme().getUIForComponent(this.focus).handleMouseDrag(this.focus, ex, ey, this.button);
        final ArrayList<Component> l = new ArrayList<Component>();
        for (Component p = this.focus; p != null; p = p.getParent()) {
            l.add(0, p);
        }
        ex = x;
        ey = y;
        final MouseListener.MouseButtonEvent event = new MouseListener.MouseButtonEvent(ex, ey, this.button, this.focus);
        for (final Component a : l) {
            event.setX(event.getX() - a.getX());
            event.setY(event.getY() - a.getY());
            if (a instanceof Container) {
                event.setX(event.getX() - ((Container)a).getOriginOffsetX());
                event.setY(event.getY() - ((Container)a).getOriginOffsetY());
            }
            a.getMouseListeners().forEach(listener -> listener.onMouseDrag(event));
            if (event.isCancelled()) {
                break;
            }
        }
    }
    
    private void catchMouse() {
        while (Mouse.next()) {
            final int x = Mouse.getX();
            int y = Mouse.getY();
            y = Display.getHeight() - y;
            if (this.press && this.focus != null && (this.x != x || this.y != y)) {
                this.handleMouseDrag(x, y);
            }
            if (Mouse.getEventButtonState()) {
                this.handleMouseDown(x, y);
            }
            else {
                this.handleMouseRelease(x, y);
            }
            if (Mouse.hasWheel()) {
                this.handleWheel(x, y, Mouse.getDWheel());
            }
        }
    }
    
    public void callTick(final Container container) {
        container.getTickListeners().forEach(tickListener -> tickListener.onTick());
        for (final Component c : container.getChildren()) {
            if (c instanceof Container) {
                this.callTick((Container)c);
            }
            else {
                c.getTickListeners().forEach(tickListener -> tickListener.onTick());
            }
        }
    }
    
    public void update() {
        if (System.currentTimeMillis() - this.lastMS > 50L) {
            this.callTick(this);
            this.lastMS = System.currentTimeMillis();
        }
    }
    
    public void drawGUI() {
        this.renderChildren();
    }
    
    public Component getFocus() {
        return this.focus;
    }
    
    public static int[] calculateRealPosition(final Component c) {
        int realX = c.getX();
        int realY = c.getY();
        if (c instanceof Container) {
            realX += ((Container)c).getOriginOffsetX();
            realY += ((Container)c).getOriginOffsetY();
        }
        for (Component parent = c.getParent(); parent != null; parent = parent.getParent()) {
            realX += parent.getX();
            realY += parent.getY();
            if (parent instanceof Container) {
                realX += ((Container)parent).getOriginOffsetX();
                realY += ((Container)parent).getOriginOffsetY();
            }
        }
        return new int[] { realX, realY };
    }
    
    public abstract String getTitle();
    
    public abstract boolean isPinned();
    
    public abstract boolean isMinimized();
    
    public abstract Docking getDocking();
}
