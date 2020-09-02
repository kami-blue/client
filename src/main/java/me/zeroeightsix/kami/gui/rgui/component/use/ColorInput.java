package me.zeroeightsix.kami.gui.rgui.component.use;

import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen;
import me.zeroeightsix.kami.gui.kami.RootSmallFontRenderer;
import me.zeroeightsix.kami.gui.rgui.GUI;
import me.zeroeightsix.kami.gui.rgui.component.AbstractComponent;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.RenderListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.KeyListener;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.poof.use.Poof;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.util.HSBColourHolder;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import com.google.common.base.Joiner;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Guac on 18/08/2020.
 * Adapted from 086's InputField.
 */
public class ColorInput extends AbstractComponent {
    String label;
    HSBColourHolder value;
    int maxLength = 7;

    char echoChar = 0;

    InputState currentState = new InputState("", 0, false, 0, 0);

    long startRail = 0;
    float railT = 0;
    boolean rail = false;
    int railChar = 0;

    KeyListener inputListener;

    int railDelay = 500;
    int railRepeat = 1000 / 31;
    long lastTypeMS = 0;

    int undoT = 0;
    ArrayList<InputState> undoMap = new ArrayList<>();
    ArrayList<InputState> redoMap = new ArrayList<>();

    int scrollX = 0;

    boolean shift = false;

    RootSmallFontRenderer smallFontRenderer = null;

    // toggle settings
    boolean toggled;
    boolean doToggle = true;
    int boxSize = 8;

    public FontRenderer getFontRenderer() {
        return smallFontRenderer == null ? getTheme().getFontRenderer() : smallFontRenderer;
    }

    public void setFontRenderer(RootSmallFontRenderer fontRenderer) {
        this.smallFontRenderer = fontRenderer;
    }

