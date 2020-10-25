package me.zeroeightsix.kami.gui.kami.component;

import me.zeroeightsix.kami.gui.rgui.component.listen.KeyListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.impl.other.BindSetting;
import me.zeroeightsix.kami.util.Bind;
import org.lwjgl.input.Keyboard;

/**
 * Created by 086 on 8/08/2017.
 */
public class BindButton extends EnumButton {

    static String[] lookingFor = new String[]{"_"};
    static String[] none = new String[]{"NONE"};
    public static boolean waiting = false;
    Module m;

    boolean ctrl = false, shift = false, alt = false;

    public BindButton(String name, String description, Module m, BindSetting bindSetting) {
        super(name, description, none);
        this.m = m;

        Bind bind = bindSetting.getValue();
        modes = new String[]{bind.toString()};

        addKeyListener(new KeyListener() {
            @Override
            public void onKeyDown(KeyEvent event) {
                if (!waiting) return;
                int key = event.getKey();

                if (isShift(key)) {
                    shift = true;
                    modes = new String[]{(ctrl ? "Ctrl+" : "") + (alt ? "Alt+" : "") + "Shift+"};
                } else if (isCtrl(key)) {
                    ctrl = true;
                    modes = new String[]{"Ctrl+" + (alt ? "Alt+" : "") + (shift ? "Shift+" : "")};
                } else if (isAlt(key)) {
                    alt = true;
                    modes = new String[]{(ctrl ? "Ctrl+" : "") + "Alt+" + (shift ? "Shift+" : "")};
                } else if (key == Keyboard.KEY_BACK || key == Keyboard.KEY_DELETE) {
                    bindSetting.getValue().setCtrl(false);
                    bindSetting.getValue().setShift(false);
                    bindSetting.getValue().setAlt(false);
                    bindSetting.getValue().setKey(-1);
                    modes = new String[]{bindSetting.getValue().toString()};
                    waiting = false;
                } else {
                    bindSetting.getValue().setCtrl(ctrl);
                    bindSetting.getValue().setShift(shift);
                    bindSetting.getValue().setAlt(alt);
                    bindSetting.getValue().setKey(key);
                    modes = new String[]{bindSetting.getValue().toString()};
                    ctrl = alt = shift = false;
                    waiting = false;
                }
            }

            @Override
            public void onKeyUp(KeyEvent event) {
            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(MouseButtonEvent event) {
                setModes(lookingFor);
                waiting = true;
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

    private boolean isAlt(int key) {
        return key == Keyboard.KEY_LMENU || key == Keyboard.KEY_RMENU;
    }

    private boolean isCtrl(int key) {
        return key == Keyboard.KEY_LCONTROL || key == Keyboard.KEY_RCONTROL;
    }

    private boolean isShift(int key) {
        return key == Keyboard.KEY_LSHIFT || key == Keyboard.KEY_RSHIFT;
    }


}
