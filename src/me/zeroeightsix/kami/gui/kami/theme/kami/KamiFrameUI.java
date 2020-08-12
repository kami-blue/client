// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.kami.RootLargeFontRenderer;
import java.util.Iterator;
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen;
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper;
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.gui.rgui.util.Docking;
import me.zeroeightsix.kami.util.Bind;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame.FrameDragPoof;
import me.zeroeightsix.kami.gui.rgui.component.listen.UpdateListener;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.poof.IPoof;
import me.zeroeightsix.kami.gui.rgui.poof.use.FramePoof;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.gui.rgui.GUI;
import java.awt.Color;
import me.zeroeightsix.kami.gui.kami.RenderHelper;
import me.zeroeightsix.kami.module.ModuleManager;
import org.lwjgl.opengl.GL11;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.setting.Setting;
import java.awt.Font;
import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zeroeightsix.kami.gui.font.CFontRenderer;
import me.zeroeightsix.kami.gui.kami.RootFontRenderer;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.util.ColourHolder;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;

public class KamiFrameUI<T extends Frame> extends AbstractComponentUI<Frame>
{
    ColourHolder frameColour;
    ColourHolder outlineColour;
    Component yLineComponent;
    Component xLineComponent;
    Component centerXComponent;
    Component centerYComponent;
    boolean centerX;
    boolean centerY;
    int xLineOffset;
    private static final RootFontRenderer ff;
    CFontRenderer cFontRendererLarge;
    public float redForBG;
    public float greenForBG;
    public float blueForBG;
    
    public KamiFrameUI() {
        this.frameColour = KamiGUI.primaryColour.setA(100);
        this.outlineColour = this.frameColour.darker();
        this.yLineComponent = null;
        this.xLineComponent = null;
        this.centerXComponent = null;
        this.centerYComponent = null;
        this.centerX = false;
        this.centerY = false;
        this.xLineOffset = 0;
        this.cFontRendererLarge = new CFontRenderer(new Font("Verdana", 0, 18), true, false);
    }
    
    private void checkSettingGuiColour(final Setting setting) {
        final String name3;
        final String s;
        final String name2 = s = (name3 = setting.getName());
        switch (s) {
            case "Border Red": {
                this.redForBG = setting.getValue();
                break;
            }
            case "Border Green": {
                this.greenForBG = setting.getValue();
                break;
            }
            case "Border Blue": {
                this.blueForBG = setting.getValue();
                break;
            }
        }
    }
    
