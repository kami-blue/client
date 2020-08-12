// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.rgui.component.container;

import java.util.Iterator;
import me.zeroeightsix.kami.gui.rgui.component.listen.RenderListener;
import org.lwjgl.opengl.GL11;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.poof.IPoof;
import me.zeroeightsix.kami.gui.rgui.poof.use.AdditionPoof;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import java.util.ArrayList;
import me.zeroeightsix.kami.gui.rgui.component.AbstractComponent;

public abstract class AbstractContainer extends AbstractComponent implements Container
{
    protected ArrayList<Component> children;
    int originoffsetX;
    int originoffsetY;
    
    public AbstractContainer(final Theme theme) {
        this.children = new ArrayList<Component>();
        this.originoffsetX = 0;
        this.originoffsetY = 0;
        this.setTheme(theme);
    }
    
    @Override
    public ArrayList<Component> getChildren() {
        return this.children;
    }
    
    @Override
    public Container addChild(final Component... components) {
        for (final Component component : components) {
            if (!this.children.contains(component)) {
                component.setTheme(this.getTheme());
                component.setParent(this);
                component.getUI().handleAddComponent(component, this);
                component.getUI().handleSizeComponent(component);
                synchronized (this.children) {
                    this.children.add(component);
                    Collections.sort(this.children, new Comparator<Component>() {
                        @Override
                        public int compare(final Component o1, final Component o2) {
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
    public Container removeChild(final Component component) {
        this.children.remove(component);
        return this;
    }
    
    @Override
    public boolean hasChild(final Component component) {
        return this.children.contains(component);
    }
    
    @Override
    public void renderChildren() {
        for (final Component c : this.getChildren()) {
            if (!c.isVisible()) {
                continue;
            }
            GL11.glPushMatrix();
            GL11.glTranslatef((float)c.getX(), (float)c.getY(), 0.0f);
            c.getRenderListeners().forEach(RenderListener::onPreRender);
            c.getUI().renderComponent(c, this.getTheme().getFontRenderer());
            if (c instanceof Container) {
                GL11.glTranslatef((float)((Container)c).getOriginOffsetX(), (float)((Container)c).getOriginOffsetY(), 0.0f);
                ((Container)c).renderChildren();
                GL11.glTranslatef((float)(-((Container)c).getOriginOffsetX()), (float)(-((Container)c).getOriginOffsetY()), 0.0f);
            }
            c.getRenderListeners().forEach(RenderListener::onPostRender);
            GL11.glTranslatef((float)(-c.getX()), (float)(-c.getY()), 0.0f);
            GL11.glPopMatrix();
        }
    }
    
    @Override
    public Component getComponentAt(final int x, final int y) {
        for (int i = this.getChildren().size() - 1; i >= 0; --i) {
            final Component c = this.getChildren().get(i);
            if (c.isVisible()) {
                final int componentX = c.getX() + this.getOriginOffsetX();
                final int componentY = c.getY() + this.getOriginOffsetY();
                final int componentWidth = c.getWidth();
                final int componentHeight = c.getHeight();
                if (c instanceof Container) {
                    final Container container = (Container)c;
                    final boolean penetrate = container.penetrateTest(x - this.getOriginOffsetX(), y - this.getOriginOffsetY());
                    if (!penetrate) {
                        continue;
                    }
                    final Component a = ((Container)c).getComponentAt(x - componentX, y - componentY);
                    if (a != c) {
                        return a;
                    }
                }
                if (x >= componentX && y >= componentY && x <= componentX + componentWidth && y <= componentY + componentHeight) {
                    if (c instanceof Container) {
                        final Container container = (Container)c;
                        final Component hit = container.getComponentAt(x - componentX, y - componentY);
                        return hit;
                    }
                    return c;
                }
            }
        }
        return this;
    }
    
    @Override
    public void setWidth(final int width) {
        super.setWidth(width + this.getOriginOffsetX());
    }
    
    @Override
    public void setHeight(final int height) {
        super.setHeight(height + this.getOriginOffsetY());
    }
    
    @Override
    public void kill() {
        for (final Component c : this.children) {
            c.kill();
        }
        super.kill();
    }
    
    @Override
    public int getOriginOffsetX() {
        return this.originoffsetX;
    }
    
    @Override
    public int getOriginOffsetY() {
        return this.originoffsetY;
    }
    
    public void setOriginOffsetX(final int originoffsetX) {
        this.originoffsetX = originoffsetX;
    }
    
    public void setOriginOffsetY(final int originoffsetY) {
        this.originoffsetY = originoffsetY;
    }
    
    @Override
    public boolean penetrateTest(final int x, final int y) {
        return true;
    }
}
