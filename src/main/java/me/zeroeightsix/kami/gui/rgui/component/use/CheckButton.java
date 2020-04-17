package me.zeroeightsix.kami.gui.rgui.component.use;

import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.poof.use.Poof;

/**
 * Created by 086 on 4/08/2017.
 */
public class CheckButton extends Button {
    boolean toggled;

    public CheckButton(String name, String description) {
        this(name, description, 0, 0);
    }

    public CheckButton(String name, String description, int x, int y) {
        super(name, description, x, y);
        addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(MouseButtonEvent event) {
                if (event.getButton() != 0) return;
                toggled = !toggled;
                callPoof(CheckButtonPoof.class, new CheckButtonPoof.CheckButtonPoofInfo(CheckButtonPoof.CheckButtonPoofInfo.CheckButtonPoofInfoAction.TOGGLE));
                if (toggled) {
                    callPoof(CheckButtonPoof.class, new CheckButtonPoof.CheckButtonPoofInfo(CheckButtonPoof.CheckButtonPoofInfo.CheckButtonPoofInfoAction.ENABLE));
                } else {
                    callPoof(CheckButtonPoof.class, new CheckButtonPoof.CheckButtonPoofInfo(CheckButtonPoof.CheckButtonPoofInfo.CheckButtonPoofInfoAction.DISABLE));
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
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public boolean isToggled() {
        return toggled;
    }

    public static abstract class CheckButtonPoof<T extends CheckButton, S extends CheckButtonPoof.CheckButtonPoofInfo> extends Poof<T, S> {
        CheckButtonPoofInfo info;

        public static class CheckButtonPoofInfo extends PoofInfo {
            CheckButtonPoofInfoAction action;

            public CheckButtonPoofInfo(CheckButtonPoofInfoAction action) {
                this.action = action;
            }

            public enum CheckButtonPoofInfoAction {
                TOGGLE, ENABLE, DISABLE
            }

            public CheckButtonPoofInfoAction getAction() {
                return action;
            }
        }
    }
}
