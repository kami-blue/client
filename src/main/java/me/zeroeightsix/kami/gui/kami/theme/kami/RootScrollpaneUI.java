package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.theme.kami.KamiGuiColors.GuiC;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Scrollpane;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.RenderListener;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.util.color.ColorHolder;
import me.zeroeightsix.kami.util.graphics.GlStateUtils;
import me.zeroeightsix.kami.util.graphics.RenderUtils2D;
import me.zeroeightsix.kami.util.graphics.VertexHelper;
import me.zeroeightsix.kami.util.math.Vec2d;

/**
 * Created by 086 on 27/06/2017.
 */
public class RootScrollpaneUI extends AbstractComponentUI<Scrollpane> {

    long lastScroll = 0;
    Component scrollComponent = null;
    float barLife = 1220;
    boolean dragBar = false;
    int dY = 0;
    double hovering = 0;

    @Override
    public void renderComponent(Scrollpane component) {

    }

    @Override
    public void handleAddComponent(Scrollpane component, Container container) {
        component.addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(MouseButtonEvent event) {
                if (component.canScrollY()) {
                    double progress = (double) component.getScrolledY() / (double) component.getMaxScrollY();
                    int barHeight = 30;
                    int y = (int) ((component.getHeight() - barHeight) * progress);
                    if (event.getX() > component.getWidth() - 10 && event.getY() > y && event.getY() < y + barHeight) {
                        dragBar = true;
                        dY = event.getY() - y;
                        event.cancel();
                    }
                }
            }

            @Override
            public void onMouseRelease(MouseButtonEvent event) {
                dragBar = false;
            }

            @Override
            public void onMouseDrag(MouseButtonEvent event) {
                if (dragBar) {
                    double progress = event.getY() / (double) component.getHeight();
                    progress = Math.max(Math.min(progress, 1), 0);
                    component.setScrolledY((int) (component.getMaxScrollY() * progress));
                    event.cancel();
                }
            }

            @Override
            public void onMouseMove(MouseMoveEvent event) {

            }

            @Override
            public void onScroll(MouseScrollEvent event) {
                lastScroll = System.currentTimeMillis();
                scrollComponent = event.getComponent();
            }
        });

        component.addRenderListener(new RenderListener() {
            @Override
            public void onPreRender() {

            }

            @Override
            public void onPostRender() {
                lastScroll = System.currentTimeMillis();

                if (component.canScrollY()) {
                    float alpha = Math.min(1, (barLife - (System.currentTimeMillis() - lastScroll)) / 100f) / 3f;
                    if (dragBar) alpha = 0.4f;

                    int barHeight = 30;
                    double progress = (double) component.getScrolledY() / (double) component.getMaxScrollY();
                    int y = (int) ((component.getHeight() - barHeight) * progress);

                    ColorHolder color = new ColorHolder(GuiC.scrollBar.color);
                    color.setA((int) (alpha * 255f));
                    VertexHelper vertexHelper = new VertexHelper(GlStateUtils.useVbo());
                    Vec2d pos1 = new Vec2d(component.getWidth() - 6, y);
                    Vec2d pos2 = pos1.plus(4, barHeight);

                    RenderUtils2D.drawRoundedRectFilled(vertexHelper, pos1, pos2, 1, 8, color);
                }
            }
        });
    }
}
