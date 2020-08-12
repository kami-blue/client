// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.rgui.component.listen;

import me.zeroeightsix.kami.gui.rgui.component.Component;

public interface MouseListener
{
    void onMouseDown(final MouseButtonEvent p0);
    
    void onMouseRelease(final MouseButtonEvent p0);
    
    void onMouseDrag(final MouseButtonEvent p0);
    
    void onMouseMove(final MouseMoveEvent p0);
    
    void onScroll(final MouseScrollEvent p0);
    
    public static class MouseMoveEvent
    {
        boolean cancelled;
        int x;
        int y;
        int oldX;
        int oldY;
        Component component;
        
        public MouseMoveEvent(final int x, final int y, final int oldX, final int oldY, final Component component) {
            this.cancelled = false;
            this.x = x;
            this.y = y;
            this.oldX = oldX;
            this.oldY = oldY;
            this.component = component;
        }
        
        public Component getComponent() {
            return this.component;
        }
        
        public int getOldX() {
            return this.oldX;
        }
        
        public int getOldY() {
            return this.oldY;
        }
        
        public int getY() {
            return this.y;
        }
        
        public int getX() {
            return this.x;
        }
        
        public void setX(final int x) {
            this.x = x;
        }
        
        public void setY(final int y) {
            this.y = y;
        }
        
        public boolean isCancelled() {
            return this.cancelled;
        }
    }
    
    public static class MouseButtonEvent
    {
        int x;
        int y;
        int button;
        Component component;
        boolean cancelled;
        
        public MouseButtonEvent(final int x, final int y, final int button, final Component component) {
            this.cancelled = false;
            this.x = x;
            this.y = y;
            this.button = button;
            this.component = component;
        }
        
        public Component getComponent() {
            return this.component;
        }
        
        public void setButton(final int button) {
            this.button = button;
        }
        
        public int getButton() {
            return this.button;
        }
        
        public void setX(final int x) {
            this.x = x;
        }
        
        public int getX() {
            return this.x;
        }
        
        public void setY(final int y) {
            this.y = y;
        }
        
        public int getY() {
            return this.y;
        }
        
        public void cancel() {
            this.cancelled = true;
        }
        
        public boolean isCancelled() {
            return this.cancelled;
        }
    }
    
    public static class MouseScrollEvent
    {
        int x;
        int y;
        boolean up;
        Component component;
        private boolean cancelled;
        
        public MouseScrollEvent(final int x, final int y, final boolean up, final Component component) {
            this.x = x;
            this.y = y;
            this.up = up;
            this.component = component;
        }
        
        public Component getComponent() {
            return this.component;
        }
        
        public boolean isUp() {
            return this.up;
        }
        
        public void setUp(final boolean up) {
            this.up = up;
        }
        
        public void setX(final int x) {
            this.x = x;
        }
        
        public int getX() {
            return this.x;
        }
        
        public void setY(final int y) {
            this.y = y;
        }
        
        public int getY() {
            return this.y;
        }
        
        public void cancel() {
            this.cancelled = true;
        }
        
        public boolean isCancelled() {
            return this.cancelled;
        }
    }
}
