package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen;
import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zeroeightsix.kami.gui.rgui.GUI;
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.UpdateListener;
import me.zeroeightsix.kami.gui.rgui.poof.use.FramePoof;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper;
import me.zeroeightsix.kami.gui.rgui.util.Docking;
import me.zeroeightsix.kami.util.Bind;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.util.color.ColorHolder;
import me.zeroeightsix.kami.util.graphics.GlStateUtils;
import me.zeroeightsix.kami.util.graphics.RenderUtils2D;
import me.zeroeightsix.kami.util.graphics.VertexHelper;
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter;
import me.zeroeightsix.kami.util.math.Vec2d;

import static me.zeroeightsix.kami.gui.kami.theme.kami.KamiGuiColors.GuiC;

/**
 * Created by 086 on 26/06/2017.
 */
public class KamiFrameUI<T extends Frame> extends AbstractComponentUI<Frame> {

    Component yLineComponent = null;
    Component xLineComponent = null;
    Component centerXComponent = null;
    Component centerYComponent = null;
    boolean centerX = false;
    boolean centerY = false;
    int xLineOffset = 0;

    @Override
    public void renderComponent(Frame component) {
        if (component.getOpacity() == 0) return;

        VertexHelper vertexHelper = new VertexHelper(GlStateUtils.useVbo());
        RenderUtils2D.drawRectFilled(vertexHelper, new Vec2d(component.getWidth(), component.getHeight()), new ColorHolder(GuiC.windowFilled.color));
        RenderUtils2D.drawRectOutline(vertexHelper, new Vec2d(0.0, 0.0), new Vec2d(component.getWidth(), component.getHeight()), 1.8f, new ColorHolder(GuiC.windowOutline.color));

        FontRenderAdapter.INSTANCE.drawString(component.getTitle(), component.getWidth() / 2f - FontRenderAdapter.INSTANCE.getStringWidth(component.getTitle()) / 2f, 1f, false);

        int top_y = 5;
        float bottom_y = FontRenderAdapter.INSTANCE.getFontHeight() - 9f;

        if (component.isCloseable() && component.isMinimizeable()) {
            top_y -= 4;
            bottom_y -= 4;
        }

        if (component.isCloseable()) {
            RenderUtils2D.drawRectOutline(vertexHelper, new Vec2d(component.getWidth() - 20, top_y), new Vec2d(component.getWidth() - 10, bottom_y), 2f, new ColorHolder(255, 255, 255));
        }

        if (component.isCloseable() && component.isMinimizeable()) {
            top_y += 12;
            bottom_y += 12;
        }

        if (component.isMinimizeable()) {
            if (component.isMinimized()) {
                RenderUtils2D.drawRectOutline(vertexHelper, new Vec2d(component.getWidth() - 15, top_y + 2), new Vec2d(component.getWidth() - 10, bottom_y + 3), 1.5f, new ColorHolder(255, 255, 255));
            } else {
                RenderUtils2D.drawLine(vertexHelper, new Vec2d(component.getWidth() - 15, bottom_y + 4), new Vec2d(component.getWidth() - 10, bottom_y + 4), 1.5f, new ColorHolder(255, 255, 255));
            }
        }

        if (component.isPinnable()) {
            ColorHolder color;
            if (component.isPinned()) color = new ColorHolder(GuiC.pinnedWindow.color);
            else color = new ColorHolder(GuiC.unpinnedWindow.color);
            RenderUtils2D.drawCircleFilled(vertexHelper, new Vec2d(7, 4), 2.0, color);
            RenderUtils2D.drawLine(vertexHelper, new Vec2d(7, 4), new Vec2d(4, 8), 3f, color);
        }

        if (component.equals(xLineComponent)) {
            ColorHolder color = new ColorHolder(GuiC.lineWindow.color);
            RenderUtils2D.drawLine(vertexHelper, new Vec2d(xLineOffset, -GUI.calculateRealPosition(component)[1]), new Vec2d(xLineOffset, Wrapper.getMinecraft().displayHeight), 1f, color);
        }

        if (component == centerXComponent && centerX) {
            float x = component.getWidth() / 2f;
            ColorHolder color = new ColorHolder(219, 8, 255);
            RenderUtils2D.drawLine(vertexHelper, new Vec2d(x, -GUI.calculateRealPosition(component)[1]), new Vec2d(x, Wrapper.getMinecraft().displayHeight), 1f, color);
        }

        if (component.equals(yLineComponent)) {
            ColorHolder color = new ColorHolder(GuiC.lineWindow.color);
            RenderUtils2D.drawLine(vertexHelper, new Vec2d(-GUI.calculateRealPosition(component)[0], 0), new Vec2d(Wrapper.getMinecraft().displayWidth, 0), 1f, color);
        }

        if (component == centerYComponent && centerY) {
            float y = component.getHeight() / 2f;
            ColorHolder color = new ColorHolder(219, 8, 255);
            RenderUtils2D.drawLine(vertexHelper, new Vec2d(-GUI.calculateRealPosition(component)[0], y), new Vec2d(Wrapper.getMinecraft().displayWidth, y), 1f, color);
        }
    }