    @Override
    public void renderComponent(final Frame component, final FontRenderer fontRenderer) {
        if (component.getOpacity() == 0.0f) {
            return;
        }
        GL11.glDisable(3553);
        ModuleManager.getModuleByName("Gui").settingList.forEach(setting -> this.checkSettingGuiColour(setting));
        GL11.glColor4f(0.2f, 0.2f, 0.2f, 0.75f);
        RenderHelper.drawFilledRectangle(0.0f, 0.0f, (float)component.getWidth(), (float)component.getHeight());
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        GL11.glColor3f(this.redForBG, this.greenForBG, this.blueForBG);
        RenderHelper.drawFilledRectangle(0.0f, 0.0f, (float)component.getWidth(), (float)KamiFrameUI.ff.getFontHeight());
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        GL11.glColor3f(this.redForBG, this.greenForBG, this.blueForBG);
        RenderHelper.drawRectangle(0.0f, 0.0f, (float)component.getWidth(), (float)component.getHeight());
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        if (ModuleManager.getModuleByName("SmoothFont").isEnabled()) {
            GL11.glDisable(2884);
            GL11.glEnable(3042);
            GL11.glEnable(3553);
            this.cFontRendererLarge.drawString(component.getTitle(), (float)(component.getWidth() / 8 - this.cFontRendererLarge.getStringWidth(component.getTitle()) / 10), 1.0f, Color.white.getRGB());
            GL11.glEnable(2884);
            GL11.glDisable(3042);
            GL11.glDisable(3553);
        }
        else {
            KamiFrameUI.ff.drawString(component.getWidth() / 8 - KamiFrameUI.ff.getStringWidth(component.getTitle()) / 10, 1, component.getTitle());
        }
        int top_y = 5;
        int bottom_y = component.getTheme().getFontRenderer().getFontHeight() - 9;
        if (component.isCloseable() && component.isMinimizeable()) {
            top_y -= 4;
            bottom_y -= 4;
        }
        if (component.isCloseable()) {
            GL11.glLineWidth(2.0f);
            GL11.glColor3f(1.0f, 1.0f, 1.0f);
            GL11.glBegin(1);
            GL11.glVertex2d((double)(component.getWidth() - 20), (double)top_y);
            GL11.glVertex2d((double)(component.getWidth() - 10), (double)bottom_y);
            GL11.glVertex2d((double)(component.getWidth() - 10), (double)top_y);
            GL11.glVertex2d((double)(component.getWidth() - 20), (double)bottom_y);
            GL11.glEnd();
        }
        if (component.isCloseable() && component.isMinimizeable()) {
            top_y += 12;
            bottom_y += 12;
        }
        if (component.isMinimizeable()) {
            GL11.glLineWidth(1.5f);
            GL11.glColor3f(1.0f, 1.0f, 1.0f);
            if (component.isMinimized()) {
                GL11.glBegin(2);
                GL11.glVertex2d((double)(component.getWidth() - 15), (double)(top_y + 2));
                GL11.glVertex2d((double)(component.getWidth() - 15), (double)(bottom_y + 3));
                GL11.glVertex2d((double)(component.getWidth() - 10), (double)(bottom_y + 3));
                GL11.glVertex2d((double)(component.getWidth() - 10), (double)(top_y + 2));
                GL11.glEnd();
            }
            else {
                GL11.glBegin(1);
                GL11.glVertex2d((double)(component.getWidth() - 15), (double)(bottom_y + 4));
                GL11.glVertex2d((double)(component.getWidth() - 10), (double)(bottom_y + 4));
                GL11.glEnd();
            }
        }
        if (component.isPinneable()) {
            if (component.isPinned()) {
                GL11.glColor3f(1.0f, 0.33f, 0.33f);
            }
            else {
                GL11.glColor3f(0.66f, 0.66f, 0.66f);
            }
            RenderHelper.drawCircle(7.0f, 4.0f, 2.0f);
            GL11.glLineWidth(3.0f);
            GL11.glBegin(1);
            GL11.glVertex2d(7.0, 4.0);
            GL11.glVertex2d(4.0, 8.0);
            GL11.glEnd();
        }
        if (component.equals(this.xLineComponent)) {
            GL11.glColor3f(0.44f, 0.44f, 0.44f);
            GL11.glLineWidth(1.0f);
            GL11.glBegin(1);
            GL11.glVertex2d((double)this.xLineOffset, (double)(-GUI.calculateRealPosition(component)[1]));
            GL11.glVertex2d((double)this.xLineOffset, (double)Wrapper.getMinecraft().field_71440_d);
            GL11.glEnd();
        }
        if (component == this.centerXComponent && this.centerX) {
            GL11.glColor3f(0.86f, 0.03f, 1.0f);
            GL11.glLineWidth(1.0f);
            GL11.glBegin(1);
            final double x = component.getWidth() / 2;
            GL11.glVertex2d(x, (double)(-GUI.calculateRealPosition(component)[1]));
            GL11.glVertex2d(x, (double)Wrapper.getMinecraft().field_71440_d);
            GL11.glEnd();
        }
        if (component.equals(this.yLineComponent)) {
            GL11.glColor3f(0.44f, 0.44f, 0.44f);
            GL11.glLineWidth(1.0f);
            GL11.glBegin(1);
            GL11.glVertex2d((double)(-GUI.calculateRealPosition(component)[0]), 0.0);
            GL11.glVertex2d((double)Wrapper.getMinecraft().field_71443_c, 0.0);
            GL11.glEnd();
        }
        if (component == this.centerYComponent && this.centerY) {
            GL11.glColor3f(0.86f, 0.03f, 1.0f);
            GL11.glLineWidth(1.0f);
            GL11.glBegin(1);
            final double y = component.getHeight() / 2;
            GL11.glVertex2d((double)(-GUI.calculateRealPosition(component)[0]), y);
            GL11.glVertex2d((double)Wrapper.getMinecraft().field_71443_c, y);
            GL11.glEnd();
        }
        GL11.glDisable(3042);
    }
    
