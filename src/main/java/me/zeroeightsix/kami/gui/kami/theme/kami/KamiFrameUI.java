package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.gui.kami.*;
import me.zeroeightsix.kami.gui.rgui.GUI;
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.UpdateListener;
import me.zeroeightsix.kami.gui.rgui.poof.use.FramePoof;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper;
import me.zeroeightsix.kami.gui.rgui.util.Docking;
import me.zeroeightsix.kami.util.Bind;
import me.zeroeightsix.kami.util.ColourHolder;
import me.zeroeightsix.kami.util.Wrapper;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by 086 on 26/06/2017.
 */
public class KamiFrameUI<T extends Frame> extends AbstractComponentUI<Frame> {

    ColourHolder frameColour = KamiGUI.primaryColour.setA(100);
    ColourHolder outlineColour = frameColour.darker();

    Component yLineComponent = null;
    Component xLineComponent = null;
    Component centerXComponent = null;
    Component centerYComponent = null;
    boolean centerX = false;
    boolean centerY = false;
    int xLineOffset = 0;

    private static final RootFontRenderer ff = new RootLargeFontRenderer();

    @Override
    public void renderComponent(Frame component, FontRenderer fontRenderer) {
        if (component.getOpacity() == 0)
            return;
        glDisable(GL_TEXTURE_2D);

        glColor4f(.17f,.17f,.18f,.9f);
        RenderHelper.drawFilledRectangle(0,0,component.getWidth(),component.getHeight());
        glColor3f(.60f,.56f,1.00f);
        glLineWidth(1.5f);
        RenderHelper.drawRectangle(0,0,component.getWidth(),component.getHeight());

        GL11.glColor3f(1,1,1);
        ff.drawString(component.getWidth() / 2 - ff.getStringWidth(component.getTitle()) / 2, 1, component.getTitle());

        int top_y = 5;
        int bottom_y = component.getTheme().getFontRenderer().getFontHeight() - 9;

        if (component.isCloseable() && component.isMinimizeable()){
            top_y -= 4;
            bottom_y -= 4;
        }

        if (component.isCloseable()){
            glLineWidth(2f);
            glColor3f(1,1,1);
            glBegin(GL_LINES);
            {
                glVertex2d(component.getWidth() - 20, top_y);
                glVertex2d(component.getWidth() - 10, bottom_y);
                glVertex2d(component.getWidth() - 10, top_y);
                glVertex2d(component.getWidth() - 20, bottom_y);
            }
            glEnd();
        }

        if (component.isCloseable() && component.isMinimizeable()){
            top_y += 12;
            bottom_y += 12;
        }

        if (component.isMinimizeable()){
            glLineWidth(1.5f);
            glColor3f(1,1,1);
            if (component.isMinimized()){
                glBegin(GL_LINE_LOOP);
                {
                    glVertex2d(component.getWidth() - 15, top_y+2);
                    glVertex2d(component.getWidth() - 15, bottom_y+3);
                    glVertex2d(component.getWidth() - 10, bottom_y+3);
                    glVertex2d(component.getWidth() - 10, top_y+2);
                }
                glEnd();
            } else {
                glBegin(GL_LINES);
                {
                    glVertex2d(component.getWidth() - 15, bottom_y+4);
                    glVertex2d(component.getWidth() - 10, bottom_y+4);
                }
                glEnd();
            }
        }

        if (component.isPinneable()){
            if (component.isPinned())
                glColor3f(1,.33f,.33f);
            else
                glColor3f(0.66f,0.66f,0.66f);
            RenderHelper.drawCircle(7,4,2f);
            glLineWidth(3f);
            glBegin(GL_LINES);
            {
                glVertex2d(7,4);
                glVertex2d(4,8);
            }
            glEnd();
        }

        if (component.equals(xLineComponent)){
            glColor3f(.44f,.44f,.44f);
            glLineWidth(1f);
            glBegin(GL_LINES);
            {
                glVertex2d(xLineOffset,-GUI.calculateRealPosition(component)[1]);
                glVertex2d(xLineOffset, Wrapper.getMinecraft().displayHeight);
            }
            glEnd();
        }

        if (component == centerXComponent && centerX) {
            glColor3f(0.86f, 0.03f, 1f);
            glLineWidth(1f);
            glBegin(GL_LINES);
            {
                double x = component.getWidth() / 2;
                glVertex2d(x, -GUI.calculateRealPosition(component)[1]);
                glVertex2d(x, Wrapper.getMinecraft().displayHeight);
            }
            glEnd();
        }

        if (component.equals(yLineComponent)){
            glColor3f(.44f,.44f,.44f);
            glLineWidth(1f);
            glBegin(GL_LINES);
            {
                glVertex2d(-GUI.calculateRealPosition(component)[0],0);
                glVertex2d(Wrapper.getMinecraft().displayWidth, 0);
            }
            glEnd();
        }

        if (component == centerYComponent && centerY) {
            glColor3f(0.86f, 0.03f, 1f);
            glLineWidth(1f);
            glBegin(GL_LINES);
            {
                double y = component.getHeight() / 2;
                glVertex2d(-GUI.calculateRealPosition(component)[0], y);
                glVertex2d(Wrapper.getMinecraft().displayWidth, y);
            }
            glEnd();
        }

        glDisable(GL_BLEND);
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
        component.setOriginOffsetY(component.getTheme().getFontRenderer().getFontHeight() + 3);
        component.setOriginOffsetX(3);

        component.addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(MouseButtonEvent event) {
                int y = event.getY();
                int x = event.getX();
                if (y < 0){
                    if (x > component.getWidth() - 22){
                        if (component.isMinimizeable() && component.isCloseable()){
                            if (y > -component.getOriginOffsetY()/2){
                                if (component.isMinimized()){
                                    component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.MAXIMIZE));
                                }else {
                                    component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.MINIMIZE));
                                }
                            }else{
                                component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.CLOSE));
                            }
                        }else{
                            if (component.isMinimized() && component.isMinimizeable()){
                                component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.MAXIMIZE));
                            }else if (component.isMinimizeable()){
                                component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.MINIMIZE));
                            }else if (component.isCloseable()) {
                                component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.CLOSE));
                            }
                        }
                    }
                    if (x < 10 && x > 0){
                        if (component.isPinneable()){
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
            public void updateLocation(Component component, int oldX, int oldY) { }
        });

        component.addPoof(new Frame.FrameDragPoof<Frame, Frame.FrameDragPoof.DragInfo>() {
            @Override
            public void execute(Frame component, DragInfo info) {
                if (Bind.isShiftDown() || Bind.isAltDown() || Bind.isCtrlDown()) return;
                int x = info.getX();
                int y = info.getY();
                yLineComponent = null;
                xLineComponent = null;

                component.setDocking(Docking.NONE);

                KamiGUI rootGUI = KamiMod.getInstance().getGuiManager();
                for (Component c : rootGUI.getChildren()){
                    if (c.equals(component)) continue;

                    int yDiff = Math.abs(y - c.getY());
                    if (yDiff < 4){
                        y = c.getY();
                        yLineComponent = component;
                    }

                    yDiff = Math.abs(y - (c.getY() + c.getHeight() + 3));
                    if (yDiff < 4) {
                        y = c.getY() + c.getHeight();
                        y += 3;
                        yLineComponent = component;
                    }

                    int xDiff = Math.abs((x + component.getWidth()) - (c.getX() + c.getWidth()));
                    if (xDiff < 4){
                        x = c.getX() + c.getWidth();
                        x -= component.getWidth();
                        xLineComponent = component;
                        xLineOffset = component.getWidth();
                    }

                    xDiff = Math.abs(x - c.getX());
                    if (xDiff < 4){
                        x = c.getX();
                        xLineComponent = component;
                        xLineOffset = 0;
                    }

                    xDiff = Math.abs(x - (c.getX() + c.getWidth() + 3));
                    if (xDiff < 4){
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
                int diff = (x+component.getWidth()) * DisplayGuiScreen.getScale() - Wrapper.getMinecraft().displayWidth;
                if (-diff < 5){
                    x = (Wrapper.getMinecraft().displayWidth / DisplayGuiScreen.getScale())-component.getWidth();
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

                diff = (y+component.getHeight()) * DisplayGuiScreen.getScale() - Wrapper.getMinecraft().displayHeight;
                if (-diff < 5) {
                    y = (Wrapper.getMinecraft().displayHeight / DisplayGuiScreen.getScale()) - component.getHeight();

                    if (component.getDocking().equals(Docking.RIGHT))
                        component.setDocking(Docking.BOTTOMRIGHT);
                    else if (component.getDocking().equals(Docking.LEFT))
                        component.setDocking(Docking.BOTTOMLEFT);
                    else
                        component.setDocking(Docking.BOTTOM);
                }

                if (Math.abs(((x + component.getWidth() / 2) * DisplayGuiScreen.getScale() * 2) - Wrapper.getMinecraft().displayWidth) < 5) { // Component is center-aligned on the x axis
                    xLineComponent = null;
                    centerXComponent = component;
                    centerX = true;
                    x = (Wrapper.getMinecraft().displayWidth / (DisplayGuiScreen.getScale() * 2)) - component.getWidth() / 2;
                    if (component.getDocking().isTop()) {
                        component.setDocking(Docking.CENTERTOP);
                    } else if (component.getDocking().isBottom()){
                        component.setDocking(Docking.CENTERBOTTOM);
                    } else {
                        component.setDocking(Docking.CENTERVERTICAL);
                    }
                    ContainerHelper.setAlignment(component, AlignedComponent.Alignment.CENTER);
                } else {
                    centerX = false;
                }

                if (Math.abs(((y + component.getHeight() / 2) * DisplayGuiScreen.getScale() * 2) - Wrapper.getMinecraft().displayHeight) < 5) { // Component is center-aligned on the y axis
                    yLineComponent = null;
                    centerYComponent = component;
                    centerY = true;
                    y = (Wrapper.getMinecraft().displayHeight / (DisplayGuiScreen.getScale() * 2)) - component.getHeight() / 2;
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

                info.setX(x);
                info.setY(y);
            }
        });
    }
}
