package me.zeroeightsix.kami.gui.rgui;

import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.AbstractContainer;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.listen.KeyListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.util.Wrapper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by 086 on 25/06/2017.
 */
public abstract class GUI extends AbstractContainer {

    Component focus = null;

    boolean press = false;
    int x = 0;
    int y = 0;
    int button = 0;

    int mx = 0;
    int my = 0;

    public GUI(Theme theme) {
        super(theme);
    }

    public abstract void initializeGUI();

    public abstract void destroyGUI();

    public void updateGUI() {
        catchMouse();
        catchKey();
    }

    public void handleKeyDown(int key) {
        if (focus == null) return;
        focus.getTheme().getUIForComponent(focus).handleKeyDown(focus, key);

        ArrayList<Component> l = new ArrayList<>();
        Component p = focus;
        while (p != null) {
            l.add(0, p);
            p = p.getParent();
        }

        KeyListener.KeyEvent event = new KeyListener.KeyEvent(key);
        for (Component a : l) {
            a.getKeyListeners().forEach(keyListener -> {
                keyListener.onKeyDown(event);
            });
        }
    }

    public void handleKeyUp(int key) {
        if (focus == null) return;
        focus.getTheme().getUIForComponent(focus).handleKeyUp(focus, key);

        ArrayList<Component> l = new ArrayList<>();
        Component p = focus;
        while (p != null) {
            l.add(0, p);
            p = p.getParent();
        }

        KeyListener.KeyEvent event = new KeyListener.KeyEvent(key);
        for (Component a : l) {
            a.getKeyListeners().forEach(keyListener -> {
                keyListener.onKeyUp(event);
            });
        }
    }