    @Override
    public void handleMouseRelease(final Frame component, final int x, final int y, final int button) {
        this.yLineComponent = null;
        this.xLineComponent = null;
        this.centerXComponent = null;
        this.centerYComponent = null;
    }
    
    @Override
    public void handleMouseDrag(final Frame component, final int x, final int y, final int button) {
        super.handleMouseDrag(component, x, y, button);
    }
    
    @Override
    public void handleAddComponent(final Frame component, final Container container) {
        super.handleAddComponent(component, container);
        component.setOriginOffsetY(component.getTheme().getFontRenderer().getFontHeight() + 3);
        component.setOriginOffsetX(3);
        component.addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(final MouseButtonEvent event) {
                final int y = event.getY();
                final int x = event.getX();
                if (y < 0) {
                    if (x > component.getWidth() - 22) {
                        if (component.isMinimizeable() && component.isCloseable()) {
                            if (y > -component.getOriginOffsetY() / 2) {
                                if (component.isMinimized()) {
                                    component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.MAXIMIZE));
                                }
                                else {
                                    component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.MINIMIZE));
                                }
                            }
                            else {
                                component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.CLOSE));
                            }
                        }
                        else if (component.isMinimized() && component.isMinimizeable()) {
                            component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.MAXIMIZE));
                        }
                        else if (component.isMinimizeable()) {
                            component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.MINIMIZE));
                        }
                        else if (component.isCloseable()) {
                            component.callPoof(FramePoof.class, new FramePoof.FramePoofInfo(FramePoof.Action.CLOSE));
                        }
                    }
                    if (x < 10 && x > 0 && component.isPinneable()) {
                        component.setPinned(!component.isPinned());
                    }
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
            }
        });
        component.addUpdateListener(new UpdateListener() {
            @Override
            public void updateSize(final Component component, final int oldWidth, final int oldHeight) {
                if (component instanceof Frame) {
                    KamiGUI.dock((Frame)component);
                }
            }
            
            @Override
            public void updateLocation(final Component component, final int oldX, final int oldY) {
            }
        });
        component.addPoof(new Frame.FrameDragPoof<Frame, Frame.FrameDragPoof.DragInfo>() {
            @Override
            public void execute(final Frame component, final DragInfo info) {
                if (Bind.isShiftDown() || Bind.isAltDown() || Bind.isCtrlDown()) {
                    return;
                }
                int x = info.getX();
                int y = info.getY();
                KamiFrameUI.this.yLineComponent = null;
                KamiFrameUI.this.xLineComponent = null;
                component.setDocking(Docking.NONE);
                final KamiGUI rootGUI = KamiMod.getInstance().getGuiManager();
                for (final Component c : rootGUI.getChildren()) {
                    if (c.equals(component)) {
                        continue;
                    }
                    int yDiff = Math.abs(y - c.getY());
                    if (yDiff < 4) {
                        y = c.getY();
                        KamiFrameUI.this.yLineComponent = component;
                    }
                    yDiff = Math.abs(y - (c.getY() + c.getHeight() + 3));
                    if (yDiff < 4) {
                        y = c.getY() + c.getHeight();
                        y += 3;
                        KamiFrameUI.this.yLineComponent = component;
                    }
                    int xDiff = Math.abs(x + component.getWidth() - (c.getX() + c.getWidth()));
                    if (xDiff < 4) {
                        x = c.getX() + c.getWidth();
                        x -= component.getWidth();
                        KamiFrameUI.this.xLineComponent = component;
                        KamiFrameUI.this.xLineOffset = component.getWidth();
                    }
                    xDiff = Math.abs(x - c.getX());
                    if (xDiff < 4) {
                        x = c.getX();
                        KamiFrameUI.this.xLineComponent = component;
                        KamiFrameUI.this.xLineOffset = 0;
                    }
                    xDiff = Math.abs(x - (c.getX() + c.getWidth() + 3));
                    if (xDiff >= 4) {
                        continue;
                    }
                    x = c.getX() + c.getWidth() + 3;
                    KamiFrameUI.this.xLineComponent = component;
                    KamiFrameUI.this.xLineOffset = 0;
                }
                if (x < 5) {
                    x = 0;
                    ContainerHelper.setAlignment(component, AlignedComponent.Alignment.LEFT);
                    component.setDocking(Docking.LEFT);
                }
                int diff = (x + component.getWidth()) * DisplayGuiScreen.getScale() - Wrapper.getMinecraft().field_71443_c;
                if (-diff < 5) {
                    x = Wrapper.getMinecraft().field_71443_c / DisplayGuiScreen.getScale() - component.getWidth();
                    ContainerHelper.setAlignment(component, AlignedComponent.Alignment.RIGHT);
                    component.setDocking(Docking.RIGHT);
                }
                if (y < 5) {
                    y = 0;
                    if (component.getDocking().equals(Docking.RIGHT)) {
                        component.setDocking(Docking.TOPRIGHT);
                    }
                    else if (component.getDocking().equals(Docking.LEFT)) {
                        component.setDocking(Docking.TOPLEFT);
                    }
                    else {
                        component.setDocking(Docking.TOP);
                    }
                }
                diff = (y + component.getHeight()) * DisplayGuiScreen.getScale() - Wrapper.getMinecraft().field_71440_d;
                if (-diff < 5) {
                    y = Wrapper.getMinecraft().field_71440_d / DisplayGuiScreen.getScale() - component.getHeight();
                    if (component.getDocking().equals(Docking.RIGHT)) {
                        component.setDocking(Docking.BOTTOMRIGHT);
                    }
                    else if (component.getDocking().equals(Docking.LEFT)) {
                        component.setDocking(Docking.BOTTOMLEFT);
                    }
                    else {
                        component.setDocking(Docking.BOTTOM);
                    }
                }
                if (Math.abs((x + component.getWidth() / 2) * DisplayGuiScreen.getScale() * 2 - Wrapper.getMinecraft().field_71443_c) < 5) {
                    KamiFrameUI.this.xLineComponent = null;
                    KamiFrameUI.this.centerXComponent = component;
                    KamiFrameUI.this.centerX = true;
                    x = Wrapper.getMinecraft().field_71443_c / (DisplayGuiScreen.getScale() * 2) - component.getWidth() / 2;
                    if (component.getDocking().isTop()) {
                        component.setDocking(Docking.CENTERTOP);
                    }
                    else if (component.getDocking().isBottom()) {
                        component.setDocking(Docking.CENTERBOTTOM);
                    }
                    else {
                        component.setDocking(Docking.CENTERVERTICAL);
                    }
                    ContainerHelper.setAlignment(component, AlignedComponent.Alignment.CENTER);
                }
                else {
                    KamiFrameUI.this.centerX = false;
                }
                if (Math.abs((y + component.getHeight() / 2) * DisplayGuiScreen.getScale() * 2 - Wrapper.getMinecraft().field_71440_d) < 5) {
                    KamiFrameUI.this.yLineComponent = null;
                    KamiFrameUI.this.centerYComponent = component;
                    KamiFrameUI.this.centerY = true;
                    y = Wrapper.getMinecraft().field_71440_d / (DisplayGuiScreen.getScale() * 2) - component.getHeight() / 2;
                    if (component.getDocking().isLeft()) {
                        component.setDocking(Docking.CENTERLEFT);
                    }
                    else if (component.getDocking().isRight()) {
                        component.setDocking(Docking.CENTERRIGHT);
                    }
                    else if (component.getDocking().isCenterHorizontal()) {
                        component.setDocking(Docking.CENTER);
                    }
                    else {
                        component.setDocking(Docking.CENTERHOIZONTAL);
                    }
                }
                else {
                    KamiFrameUI.this.centerY = false;
                }
                info.setX(x);
                info.setY(y);
            }
        });
    }
    
    static {
        ff = new RootLargeFontRenderer();
    }
}