    @Override
    public void handleMouseRelease(Frame component, int x, int y, int button) {
        yLineComponent = null;
        xLineComponent = null;
        centerXComponent = null;
        centerYComponent = null;
    }

    @Override
    public void handleMouseDrag(Frame component, int x, int y, int button) {
        super.handleMouseDrag(component, x, y, button);
    }

    @Override
    public void handleAddComponent(Frame component, Container container) {
        super.handleAddComponent(component, container);
        component.setOriginOffsetY((int) (FontRenderAdapter.INSTANCE.getFontHeight() + 3f));
        component.setOriginOffsetX(3);

        component.addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(MouseButtonEvent event) {
                int y = event.getY();
                int x = event.getX();
                if (y < 0) {
                    if (x > component.getWidth() - 22) {
                        if (component.isMinimizeable() && component.isCloseable()) {
                            if (y > -component.getOriginOffsetY() / 2) {
                                if (component.isMinimized()) {
                                    component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.MAXIMIZE));
                                } else {
                                    component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.MINIMIZE));
                                }
                            } else {
                                component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.CLOSE));
                            }
                        } else {
                            if (component.isMinimized() && component.isMinimizeable()) {
                                component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.MAXIMIZE));
                            } else if (component.isMinimizeable()) {
                                component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.MINIMIZE));
                            } else if (component.isCloseable()) {
                                component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.CLOSE));
                            }
                        }
                    }
                    if (x < 10 && x > 0) {
                        if (component.isPinnable()) {
                            component.setPinned(!component.isPinned());
                        }
                    }
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

            }
        });

        component.addUpdateListener(new UpdateListener() {
            @Override
            public void updateSize(Component component, int oldWidth, int oldHeight) {
                if (component instanceof Frame) {
                    KamiGUI.dock((Frame) component);
                }
            }

            @Override
            public void updateLocation(Component component, int oldX, int oldY) {
            }
        });

        component.addPoof(new Frame.FrameDragPoof<Frame, Frame.FrameDragPoof.DragInfo>() {
            @Override
            public void execute(Frame component, DragInfo info) {
                if (Bind.isShiftDown() || Bind.isAltDown() || Bind.isCtrlDown()) return;
                double x = info.getX();
                double y = info.getY();
                yLineComponent = null;
                xLineComponent = null;

                component.setDocking(Docking.NONE);

                KamiGUI rootGUI = KamiMod.INSTANCE.getGuiManager();
                for (Component c : rootGUI.getChildren()) {
                    if (c.equals(component)) continue;

                    double yDiff = Math.abs(y - c.getY());
                    if (yDiff < 4) {
                        y = c.getY();
                        yLineComponent = component;
                    }

                    yDiff = Math.abs(y - (c.getY() + c.getHeight() + 3));
                    if (yDiff < 4) {
                        y = c.getY() + c.getHeight();
                        y += 3;
                        yLineComponent = component;
                    }

                    double xDiff = Math.abs((x + component.getWidth()) - (c.getX() + c.getWidth()));
                    if (xDiff < 4) {
                        x = c.getX() + c.getWidth();
                        x -= component.getWidth();
                        xLineComponent = component;
                        xLineOffset = component.getWidth();
                    }

                    xDiff = Math.abs(x - c.getX());
                    if (xDiff < 4) {
                        x = c.getX();
                        xLineComponent = component;
                        xLineOffset = 0;
                    }

                    xDiff = Math.abs(x - (c.getX() + c.getWidth() + 3));
                    if (xDiff < 4) {
                        x = c.getX() + c.getWidth() + 3;
                        xLineComponent = component;
                        xLineOffset = 0;
                    }

                }

                if (x < 5) {
                    x = 0;
                    ContainerHelper.setAlignment(component, AlignedComponent.Alignment.LEFT);
                    component.setDocking(Docking.LEFT);
                }
                double diff = (x + component.getWidth()) * DisplayGuiScreen.getScale() - Wrapper.getMinecraft().displayWidth;
                if (-diff < 5) {
                    x = (Wrapper.getMinecraft().displayWidth / DisplayGuiScreen.getScale()) - component.getWidth();
                    ContainerHelper.setAlignment(component, AlignedComponent.Alignment.RIGHT);
                    component.setDocking(Docking.RIGHT);
                }

                if (y < 5) {
                    y = 0;
                    if (component.getDocking().equals(Docking.RIGHT))
                        component.setDocking(Docking.TOPRIGHT);
                    else if (component.getDocking().equals(Docking.LEFT))
                        component.setDocking(Docking.TOPLEFT);
                    else
                        component.setDocking(Docking.TOP);
                }

                diff = (y + component.getHeight()) * DisplayGuiScreen.getScale() - Wrapper.getMinecraft().displayHeight;
                if (-diff < 5) {
                    y = (Wrapper.getMinecraft().displayHeight / DisplayGuiScreen.getScale()) - component.getHeight();

                    if (component.getDocking().equals(Docking.RIGHT))
                        component.setDocking(Docking.BOTTOMRIGHT);
                    else if (component.getDocking().equals(Docking.LEFT))
                        component.setDocking(Docking.BOTTOMLEFT);
                    else
                        component.setDocking(Docking.BOTTOM);
                }

                if (Math.abs(((x + component.getWidth() / 2.0) * DisplayGuiScreen.getScale() * 2) - Wrapper.getMinecraft().displayWidth) < 5) { // Component is center-aligned on the x axis
                    xLineComponent = null;
                    centerXComponent = component;
                    centerX = true;
                    x = (Wrapper.getMinecraft().displayWidth / (DisplayGuiScreen.getScale() * 2)) - component.getWidth() / 2.0;
                    if (component.getDocking().isTop()) {
                        component.setDocking(Docking.CENTERTOP);
                    } else if (component.getDocking().isBottom()) {
                        component.setDocking(Docking.CENTERBOTTOM);
                    } else {
                        component.setDocking(Docking.CENTERVERTICAL);
                    }
                    ContainerHelper.setAlignment(component, AlignedComponent.Alignment.CENTER);
                } else {
                    centerX = false;
                }

                if (Math.abs(((y + component.getHeight() / 2.0) * DisplayGuiScreen.getScale() * 2) - Wrapper.getMinecraft().displayHeight) < 5) { // Component is center-aligned on the y axis
                    yLineComponent = null;
                    centerYComponent = component;
                    centerY = true;
                    y = (Wrapper.getMinecraft().displayHeight / (DisplayGuiScreen.getScale() * 2)) - component.getHeight() / 2.0;
                    if (component.getDocking().isLeft()) {
                        component.setDocking(Docking.CENTERLEFT);
                    } else if (component.getDocking().isRight()) {
                        component.setDocking(Docking.CENTERRIGHT);
                    } else if (component.getDocking().isCenterHorizontal()) {
                        component.setDocking(Docking.CENTER);
                    } else {
                        component.setDocking(Docking.CENTERHOIZONTAL);
                    }
                } else {
                    centerY = false;
                }

                info.setX((int) x);
                info.setY((int) y);
            }
        });
    }
}
