// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.rgui.component.container.use;

import me.zeroeightsix.kami.gui.rgui.poof.use.Poof;
import java.util.Iterator;
import java.util.Map;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen;
import me.zeroeightsix.kami.gui.rgui.component.listen.RenderListener;
import me.zeroeightsix.kami.gui.rgui.component.container.Container;
import me.zeroeightsix.kami.gui.rgui.component.listen.UpdateListener;
import me.zeroeightsix.kami.gui.rgui.poof.IPoof;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.poof.use.FramePoof;
import me.zeroeightsix.kami.gui.rgui.layout.Layout;
import me.zeroeightsix.kami.gui.rgui.layout.UselessLayout;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import java.util.HashMap;
import me.zeroeightsix.kami.gui.rgui.util.Docking;
import me.zeroeightsix.kami.gui.rgui.component.container.OrganisedContainer;

public class Frame extends OrganisedContainer
{
    String title;
    int trueheight;
    int truemaxheight;
    int dx;
    int dy;
    boolean doDrag;
    boolean startDrag;
    boolean isMinimized;
    boolean isMinimizeable;
    boolean isCloseable;
    boolean isPinned;
    boolean isPinneable;
    boolean isLayoutWorking;
    boolean boxEnabled;
    Docking docking;
    HashMap<Component, Boolean> visibilityMap;
    
    public Frame(final Theme theme, final String title) {
        this(theme, new UselessLayout(), title);
    }
    
    public Frame(final Theme theme, final Layout layout, final String title) {
        super(theme, layout);
        this.trueheight = 0;
        this.truemaxheight = 0;
        this.dx = 0;
        this.dy = 0;
        this.doDrag = false;
        this.startDrag = false;
        this.isMinimized = false;
        this.isMinimizeable = true;
        this.isCloseable = true;
        this.isPinned = false;
        this.isPinneable = false;
        this.isLayoutWorking = false;
        this.boxEnabled = true;
        this.docking = Docking.NONE;
        this.visibilityMap = new HashMap<Component, Boolean>();
        this.title = title;
        this.addPoof(new FramePoof<Frame, FramePoof.FramePoofInfo>() {
            @Override
            public void execute(final Frame component, final FramePoofInfo info) {
                switch (info.getAction()) {
                    case MINIMIZE: {
                        if (Frame.this.isMinimizeable) {
                            Frame.this.setMinimized(true);
                            break;
                        }
                        break;
                    }
                    case MAXIMIZE: {
                        if (Frame.this.isMinimizeable) {
                            Frame.this.setMinimized(false);
                            break;
                        }
                        break;
                    }
                    case CLOSE: {
                        if (Frame.this.isCloseable) {
                            Frame.this.getParent().removeChild(Frame.this);
                            break;
                        }
                        break;
                    }
                }
            }
        });
        this.addUpdateListener(new UpdateListener() {
            @Override
            public void updateSize(final Component component, final int oldWidth, final int oldHeight) {
                if (Frame.this.isLayoutWorking) {
                    return;
                }
                if (!component.equals(Frame.this)) {
                    Frame.this.isLayoutWorking = true;
                    layout.organiseContainer(Frame.this);
                    Frame.this.isLayoutWorking = false;
                }
            }
            
            @Override
            public void updateLocation(final Component component, final int oldX, final int oldY) {
                if (Frame.this.isLayoutWorking) {
                    return;
                }
                if (!component.equals(Frame.this)) {
                    Frame.this.isLayoutWorking = true;
                    layout.organiseContainer(Frame.this);
                    Frame.this.isLayoutWorking = false;
                }
            }
        });
        this.addRenderListener(new RenderListener() {
            @Override
            public void onPreRender() {
                if (Frame.this.startDrag) {
                    final FrameDragPoof.DragInfo info = new FrameDragPoof.DragInfo(DisplayGuiScreen.mouseX - Frame.this.dx, DisplayGuiScreen.mouseY - Frame.this.dy);
                    Frame.this.callPoof(FrameDragPoof.class, info);
                    Frame.this.setX(info.getX());
                    Frame.this.setY(info.getY());
                }
            }
            
            @Override
            public void onPostRender() {
            }
        });
        this.addMouseListener(new GayMouseListener());
    }
    