    public void catchKey() {
        if (focus == null) return;
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                handleKeyDown(Keyboard.getEventKey());
            } else {
                handleKeyUp(Keyboard.getEventKey());
            }
        }
    }

    public void handleMouseDown(int x, int y) {
        Component c = getComponentAt(x, y);

        int[] real = calculateRealPosition(c);

        if (focus != null)
            focus.setFocused(false);
        focus = c;

        if (!c.equals(this)) {
            Component upperParent = c;
            while (!hasChild(upperParent))
                upperParent = upperParent.getParent();

            // Bring to front
            // Direct access to 'children' to avoid using remove and addChild, so it doesn't cause the componentUI handles to invoke
            children.remove(upperParent);
            children.add(upperParent);
            Collections.sort(children, new Comparator<Component>() {
                @Override
                public int compare(Component o1, Component o2) {
                    return o1.getPriority() - o2.getPriority();
                }
            });
        }


        focus.setFocused(true);

        press = true;
        this.x = x;
        this.y = y;
        button = Mouse.getEventButton();

        getTheme().getUIForComponent(c).handleMouseDown(c, x - real[0], y - real[1], Mouse.getEventButton());

        ArrayList<Component> l = new ArrayList<>();
        Component p = focus;
        while (p != null) {
            l.add(0, p);
            p = p.getParent();
        }
        int ex = x;
        int ey = y;
        MouseListener.MouseButtonEvent event = new MouseListener.MouseButtonEvent(ex, ey, button, focus);
        for (Component a : l) {

            event.setX(event.getX() - a.getX());
            event.setY(event.getY() - a.getY());

            if (a instanceof Container) {
                event.setX(event.getX() - ((Container) a).getOriginOffsetX());
                event.setY(event.getY() - ((Container) a).getOriginOffsetY());
            }

            a.getMouseListeners().forEach(listener -> {
                listener.onMouseDown(event);
            });

            if (event.isCancelled())
                break;
        }
    }

    public void handleMouseRelease(int x, int y) {
        int button = Mouse.getEventButton();
        if (focus != null && button != -1) {
            int[] real = calculateRealPosition(focus);
            getTheme().getUIForComponent(focus).handleMouseRelease(focus, x - real[0], y - real[1], button);

            ArrayList<Component> l = new ArrayList<>();
            Component p = focus;
            while (p != null) {
                l.add(0, p);
                p = p.getParent();
            }
            int ex = x;
            int ey = y;
            MouseListener.MouseButtonEvent event = new MouseListener.MouseButtonEvent(ex, ey, button, focus);
            for (Component a : l) {
                event.setX(event.getX() - a.getX());
                event.setY(event.getY() - a.getY());

                if (a instanceof Container) {
                    event.setX(event.getX() - ((Container) a).getOriginOffsetX());
                    event.setY(event.getY() - ((Container) a).getOriginOffsetY());
                }

                a.getMouseListeners().forEach(listener -> {
                    listener.onMouseRelease(event);
                });

                if (event.isCancelled())
                    break;
            }

            press = false;
            return;
        } else {
            if (button != -1) {
                Component c = getComponentAt(x, y);

                int[] real = calculateRealPosition(c);

                getTheme().getUIForComponent(c).handleMouseRelease(c, x - real[0], y - real[1], button);

                ArrayList<Component> l = new ArrayList<>();
                Component p = c;
                while (p != null) {
                    l.add(0, p);
                    p = p.getParent();
                }
                int ex = x;
                int ey = y;
                MouseListener.MouseButtonEvent event = new MouseListener.MouseButtonEvent(ex, ey, button, c);
                for (Component a : l) {
                    event.setX(event.getX() - a.getX());
                    event.setY(event.getY() - a.getY());

                    if (a instanceof Container) {
                        event.setX(event.getX() - ((Container) a).getOriginOffsetX());
                        event.setY(event.getY() - ((Container) a).getOriginOffsetY());
                    }

                    a.getMouseListeners().forEach(listener -> {
                        listener.onMouseRelease(event);
                    });

                    if (event.isCancelled())
                        break;
                }

                press = false;
            }
        }
    }

    public void handleWheel(int x, int y, int step) {
        //int intMouseMovement = Mouse.getDWheel();
        int intMouseMovement = step;
        if (intMouseMovement == 0) return;
        Component c = getComponentAt(x, y);

        int[] real = calculateRealPosition(c);

        getTheme().getUIForComponent(c).handleScroll(c, x - real[0], y - real[1], intMouseMovement, intMouseMovement > 0);

        ArrayList<Component> l = new ArrayList<>();
        Component p = c;
        while (p != null) {
            l.add(0, p);
            p = p.getParent();
        }
        int ex = x;
        int ey = y;
        MouseListener.MouseScrollEvent event = new MouseListener.MouseScrollEvent(ex, ey, intMouseMovement > 0, c);
        for (Component a : l) {
            event.setX(event.getX() - a.getX());
            event.setY(event.getY() - a.getY());

            if (a instanceof Container) {
                event.setX(event.getX() - ((Container) a).getOriginOffsetX());
                event.setY(event.getY() - ((Container) a).getOriginOffsetY());
            }

            a.getMouseListeners().forEach(listener -> {
                listener.onScroll(event);
            });

            if (event.isCancelled())
                break;
        }
    }

    public void handleMouseDrag(int x, int y) {
        int[] real = calculateRealPosition(focus);
        int ex = x - real[0];
        int ey = y - real[1];

        getTheme().getUIForComponent(focus).handleMouseDrag(focus, ex, ey, button);

        ArrayList<Component> l = new ArrayList<>();
        Component p = focus;
        while (p != null) {
            l.add(0, p);
            p = p.getParent();
        }

        ex = x;
        ey = y;

        MouseListener.MouseButtonEvent event = new MouseListener.MouseButtonEvent(ex, ey, button, focus);
        for (Component a : l) {
            event.setX(event.getX() - a.getX());
            event.setY(event.getY() - a.getY());

            if (a instanceof Container) {
                event.setX(event.getX() - ((Container) a).getOriginOffsetX());
                event.setY(event.getY() - ((Container) a).getOriginOffsetY());
            }

            a.getMouseListeners().forEach(listener -> {
                listener.onMouseDrag(event);
            });

            if (event.isCancelled())
                break;
        }
    }

    private void catchMouse() {
        while (Mouse.next()) {
            int x = Mouse.getX();
            int y = Mouse.getY();
            y = Display.getHeight() - y;

            if (press && focus != null && (this.x != x || this.y != y)) {
                handleMouseDrag(x, y);
            }

            if (Mouse.getEventButtonState()) {
                handleMouseDown(x, y);
            } else {
                handleMouseRelease(x, y);
            }

            if (Mouse.hasWheel()) {
                handleWheel(x, y, Mouse.getDWheel());
            }
        }
    }

    public void callTick(Container container) {
        if (Wrapper.getPlayer() == null || Wrapper.getWorld() == null) return;
        container.getTickListeners().forEach(tickListener -> tickListener.onTick());
        for (Component c : container.getChildren()) {
            if (c instanceof Container)
                callTick((Container) c);
            else
                c.getTickListeners().forEach(tickListener -> tickListener.onTick());
        }
    }

    long lastMS = System.currentTimeMillis();

    public void update() {
        if (System.currentTimeMillis() - lastMS > 1000 / 20) {
            callTick(this);
            lastMS = System.currentTimeMillis();
        }
    }

    public void drawGUI() {
        renderChildren();
    }

    public Component getFocus() {
        return focus;
    }

    public static int[] calculateRealPosition(Component c) {
        int realX = c.getX(), realY = c.getY();
        if (c instanceof Container) {
            realX += ((Container) c).getOriginOffsetX();
            realY += ((Container) c).getOriginOffsetY();
        }

        Component parent = c.getParent();
        while (parent != null) {
            realX += parent.getX();
            realY += parent.getY();
            if (parent instanceof Container) {
                realX += ((Container) parent).getOriginOffsetX();
                realY += ((Container) parent).getOriginOffsetY();
            }
            parent = parent.getParent();
        }

        return new int[]{
                realX,
                realY
        };
    }
}
