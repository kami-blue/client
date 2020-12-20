package me.zeroeightsix.kami.gui.rgui.component.listen;

/**
 * Created by 086 on 30/06/2017.
 */
public interface KeyListener {

    void onKeyDown(KeyEvent event);

    void onKeyUp(KeyEvent event);

    class KeyEvent {
        int key;

        public KeyEvent(int key) {
            this.key = key;
        }

        public int getKey() {
            return key;
        }

        public void setKey(int key) {
            this.key = key;
        }
    }
}
