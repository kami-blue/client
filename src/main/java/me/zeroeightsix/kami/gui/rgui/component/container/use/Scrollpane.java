package me.zeroeightsix.kami.gui.rgui.component.container.use;

import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen;
import me.zeroeightsix.kami.gui.rgui.GUI;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.container.OrganisedContainer;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.RenderListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.UpdateListener;
import me.zeroeightsix.kami.gui.rgui.layout.Layout;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

/**
 * Created by 086 on 27/06/2017.
 */
public class Scrollpane extends OrganisedContainer {

    int scrolledX;
    int maxScrollX;
    int scrolledY;
    int maxScrollY;

    boolean doScrollX = false;
    boolean doScrollY = true;

    boolean canScrollX = false;
    boolean canScrollY = false;

    boolean lockWidth = false;
    boolean lockHeight = false;

    int step = 22;

    public Scrollpane(Theme theme, Layout layout, int width, int height) {
        super(theme, layout);

        setWidth(width);
        setHeight(height);

        scrolledX = 0;
        scrolledY = 0;

        addRenderListener(new RenderListener() {
            int translatex;
            int translatey;

            @Override
            public void onPreRender() {
                translatex = scrolledX;
                translatey = scrolledY;
                int[] real = GUI.calculateRealPosition(Scrollpane.this);
                double scale = DisplayGuiScreen.getScale();
                GL11.glScissor(
                        (int) (getX() * scale + real[0] * scale - getParent().getOriginOffsetX() - 1),
                        (int) (Display.getHeight() - getHeight() * scale - real[1] * scale - 1),
                        (int) (getWidth() * scale + getParent().getOriginOffsetX() * scale + 1),
                        (int) (getHeight() * scale + 1));
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }

            @Override
            public void onPostRender() {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(MouseButtonEvent event) {
                if (event.getY() > getHeight() || event.getX() > getWidth() || event.getX() < 0 || event.getY() < 0) {
                    event.cancel();
                }
            }

            @Override
            public void onMouseRelease(MouseButtonEvent event) {

            }

            @Override
            public void onMouseDrag(MouseButtonEvent event) {

            }

            @Override
            public void onMouseMove(MouseMoveEvent event) {

            }

            @Override
            public void onScroll(MouseScrollEvent event) {
                if (canScrollY() && !(canScrollX() && scrolledX != 0 && isDoScrollX()) && isDoScrollY()) {
                    if (event.isUp() && getScrolledY() > 0) {
                        setScrolledY(Math.max(0, getScrolledY() - step));
                        return;
                    } else if (!event.isUp() && getScrolledY() < getMaxScrollY()) {
                        setScrolledY(Math.min(getMaxScrollY(), getScrolledY() + step));
                        return;
                    }
                }

                if (canScrollX() && isDoScrollX()) {
                    if (event.isUp() && getScrolledX() > 0) {
                        setScrolledX(Math.max(0, getScrolledX() - step));
                        return;
                    } else if (!event.isUp() && getScrolledX() < getMaxScrollX()) {
                        setScrolledX(Math.min(getMaxScrollX(), getScrolledX() + step));
                        return;
                    }
                }
            }

        });

        addUpdateListener(new UpdateListener() {
            @Override
            public void updateSize(Component component, int oldWidth, int oldHeight) {
                performCalculations();
            }

            @Override
            public void updateLocation(Component component, int oldX, int oldY) {
                performCalculations();
            }
        });
    }

    @Override
    public void setWidth(int width) {
        if (!lockWidth)
            super.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        if (!lockHeight)
            super.setHeight(height);
    }

    @Override
    public Container addChild(Component... component) {
        super.addChild(component);
        performCalculations();
        return this;
    }

    private void performCalculations() {
        int farX = 0;
        int farY = 0;
        for (Component c : getChildren()) {
            farX = Math.max(getScrolledX() + c.getX() + c.getWidth(), farX);
            farY = Math.max(getScrolledY() + c.getY() + c.getHeight(), farY);
        }

        canScrollX = farX > getWidth();
        maxScrollX = farX - getWidth();
        canScrollY = farY > getHeight();
        maxScrollY = farY - getHeight();
    }

    public boolean canScrollX() {
        return canScrollX;
    }

    public boolean canScrollY() {
        return canScrollY;
    }

    public boolean isDoScrollX() {
        return doScrollX;
    }

    public boolean isDoScrollY() {
        return doScrollY;
    }

    public void setScrolledX(int scrolledX) {
        int a = getScrolledX();
        this.scrolledX = scrolledX;
        int dif = getScrolledX() - a;
        for (Component component : getChildren())
            component.setX(component.getX() - dif);
    }

    public void setScrolledY(int scrolledY) {
        int a = getScrolledY();
        this.scrolledY = scrolledY;
        int dif = getScrolledY() - a;
        for (Component component : getChildren())
            component.setY(component.getY() - dif);
    }

    public int getScrolledX() {
        return scrolledX;
    }

    public int getScrolledY() {
        return scrolledY;
    }

    public int getMaxScrollX() {
        return maxScrollX;
    }

    public int getMaxScrollY() {
        return maxScrollY;
    }

    public Scrollpane setLockHeight(boolean lockHeight) {
        this.lockHeight = lockHeight;
        return this;
    }

    public Scrollpane setLockWidth(boolean lockWidth) {
        this.lockWidth = lockWidth;
        return this;
    }

    @Override
    public boolean penetrateTest(int x, int y) {
        return x > 0 && x < getWidth() && y > 0 && y < getHeight();
    }
}