    public void setCloseable(final boolean closeable) {
        this.isCloseable = closeable;
    }
    
    public boolean isBoxEnabled() {
        return this.boxEnabled;
    }
    
    public void setMinimizeable(final boolean minimizeable) {
        this.isMinimizeable = minimizeable;
    }
    
    public boolean isMinimizeable() {
        return this.isMinimizeable;
    }
    
    public boolean isMinimized() {
        return this.isMinimized;
    }
    
    public void setMinimized(final boolean minimized) {
        if (minimized && !this.isMinimized) {
            this.trueheight = this.getHeight();
            this.truemaxheight = this.getMaximumHeight();
            this.setHeight(0);
            this.setMaximumHeight(this.getOriginOffsetY());
            for (final Component c : this.getChildren()) {
                this.visibilityMap.put(c, c.isVisible());
                c.setVisible(false);
            }
        }
        else if (!minimized && this.isMinimized) {
            this.setMaximumHeight(this.truemaxheight);
            this.setHeight(this.trueheight - this.getOriginOffsetY());
            for (final Map.Entry<Component, Boolean> entry : this.visibilityMap.entrySet()) {
                entry.getKey().setVisible(entry.getValue());
            }
        }
        this.isMinimized = minimized;
    }
    
    public boolean isCloseable() {
        return this.isCloseable;
    }
    
    public boolean isPinneable() {
        return this.isPinneable;
    }
    
    public boolean isPinned() {
        return this.isPinned;
    }
    
    public void setPinneable(final boolean pinneable) {
        this.isPinneable = pinneable;
    }
    
    public void setPinned(final boolean pinned) {
        this.isPinned = (pinned && this.isPinneable);
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public Docking getDocking() {
        return this.docking;
    }
    
    public void setDocking(final Docking docking) {
        this.docking = docking;
    }
    
    public class GayMouseListener implements MouseListener
    {
        @Override
        public void onMouseDown(final MouseButtonEvent event) {
            Frame.this.dx = event.getX() + Frame.this.getOriginOffsetX();
            Frame.this.dy = event.getY() + Frame.this.getOriginOffsetY();
            if (Frame.this.dy <= Frame.this.getOriginOffsetY() && event.getButton() == 0 && Frame.this.dy > 0) {
                Frame.this.doDrag = true;
            }
            else {
                Frame.this.doDrag = false;
            }
            if (Frame.this.isMinimized && event.getY() > Frame.this.getOriginOffsetY()) {
                event.cancel();
            }
        }
        
        @Override
        public void onMouseRelease(final MouseButtonEvent event) {
            Frame.this.doDrag = false;
            Frame.this.startDrag = false;
        }
        
        @Override
        public void onMouseDrag(final MouseButtonEvent event) {
            if (!Frame.this.doDrag) {
                return;
            }
            Frame.this.startDrag = true;
        }
        
        @Override
        public void onMouseMove(final MouseMoveEvent event) {
        }
        
        @Override
        public void onScroll(final MouseScrollEvent event) {
        }
    }
    
    public abstract static class FrameDragPoof<T extends Frame, S extends DragInfo> extends Poof<T, S>
    {
        public static class DragInfo extends PoofInfo
        {
            int x;
            int y;
            
            public DragInfo(final int x, final int y) {
                this.x = x;
                this.y = y;
            }
            
            public int getX() {
                return this.x;
            }
            
            public int getY() {
                return this.y;
            }
            
            public void setX(final int x) {
                this.x = x;
            }
            
            public void setY(final int y) {
                this.y = y;
            }
        }
    }
}
