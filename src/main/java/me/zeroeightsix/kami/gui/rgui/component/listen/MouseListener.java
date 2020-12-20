package me.zeroeightsix.kami.gui.rgui.component.listen;

import me.zeroeightsix.kami.gui.rgui.component.Component;

/**
 * Created by 086 on 26/06/2017.
 */
public interface MouseListener {
    void onMouseDown(MouseButtonEvent event);

    void onMouseRelease(MouseButtonEvent event);

    void onMouseDrag(MouseButtonEvent event);

    void onMouseMove(MouseMoveEvent event);

    void onScroll(MouseScrollEvent event);

    class MouseMoveEvent {
        boolean cancelled = false;
        int x;
        int y;
        int oldX;
        int oldY;
        Component component;

        public MouseMoveEvent(int x, int y, int oldX, int oldY, Component component) {
            this.x = x;
            this.y = y;
            this.oldX = oldX;
            this.oldY = oldY;
            this.component = component;
        }

        public Component getComponent() {
            return component;
        }

        public int getOldX() {
            return oldX;
        }

        public int getOldY() {
            return oldY;
        }

        public int getY() {
            return y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }

    class MouseButtonEvent {
        int x;
        int y;
        int button;
        Component component;

        boolean cancelled = false;

        public MouseButtonEvent(int x, int y, int button, Component component) {
            this.x = x;
            this.y = y;
            this.button = button;
            this.component = component;
        }

        public Component getComponent() {
            return component;
        }

        public void setButton(int button) {
            this.button = button;
        }

        public int getButton() {
            return button;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getX() {
            return x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getY() {
            return y;
        }

        public void cancel() {
            cancelled = true;
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }

    class MouseScrollEvent {
        int x;
        int y;
        boolean up;
        Component component;
        private boolean cancelled;

        public MouseScrollEvent(int x, int y, boolean up, Component component) {
            this.x = x;
            this.y = y;
            this.up = up;
            this.component = component;
        }

        public Component getComponent() {
            return component;
        }

        public boolean isUp() {
            return up;
        }

        public void setUp(boolean up) {
            this.up = up;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getX() {
            return x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getY() {
            return y;
        }

        public void cancel() {
            cancelled = true;
        }

        public boolean isCancelled() {
            return cancelled;
        }

    }
}
