// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.rgui.component.container.use;

import java.util.Iterator;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.listen.UpdateListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Display;
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.GUI;
import me.zeroeightsix.kami.gui.rgui.component.listen.RenderListener;
import me.zeroeightsix.kami.gui.rgui.layout.Layout;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.gui.rgui.component.container.OrganisedContainer;

public class Scrollpane extends OrganisedContainer
{
    int scrolledX;
    int maxScrollX;
    int scrolledY;
    int maxScrollY;
    boolean doScrollX;
    boolean doScrollY;
    boolean canScrollX;
    boolean canScrollY;
    boolean lockWidth;
    boolean lockHeight;
    int step;
    
    public Scrollpane(final Theme theme, final Layout layout, final int width, final int height) {
        super(theme, layout);
        this.doScrollX = false;
        this.doScrollY = true;
        this.canScrollX = false;
        this.canScrollY = false;
        this.lockWidth = false;
        this.lockHeight = false;
        this.step = 22;
        this.setWidth(width);
        this.setHeight(height);
        this.scrolledX = 0;
        this.scrolledY = 0;
        this.addRenderListener(new RenderListener() {
            int translatex;
            int translatey;
            
            @Override
            public void onPreRender() {
                this.translatex = Scrollpane.this.scrolledX;
                this.translatey = Scrollpane.this.scrolledY;
                final int[] real = GUI.calculateRealPosition(Scrollpane.this);
                final int scale = DisplayGuiScreen.getScale();
                GL11.glScissor(Scrollpane.this.getX() * scale + real[0] * scale - Scrollpane.this.getParent().getOriginOffsetX() - 1, Display.getHeight() - Scrollpane.this.getHeight() * scale - real[1] * scale - 1, Scrollpane.this.getWidth() * scale + Scrollpane.this.getParent().getOriginOffsetX() * scale + 1, Scrollpane.this.getHeight() * scale + 1);
                GL11.glEnable(3089);
            }
            
            @Override
            public void onPostRender() {
                GL11.glDisable(3089);
            }
        });
        this.addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(final MouseButtonEvent event) {
                if (event.getY() > Scrollpane.this.getHeight() || event.getX() > Scrollpane.this.getWidth() || event.getX() < 0 || event.getY() < 0) {
                    event.cancel();
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
                if (Scrollpane.this.canScrollY() && (!Scrollpane.this.canScrollX() || Scrollpane.this.scrolledX == 0 || !Scrollpane.this.isDoScrollX()) && Scrollpane.this.isDoScrollY()) {
                    if (event.isUp() && Scrollpane.this.getScrolledY() > 0) {
                        Scrollpane.this.setScrolledY(Math.max(0, Scrollpane.this.getScrolledY() - Scrollpane.this.step));
                        return;
                    }
                    if (!event.isUp() && Scrollpane.this.getScrolledY() < Scrollpane.this.getMaxScrollY()) {
                        Scrollpane.this.setScrolledY(Math.min(Scrollpane.this.getMaxScrollY(), Scrollpane.this.getScrolledY() + Scrollpane.this.step));
                        return;
                    }
                }
                if (Scrollpane.this.canScrollX() && Scrollpane.this.isDoScrollX()) {
                    if (event.isUp() && Scrollpane.this.getScrolledX() > 0) {
                        Scrollpane.this.setScrolledX(Math.max(0, Scrollpane.this.getScrolledX() - Scrollpane.this.step));
                        return;
                    }
                    if (!event.isUp() && Scrollpane.this.getScrolledX() < Scrollpane.this.getMaxScrollX()) {
                        Scrollpane.this.setScrolledX(Math.min(Scrollpane.this.getMaxScrollX(), Scrollpane.this.getScrolledX() + Scrollpane.this.step));
                    }
                }
            }
        });
        this.addUpdateListener(new UpdateListener() {
            @Override
            public void updateSize(final Component component, final int oldWidth, final int oldHeight) {
                Scrollpane.this.performCalculations();
            }
            
            @Override
            public void updateLocation(final Component component, final int oldX, final int oldY) {
                Scrollpane.this.performCalculations();
            }
        });
    }
    
    @Override
    public void setWidth(final int width) {
        if (!this.lockWidth) {
            super.setWidth(width);
        }
    }
    
    @Override
    public void setHeight(final int height) {
        if (!this.lockHeight) {
            super.setHeight(height);
        }
    }
    
    @Override
    public Container addChild(final Component... component) {
        super.addChild(component);
        this.performCalculations();
        return this;
    }
    
    private void performCalculations() {
        int farX = 0;
        int farY = 0;
        for (final Component c : this.getChildren()) {
            farX = Math.max(this.getScrolledX() + c.getX() + c.getWidth(), farX);
            farY = Math.max(this.getScrolledY() + c.getY() + c.getHeight(), farY);
        }
        this.canScrollX = (farX > this.getWidth());
        this.maxScrollX = farX - this.getWidth();
        this.canScrollY = (farY > this.getHeight());
        this.maxScrollY = farY - this.getHeight();
    }
    
    public boolean canScrollX() {
        return this.canScrollX;
    }
    
    public boolean canScrollY() {
        return this.canScrollY;
    }
    
    public boolean isDoScrollX() {
        return this.doScrollX;
    }
    
    public boolean isDoScrollY() {
        return this.doScrollY;
    }
    
    public void setDoScrollY(final boolean doScrollY) {
        this.doScrollY = doScrollY;
    }
    
    public void setDoScrollX(final boolean doScrollX) {
        this.doScrollX = doScrollX;
    }
    
    public void setScrolledX(final int scrolledX) {
        final int a = this.getScrolledX();
        this.scrolledX = scrolledX;
        final int dif = this.getScrolledX() - a;
        for (final Component component : this.getChildren()) {
            component.setX(component.getX() - dif);
        }
    }
    
    public void setScrolledY(final int scrolledY) {
        final int a = this.getScrolledY();
        this.scrolledY = scrolledY;
        final int dif = this.getScrolledY() - a;
        for (final Component component : this.getChildren()) {
            component.setY(component.getY() - dif);
        }
    }
    
    public int getScrolledX() {
        return this.scrolledX;
    }
    
    public int getScrolledY() {
        return this.scrolledY;
    }
    
    public int getMaxScrollX() {
        return this.maxScrollX;
    }
    
    public int getMaxScrollY() {
        return this.maxScrollY;
    }
    
    public Scrollpane setLockHeight(final boolean lockHeight) {
        this.lockHeight = lockHeight;
        return this;
    }
    
    public Scrollpane setLockWidth(final boolean lockWidth) {
        this.lockWidth = lockWidth;
        return this;
    }
    
    @Override
    public boolean penetrateTest(final int x, final int y) {
        return x > 0 && x < this.getWidth() && y > 0 && y < this.getHeight();
    }
}