    public ColorInput(HSBColourHolder value, String label) {
        this.value = value;
        this.label = label;
        currentState.text = label;

        addRenderListener(new RenderListener() {
            @Override
            public void onPreRender() {

            }

            @Override
            public void onPostRender() {
                if (!isFocused())
                    currentState.selection = false;

                int[] real = GUI.calculateRealPosition(ColorInput.this);
                int scale = DisplayGuiScreen.getScale();
                GL11.glScissor(real[0] * scale - getParent().getOriginOffsetX() - 1, Display.getHeight() - getHeight() * scale - real[1] * scale - 1, getWidth() * scale + getParent().getOriginOffsetX() + 1, getHeight() * scale + 1);
                GL11.glEnable(GL11.GL_SCISSOR_TEST);

                GL11.glTranslatef(-scrollX, 0, 0);

                RootSmallFontRenderer smallFontRenderer = new RootSmallFontRenderer();

                glLineWidth(1);

//                ColourHolder holder = ColourHolder.fromHex(fontRenderer.getBaseColor());
//                holder.setGLColour();
                GL11.glColor3f(1, 1, 1);

                boolean cursor = ((int) ((System.currentTimeMillis() - lastTypeMS) / 500) % 2 == 0) && isFocused();
                int x = 0;
                int i = 0;
                boolean selection = false;

                if (getCursorRow() == 0 && cursor) {
                    glBegin(GL_LINES);
                    {
                        glVertex2d(4, 0);
                        glVertex2d(4, smallFontRenderer.getFontHeight() + 1);
                    }
                    glEnd();
                }

                for (char c : getDisplayText().toCharArray()) {
                    int w = smallFontRenderer.getStringWidth(c + "");

                    if (getCurrentState().isSelection()) {
                        if (i == getCurrentState().getSelectionStart())
                            selection = true;
                    }

                    if (selection) {
                        glColor4f(0.2f, 0.6f, 1f, .3f);
                        glBegin(GL_QUADS);
                        {
                            glVertex2d(x + 2, 0);
                            glVertex2d(x + 2, smallFontRenderer.getFontHeight() + 1);
                            glVertex2d(x + w + 2, smallFontRenderer.getFontHeight() + 1);
                            glVertex2d(x + w + 2, 0);
                        }
                        glEnd();
                    }

                    i++;
                    x += w;

                    if (i == getCursorRow() && cursor && !getCurrentState().isSelection()) {
                        glBegin(GL_LINES);
                        {
                            glVertex2d(x + 2, 0);
                            glVertex2d(x + 2, smallFontRenderer.getFontHeight() + 1);
                        }
                        glEnd();
                    }

                    if (getCurrentState().isSelection()) {
                        if (i == getCurrentState().getSelectionEnd())
                            selection = false;
                    }
                }

                String s = getDisplayText();
                if (s.isEmpty()) s = " ";
                glEnable(GL_BLEND);
                smallFontRenderer.drawString(0, 2, s);

                glDisable(GL_TEXTURE_2D);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                GL11.glTranslatef(scrollX, 0, 0);

                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }
        });

        addKeyListener(inputListener = new KeyListener() {
            @Override
            public void onKeyDown(KeyEvent event) {
                lastTypeMS = System.currentTimeMillis();
                if (event.getKey() == Keyboard.KEY_BACK) {
                    if (getText().length() > 0) {
                        pushUndo();
                        if (currentState.selection) {
                            currentState.cursorRow = currentState.selectionEnd;
                            scroll();
                            remove(currentState.selectionEnd - currentState.selectionStart);
                            currentState.selection = false;
                        } else
                            remove(1);
                    }
                } else if (Keyboard.getEventCharacter() == 26) { // CTRL + Z
                    if (!undoMap.isEmpty()) {
                        redoMap.add(0, currentState.clone());
                        currentState = undoMap.get(0);
                        undoMap.remove(0);
                    }
                } else if (Keyboard.getEventCharacter() == 25) { // CTRL + Y
                    if (!redoMap.isEmpty()) {
                        undoMap.add(0, currentState.clone());
                        currentState = redoMap.get(0);
                        redoMap.remove(0);
                    }
                } else if (Keyboard.getEventCharacter() == 1) { // CTRL + A
                    currentState.selection = true;
                    currentState.selectionStart = 0;
                    currentState.selectionEnd = currentState.getText().length();
                } else if (event.getKey() == 54) { // shift
                    shift = true;
                } else if (event.getKey() == 1) { // ecape
                    currentState.selection = false;
                } else if (Keyboard.getEventCharacter() == 22) { // CTRL + V
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    try {
                        type((String) clipboard.getData(DataFlavor.stringFlavor));
                    } catch (UnsupportedFlavorException e) {
                    } catch (IOException e) {
                    }
                } else if (Keyboard.getEventCharacter() == 3) { // CTRL + C
                    if (currentState.selection) {
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        StringSelection selection = new StringSelection(currentState.getText().substring(currentState.selectionStart, currentState.selectionEnd));
                        clipboard.setContents(selection, selection);
                    }
                } else if (event.getKey() == 205) { // key right
                    if (currentState.cursorRow < getText().length()) {
                        if (shift) {
                            if (!currentState.selection) {
                                currentState.selectionStart = currentState.cursorRow;
                                currentState.selectionEnd = currentState.cursorRow;
                            }
                            currentState.selection = true;
                            currentState.selectionEnd = Math.min(getText().length(), currentState.selectionEnd + 1);
                        } else if (currentState.selection) {
                            currentState.selection = false;
                            currentState.cursorRow = currentState.selectionEnd;
                            scroll();
                        } else {
                            currentState.cursorRow = Math.min(getText().length(), currentState.cursorRow + 1);
                            scroll();
                        }
                    }
                } else if (event.getKey() == 203) { // key left
                    if (currentState.cursorRow > 0) {
                        if (shift) {
                            if (!currentState.selection) {
                                currentState.selectionStart = currentState.cursorRow;
                                currentState.selectionEnd = currentState.cursorRow;
                            }
                            currentState.selection = true;
                            currentState.selectionStart = Math.max(0, currentState.selectionStart - 1);
                        } else if (currentState.selection) {
                            currentState.selection = false;
                            currentState.cursorRow = currentState.selectionStart;
                            scroll();
                        } else {
                            currentState.cursorRow = Math.max(0, currentState.cursorRow - 1);
                            scroll();
                        }
                    }
                    //currentState.cursorRow = Math.max(0, currentState.cursorRow - 1);
                } else if (event.getKey() == Keyboard.KEY_RETURN) {
                    setValue(getText());
                    setText(label);
                } else {
                    if (Keyboard.getEventCharacter() != 0) {
                        pushUndo();
                        if (currentState.selection) {
                            currentState.cursorRow = currentState.selectionEnd;
                            remove(currentState.selectionEnd - currentState.selectionStart);
                            currentState.selection = false;
                            type(Keyboard.getEventCharacter() + "");
                        } else if (getText().length() < maxLength) {
                            type(Keyboard.getEventCharacter() + "");
                        }
                    }
                }

                if (event.getKey() == 42)
                    return;
                startRail = System.currentTimeMillis();
                railChar = event.getKey();
            }

            @Override
            public void onKeyUp(KeyEvent event) {
                rail = false;
                startRail = 0;

                if (event.getKey() == 54) { // shift
                    shift = false;
                }
            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(MouseButtonEvent event) {
                if (event.getX() > getWidth() - boxSize - 4 && event.getButton() == 0) {
                    setToggled(!toggled);
                    callPoof(ColorInput.ColorTogglePoof.class, new ColorInput.ColorTogglePoof.ColorTogglePoofInfo(ColorInput.ColorTogglePoof.ColorTogglePoofInfo.ColorTogglePoofInfoAction.TOGGLE));
                    if (toggled) {
                        callPoof(ColorInput.ColorTogglePoof.class, new ColorInput.ColorTogglePoof.ColorTogglePoofInfo(ColorInput.ColorTogglePoof.ColorTogglePoofInfo.ColorTogglePoofInfoAction.ENABLE));
                    } else {
                        callPoof(ColorInput.ColorTogglePoof.class, new ColorInput.ColorTogglePoof.ColorTogglePoofInfo(ColorInput.ColorTogglePoof.ColorTogglePoofInfo.ColorTogglePoofInfoAction.DISABLE));
                    }
                    return;
                }
                currentState.selection = false;
                if (getText().equals(label)) setText("");
                int x = -scrollX;
                int i = 0;
                for (char c : getText().toCharArray()) {
                    x += getFontRenderer().getStringWidth(c + "");
                    if (event.getX() < x) {
                        currentState.cursorRow = i;
                        scroll();
                        return;
                    }
                    i++;
                }
                currentState.cursorRow = i;
                scroll();
            }

            @Override
            public void onMouseRelease(MouseButtonEvent event) {

            }

            @Override
            public void onMouseDrag(MouseButtonEvent event) {
                currentState.selection = true;
                currentState.selectionStart = currentState.cursorRow;

                int x = -scrollX;
                int i = 0;
                for (char c : getText().toCharArray()) {
                    x += getFontRenderer().getStringWidth(c + "");
                    if (event.getX() < x) {
                        currentState.selectionEnd = i;
                        scroll();
                        break;
                    }
                    i++;
                }
                currentState.selectionEnd = i;

                int buf = currentState.cursorRow;
                currentState.cursorRow = i;
                scroll();
                currentState.cursorRow = buf;

                if (currentState.selectionStart > currentState.selectionEnd) {
                    int a = currentState.selectionStart;
                    currentState.selectionStart = currentState.selectionEnd;
                    currentState.selectionEnd = a;
                }

                if (currentState.selectionStart == currentState.selectionEnd)
                    currentState.selection = false;
            }

            @Override
            public void onMouseMove(MouseMoveEvent event) {

            }

            @Override
            public void onScroll(MouseScrollEvent event) {

            }
        });

        addRenderListener(new RenderListener() {
            @Override
            public void onPreRender() {
                if (startRail == 0) return;
                if (!rail) {
                    railT = System.currentTimeMillis() - startRail;
                    if (railT > railDelay) {
                        rail = true;
                        startRail = System.currentTimeMillis();
                    }
                } else {
                    railT = System.currentTimeMillis() - startRail;
                    if (railT > railRepeat) {
                        inputListener.onKeyDown(new KeyListener.KeyEvent(railChar));
                        startRail = System.currentTimeMillis();
                    }
                }
            }

            @Override
            public void onPostRender() {

            }
        });
    }

    public InputState getCurrentState() {
        return currentState;
    }

    public void type(String text) {
        try {
            setText(getText().substring(0, currentState.getCursorRow()) + text + getText().substring(currentState.getCursorRow()));
            currentState.cursorRow += text.length();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        scroll();
    }

    public void remove(int back) {
        back = Math.min(back, currentState.getCursorRow());
        boolean a = setText(getText().substring(0, Math.max(currentState.getCursorRow() - back, 0)) + getText().substring(currentState.getCursorRow()));
        if (!a)
            currentState.cursorRow -= back;
        scroll();
    }

    private void scroll() {
        int aX = 0;
        int i = 0;
        String a = "";
        for (char c : getText().toCharArray()) {
            aX += getFontRenderer().getStringWidth(c + "");
            i++;
            a += c;
            if (i >= currentState.cursorRow)
                break;
        }

        int diff = aX - scrollX;
        if (diff > getWidth()) {
            scrollX = aX - getWidth() + 8;
        } else if (diff < 0) {
            scrollX = aX + 8;
        }

        if (currentState.cursorRow == 0)
            scrollX = 0;
    }

    public int getCursorRow() {
        return currentState.getCursorRow();
    }

    private void pushUndo() {
        undoT++;
        if (undoT > 3) {
            undoT = 0;
            undoMap.add(0, currentState.clone());
        }
    }

    public String getText() {
        return currentState.getText();
    }

    public String getDisplayText() {
        return isEchoCharSet() ? getText().replaceAll(".", getEchoChar() + "") : getText();
    }

    public boolean setText(String text) {
        this.currentState.text = text;
        if (currentState.cursorRow > currentState.text.length()) {
            currentState.cursorRow = currentState.text.length();
            scroll();
            return true;
        }
        return false;
    }

    public char getEchoChar() {
        return echoChar;
    }

    public ColorInput setEchoChar(char echoChar) {
        this.echoChar = echoChar;
        return this;
    }

    public boolean isEchoCharSet() {
        return echoChar != 0;
    }

    public HSBColourHolder getValue() {
        return this.value;
    }

    public HSBColourHolder setValue(String value) {
        String str = sanitizeHex(value);
        HSBColourHolder val = new HSBColourHolder(str);
        ColorInput.ColorInputPoof.ColorInputPoofInfo info = new ColorInput.ColorInputPoof.ColorInputPoofInfo(this.value, val);
        callPoof(ColorInputPoof.class, info);
        HSBColourHolder newValue = info.getNewValue();
        this.value = newValue;
        return this.value;
    }

    public String sanitizeHex(String value) {
        String digits = value.toLowerCase();
        String validValues = "0123456789abcdef";
        ArrayList<Character> chars = new ArrayList();
        for (int i = 0; i < Math.max(digits.length(), (digits.startsWith("#") ? 7 : 6)); i++) {
            char c = i < digits.length() ? digits.charAt(i) : 'y';
            if (validValues.indexOf(c) > 0) {
                chars.add(digits.charAt(i));
            } else if (c == '#') {
            } else { chars.add('0'); }
        }
        return "#" + Joiner.on("").join(chars);
    }

    public int getSize() {
        return boxSize;
    }

    public void setSize(int size) {
        this.boxSize = size;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public boolean isToggled() {
        return toggled;
    }

    public class InputState {
        String text;
        int cursorRow;
        boolean selection;
        int selectionStart;
        int selectionEnd;

        public InputState(String text, int cursorRow, boolean selection, int selectionStart, int selectionEnd) {
            this.text = text;
            this.cursorRow = cursorRow;
            this.selection = selection;
            this.selectionStart = selectionStart;
            this.selectionEnd = selectionEnd;
        }

        protected InputState clone() {
            return new InputState(getText(), getCursorRow(), isSelection(), getSelectionStart(), getSelectionEnd());
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getCursorRow() {
            return cursorRow;
        }

        public void setCursorRow(int cursorRow) {
            this.cursorRow = cursorRow;
            scroll();
        }

        public boolean isSelection() {
            return selection;
        }

        public void setSelection(boolean selection) {
            this.selection = selection;
        }

        public int getSelectionStart() {
            return selectionStart;
        }

        public void setSelectionStart(int selectionStart) {
            this.selectionStart = selectionStart;
        }

        public int getSelectionEnd() {
            return selectionEnd;
        }

        public void setSelectionEnd(int selectionEnd) {
            this.selectionEnd = selectionEnd;
        }
    }

    public abstract static class ColorInputPoof<T extends ColorInput, S extends PoofInfo> extends Poof<T, S> {
        public static class ColorInputPoofInfo extends PoofInfo {
            HSBColourHolder oldValue;
            HSBColourHolder newValue;

            public ColorInputPoofInfo(HSBColourHolder oldValue, HSBColourHolder newValue) {
                ColorInput.ColorInputPoof.ColorInputPoofInfo.this.oldValue = oldValue;
                ColorInput.ColorInputPoof.ColorInputPoofInfo.this.newValue = newValue;
            }

            public HSBColourHolder getOldValue() {
                return oldValue;
            }

            public HSBColourHolder getNewValue() {
                return newValue;
            }

            public void setNewValue(HSBColourHolder newValue) { this.newValue = newValue; }
        }
    }

    public static abstract class ColorTogglePoof<T extends ColorInput, S extends ColorInput.ColorTogglePoof.ColorTogglePoofInfo> extends Poof<T, S> {
        ColorInput.ColorTogglePoof.ColorTogglePoofInfo info;

        public static class ColorTogglePoofInfo extends PoofInfo {
            ColorInput.ColorTogglePoof.ColorTogglePoofInfo.ColorTogglePoofInfoAction action;

            public ColorTogglePoofInfo(ColorInput.ColorTogglePoof.ColorTogglePoofInfo.ColorTogglePoofInfoAction action) {
                this.action = action;
            }

            public enum ColorTogglePoofInfoAction {
                TOGGLE, ENABLE, DISABLE
            }

            public ColorInput.ColorTogglePoof.ColorTogglePoofInfo.ColorTogglePoofInfoAction getAction() {
                return action;
            }
        }
    }
}
