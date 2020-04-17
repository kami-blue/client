package me.zeroeightsix.kami.gui.rgui.component.use;

import me.zeroeightsix.kami.gui.rgui.component.AbstractComponent;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.poof.use.Poof;

/**
 * Created by 086 on 25/06/2017.
 */
public class Button extends AbstractComponent {

    private String name;
    private String description;

    public Button(String name, String description) {
        this(name, description, 0, 0);
        addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(MouseButtonEvent event) {
                callPoof(ButtonPoof.class, new ButtonPoof.ButtonInfo(event.getButton(), event.getX(), event.getY()));
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
    }

    public Button(String name, String description, int x, int y) {
        this.name = name;
        this.description = description;
        setX(x);
        setY(y);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasDescription() {
        return description != null;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Nothing to wipe.
    @Override
    public void kill() {
    }


    public static abstract class ButtonPoof<T extends Button, S extends ButtonPoof.ButtonInfo> extends Poof<T, S> {
        ButtonInfo info;

        public static class ButtonInfo extends PoofInfo {
            int button;
            int x;
            int y;

            public ButtonInfo(int button, int x, int y) {
                this.button = button;
                this.x = x;
                this.y = y;
            }

            public int getX() {
                return x;
            }

            public int getY() {
                return y;
            }

            public int getButton() {
                return button;
            }
        }
    }

}
