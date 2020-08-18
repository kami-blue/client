package me.zeroeightsix.kami.util;

import me.zeroeightsix.kami.command.commands.BindCommand;
import org.lwjgl.input.Keyboard;

/**
 * Created by 086 on 9/10/2018.
 */
public class Bind {

    boolean ctrl;
    boolean alt;
    boolean shift;
    int key;

    public Bind(boolean ctrl, boolean alt, boolean shift, int key) {
        this.ctrl = ctrl;
        this.alt = alt;
        this.shift = shift;
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public boolean isCtrl() {
        return ctrl;
    }

    public boolean isAlt() {
        return alt;
    }

    public boolean isShift() {
        return shift;
    }

    public boolean isEmpty() {
        return !ctrl && !shift && !alt && key < 0;
    }

    public void setAlt(boolean alt) {
        this.alt = alt;
    }

    public void setCtrl(boolean ctrl) {
        this.ctrl = ctrl;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setShift(boolean shift) {
        this.shift = shift;
    }

    @Override
    public String toString() {
        return isEmpty() ? "None" : (isCtrl() ? "Ctrl+" : "") + (isAlt() ? "Alt+" : "") + (isShift() ? "Shift+" : "") + (key < 0 ? "None" : capitalise(Keyboard.getKeyName(key)));
    }

    public boolean isDown(int eventKey) {
        return !isEmpty() && (!BindCommand.modifiersEnabled.getValue() || (isShift() == isShiftDown()) && (isCtrl() == isCtrlDown()) && (isAlt() == isAltDown())) && eventKey == getKey();
    }

    public static boolean isShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    public static boolean isCtrlDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }

    public static boolean isAltDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
    }

    public String capitalise(String str) {
        if (str.isEmpty()) return "";
        return Character.toUpperCase(str.charAt(0)) + (str.length() != 1 ? str.substring(1).toLowerCase() : "");
    }

    public static Bind none() {
        return new Bind(false, false, false, -1);
    }

}
