package me.zeroeightsix.kami.gui.rgui.component.container;

import me.zeroeightsix.kami.gui.rgui.component.AbstractComponent;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.listen.RenderListener;
import me.zeroeightsix.kami.gui.rgui.poof.use.AdditionPoof;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.module.modules.client.*;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * Created by 086 on 25/06/2017.
 */
public abstract class AbstractContainer extends AbstractComponent implements Container {

    protected ArrayList<Component> children = new ArrayList<>();

    int originoffsetX = 0;
    int originoffsetY = 0;

    public AbstractContainer(Theme theme) {
        setTheme(theme);
    }

    @Override
    public ArrayList<Component> getChildren() {
        return children;
    }

    /**
     *Changes the visibility of a child given the name
     * @param name The name of the child
     * @param visibility Whether the child is visible
     */
    public void setChildVisibilityByName(String name, boolean visibility)
    {
        for (Component c : children)
        {
            if (c.getName() == null) continue;
            if (c.getName().equals(name))
            {
                c.setVisible(visibility);
                return;
            }
        }
        throw new NoSuchElementException("The specified element was not found");
    }

    @Override
    public Container addChild(Component... components) {
        for (Component component : components) {
            if (!children.contains(component)) {
                component.setTheme(getTheme());
                component.setParent(this);
                component.getUI().handleAddComponent(component, this);
                component.getUI().handleSizeComponent(component);

                synchronized (children) {
                    children.add(component);
                    Collections.sort(children, new Comparator<Component>() {
                        @Override
                        public int compare(Component o1, Component o2) {
                            return o1.getPriority() - o2.getPriority();
                        }
                    });

                    component.callPoof(AdditionPoof.class, null);
                }
            }
        }
        return this;
    }

    @Override
    public Container removeChild(Component component) {
        if (children.contains(component))
            children.remove(component);
        return this;
    }

    @Override
    public boolean hasChild(Component component) {
        return children.contains(component);
    }

    @Override
    public void renderChildren() {
        for (Component c : getChildren()) {
            if (!c.isVisible()) continue;

            GL11.glPushMatrix();
            GL11.glTranslatef(c.getX(), c.getY(), 0);

            c.getRenderListeners().forEach(RenderListener::onPreRender);

            c.getUI().renderComponent(c, getTheme().getFontRenderer());
            if (c instanceof Container) {
                GL11.glTranslatef(((Container) c).getOriginOffsetX(), ((Container) c).getOriginOffsetY(), 0);
                ((Container) c).renderChildren();
                GL11.glTranslatef(-((Container) c).getOriginOffsetX(), -((Container) c).getOriginOffsetY(), 0);
            }

            c.getRenderListeners().forEach(RenderListener::onPostRender);

            GL11.glTranslatef(-c.getX(), -c.getY(), 0);
            GL11.glPopMatrix();
        }

    }

    @Override
    public Component getComponentAt(int x, int y) {
        for (int i = getChildren().size() - 1; i >= 0; i--) {
            Component c = getChildren().get(i);

            if (!c.isVisible()) continue;

            int componentX = c.getX() + getOriginOffsetX();
            int componentY = c.getY() + getOriginOffsetY();
            int componentWidth = c.getWidth();
            int componentHeight = c.getHeight();

            if (c instanceof Container) {
                Container container = (Container) c;
                boolean penetrate = container.penetrateTest(x - getOriginOffsetX(), y - getOriginOffsetY());

                if (!penetrate) continue;
                Component a = ((Container) c).getComponentAt(x - componentX, y - componentY);

                if (a != c) {
                    return a;
                }
            }

            if (x >= componentX && y >= componentY && x <= componentX + componentWidth && y <= componentY + componentHeight) {
                if (c instanceof Container) {
                    Container container = (Container) c;
                    Component hit = container.getComponentAt(x - componentX, y - componentY);
                    return hit;
                }
                return c;
            }
        }
        return this;
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width + getOriginOffsetX());
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height + getOriginOffsetY());
    }

    @Override
    public void kill() {
        for (Component c : children)
            c.kill();
        super.kill();
    }

    @Override
    public int getOriginOffsetX() {
        return originoffsetX;
    }

    @Override
    public int getOriginOffsetY() {
        return originoffsetY;
    }

    public void setOriginOffsetX(int originoffsetX) {
        this.originoffsetX = originoffsetX;
    }

    public void setOriginOffsetY(int originoffsetY) {
        this.originoffsetY = originoffsetY;
    }

    @Override
    public boolean penetrateTest(int x, int y) {
        return true;
    }
}
