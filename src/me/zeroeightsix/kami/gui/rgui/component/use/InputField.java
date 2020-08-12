// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.rgui.component.use;

import me.zeroeightsix.kami.gui.rgui.poof.use.Poof;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.poof.IPoof;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.DataFlavor;
import java.awt.Toolkit;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Display;
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.GUI;
import me.zeroeightsix.kami.gui.rgui.component.listen.RenderListener;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import java.util.ArrayList;
import me.zeroeightsix.kami.gui.rgui.component.listen.KeyListener;
import me.zeroeightsix.kami.gui.rgui.component.AbstractComponent;

public class InputField extends AbstractComponent
{
    char echoChar;
    InputState currentState;
    long startRail;
    float railT;
    boolean rail;
    int railChar;
    KeyListener inputListener;
    int railDelay;
    int railRepeat;
    long lastTypeMS;
    int undoT;
    ArrayList<InputState> undoMap;
    ArrayList<InputState> redoMap;
    int scrollX;
    boolean shift;
    FontRenderer fontRenderer;
    
    public FontRenderer getFontRenderer() {
        return (this.fontRenderer == null) ? this.getTheme().getFontRenderer() : this.fontRenderer;
    }
    
    public void setFontRenderer(final FontRenderer fontRenderer) {
        this.fontRenderer = fontRenderer;
    }
    
    public InputField(final String text) {
        this.echoChar = '\0';
        this.currentState = new InputState("", 0, false, 0, 0);
        this.startRail = 0L;
        this.railT = 0.0f;
        this.rail = false;
        this.railChar = 0;
        this.railDelay = 500;
        this.railRepeat = 32;
        this.lastTypeMS = 0L;
        this.undoT = 0;
        this.undoMap = new ArrayList<InputState>();
        this.redoMap = new ArrayList<InputState>();
        this.scrollX = 0;
        this.shift = false;
        this.fontRenderer = null;
        this.currentState.text = text;
        this.addRenderListener(new RenderListener() {
            @Override
            public void onPreRender() {
            }
            
            @Override
            public void onPostRender() {
                if (!InputField.this.isFocussed()) {
                    InputField.this.currentState.selection = false;
                }
                final int[] real = GUI.calculateRealPosition(InputField.this);
                final int scale = DisplayGuiScreen.getScale();
                GL11.glScissor(real[0] * scale - InputField.this.getParent().getOriginOffsetX() - 1, Display.getHeight() - InputField.this.getHeight() * scale - real[1] * scale - 1, InputField.this.getWidth() * scale + InputField.this.getParent().getOriginOffsetX() + 1, InputField.this.getHeight() * scale + 1);
                GL11.glEnable(3089);
                GL11.glTranslatef((float)(-InputField.this.scrollX), 0.0f, 0.0f);
                final FontRenderer fontRenderer = InputField.this.getFontRenderer();
                GL11.glLineWidth(1.0f);
                GL11.glColor3f(1.0f, 1.0f, 1.0f);
                final boolean cursor = (int)((System.currentTimeMillis() - InputField.this.lastTypeMS) / 500L) % 2 == 0 && InputField.this.isFocussed();
                int x = 0;
                int i = 0;
                boolean selection = false;
                if (InputField.this.getCursorRow() == 0 && cursor) {
                    GL11.glBegin(1);
                    GL11.glVertex2d(4.0, 2.0);
                    GL11.glVertex2d(4.0, (double)(fontRenderer.getFontHeight() - 1));
                    GL11.glEnd();
                }
                for (final char c : InputField.this.getDisplayText().toCharArray()) {
                    final int w = fontRenderer.getStringWidth(c + "");
                    if (InputField.this.getCurrentState().isSelection() && i == InputField.this.getCurrentState().getSelectionStart()) {
                        selection = true;
                    }
                    if (selection) {
                        GL11.glColor4f(0.2f, 0.6f, 1.0f, 0.3f);
                        GL11.glBegin(7);
                        GL11.glVertex2d((double)(x + 2), 2.0);
                        GL11.glVertex2d((double)(x + 2), (double)(fontRenderer.getFontHeight() - 2));
                        GL11.glVertex2d((double)(x + w + 2), (double)(fontRenderer.getFontHeight() - 2));
                        GL11.glVertex2d((double)(x + w + 2), 2.0);
                        GL11.glEnd();
                    }
                    ++i;
                    x += w;
                    if (i == InputField.this.getCursorRow() && cursor && !InputField.this.getCurrentState().isSelection()) {
                        GL11.glBegin(1);
                        GL11.glVertex2d((double)(x + 2), 2.0);
                        GL11.glVertex2d((double)(x + 2), (double)fontRenderer.getFontHeight());
                        GL11.glEnd();
                    }
                    if (InputField.this.getCurrentState().isSelection() && i == InputField.this.getCurrentState().getSelectionEnd()) {
                        selection = false;
                    }
                }
                String s = InputField.this.getDisplayText();
                if (s.isEmpty()) {
                    s = " ";
                }
                GL11.glEnable(3042);
                fontRenderer.drawString(0, -1, s);
                GL11.glDisable(3553);
                GL11.glBlendFunc(770, 771);
                GL11.glTranslatef((float)InputField.this.scrollX, 0.0f, 0.0f);
                GL11.glDisable(3089);
            }
        });
        this.addKeyListener(this.inputListener = new KeyListener() {
            @Override
            public void onKeyDown(final KeyEvent event) {
                InputField.this.lastTypeMS = System.currentTimeMillis();
                if (event.getKey() == 14) {
                    if (InputField.this.getText().length() > 0) {
                        InputField.this.pushUndo();
                        if (InputField.this.currentState.selection) {
                            InputField.this.currentState.cursorRow = InputField.this.currentState.selectionEnd;
                            InputField.this.scroll();
                            InputField.this.remove(InputField.this.currentState.selectionEnd - InputField.this.currentState.selectionStart);
                            InputField.this.currentState.selection = false;
                        }
                        else {
                            InputField.this.remove(1);
                        }
                    }
                }
                else if (Keyboard.getEventCharacter() == '\u001a') {
                    if (!InputField.this.undoMap.isEmpty()) {
                        InputField.this.redoMap.add(0, InputField.this.currentState.clone());
                        InputField.this.currentState = InputField.this.undoMap.get(0);
                        InputField.this.undoMap.remove(0);
                    }
                }
                else if (Keyboard.getEventCharacter() == '\u0019') {
                    if (!InputField.this.redoMap.isEmpty()) {
                        InputField.this.undoMap.add(0, InputField.this.currentState.clone());
                        InputField.this.currentState = InputField.this.redoMap.get(0);
                        InputField.this.redoMap.remove(0);
                    }
                }
                else if (Keyboard.getEventCharacter() == '\u0001') {
                    InputField.this.currentState.selection = true;
                    InputField.this.currentState.selectionStart = 0;
                    InputField.this.currentState.selectionEnd = InputField.this.currentState.getText().length();
                }
                else if (event.getKey() == 54) {
                    InputField.this.shift = true;
                }
                else if (event.getKey() == 1) {
                    InputField.this.currentState.selection = false;
                }
                else if (Keyboard.getEventCharacter() == '\u0016') {
                    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    try {
                        InputField.this.type((String)clipboard.getData(DataFlavor.stringFlavor));
                    }
                    catch (UnsupportedFlavorException ex) {}
                    catch (IOException ex2) {}
                }
                else if (Keyboard.getEventCharacter() == '\u0003') {
                    if (InputField.this.currentState.selection) {
                        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        final StringSelection selection = new StringSelection(InputField.this.currentState.getText().substring(InputField.this.currentState.selectionStart, InputField.this.currentState.selectionEnd));
                        clipboard.setContents(selection, selection);
                    }
                }
                else if (event.getKey() == 205) {
                    if (InputField.this.currentState.cursorRow < InputField.this.getText().length()) {
                        if (InputField.this.shift) {
                            if (!InputField.this.currentState.selection) {
                                InputField.this.currentState.selectionStart = InputField.this.currentState.cursorRow;
                                InputField.this.currentState.selectionEnd = InputField.this.currentState.cursorRow;
                            }
                            InputField.this.currentState.selection = true;
                            InputField.this.currentState.selectionEnd = Math.min(InputField.this.getText().length(), InputField.this.currentState.selectionEnd + 1);
                        }
                        else if (InputField.this.currentState.selection) {
                            InputField.this.currentState.selection = false;
                            InputField.this.currentState.cursorRow = InputField.this.currentState.selectionEnd;
                            InputField.this.scroll();
                        }
                        else {
                            InputField.this.currentState.cursorRow = Math.min(InputField.this.getText().length(), InputField.this.currentState.cursorRow + 1);
                            InputField.this.scroll();
                        }
                    }
                }
                else if (event.getKey() == 203) {
                    if (InputField.this.currentState.cursorRow > 0) {
                        if (InputField.this.shift) {
                            if (!InputField.this.currentState.selection) {
                                InputField.this.currentState.selectionStart = InputField.this.currentState.cursorRow;
                                InputField.this.currentState.selectionEnd = InputField.this.currentState.cursorRow;
                            }
                            InputField.this.currentState.selection = true;
                            InputField.this.currentState.selectionStart = Math.max(0, InputField.this.currentState.selectionStart - 1);
                        }
                        else if (InputField.this.currentState.selection) {
                            InputField.this.currentState.selection = false;
                            InputField.this.currentState.cursorRow = InputField.this.currentState.selectionStart;
                            InputField.this.scroll();
                        }
                        else {
                            InputField.this.currentState.cursorRow = Math.max(0, InputField.this.currentState.cursorRow - 1);
                            InputField.this.scroll();
                        }
                    }
                }
                else if (Keyboard.getEventCharacter() != '\0') {
                    InputField.this.pushUndo();
                    if (InputField.this.currentState.selection) {
                        InputField.this.currentState.cursorRow = InputField.this.currentState.selectionEnd;
                        InputField.this.remove(InputField.this.currentState.selectionEnd - InputField.this.currentState.selectionStart);
                        InputField.this.currentState.selection = false;
                    }
                    InputField.this.type(Keyboard.getEventCharacter() + "");
                }
                if (event.getKey() == 42) {
                    return;
                }
                InputField.this.startRail = System.currentTimeMillis();
                InputField.this.railChar = event.getKey();
            }
            
            @Override
            public void onKeyUp(final KeyEvent event) {
                InputField.this.rail = false;
                InputField.this.startRail = 0L;
                if (event.getKey() == 54) {
                    InputField.this.shift = false;
                }
            }
        });
        this.addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(final MouseButtonEvent event) {
                InputField.this.currentState.selection = false;
                int x = -InputField.this.scrollX;
                int i = 0;
                for (final char c : InputField.this.getText().toCharArray()) {
                    x += InputField.this.getFontRenderer().getStringWidth(c + "");
                    if (event.getX() < x) {
                        InputField.this.currentState.cursorRow = i;
                        InputField.this.scroll();
                        return;
                    }
                    ++i;
                }
                InputField.this.currentState.cursorRow = i;
                InputField.this.scroll();
            }
            
            @Override
            public void onMouseRelease(final MouseButtonEvent event) {
            }
            
            @Override
            public void onMouseDrag(final MouseButtonEvent event) {
                InputField.this.currentState.selection = true;
                InputField.this.currentState.selectionStart = InputField.this.currentState.cursorRow;
                int x = -InputField.this.scrollX;
                int i = 0;
                for (final char c : InputField.this.getText().toCharArray()) {
                    x += InputField.this.getFontRenderer().getStringWidth(c + "");
                    if (event.getX() < x) {
                        InputField.this.currentState.selectionEnd = i;
                        InputField.this.scroll();
                        break;
                    }
                    ++i;
                }
                InputField.this.currentState.selectionEnd = i;
                final int buf = InputField.this.currentState.cursorRow;
                InputField.this.currentState.cursorRow = i;
                InputField.this.scroll();
                InputField.this.currentState.cursorRow = buf;
                if (InputField.this.currentState.selectionStart > InputField.this.currentState.selectionEnd) {
                    final int a = InputField.this.currentState.selectionStart;
                    InputField.this.currentState.selectionStart = InputField.this.currentState.selectionEnd;
                    InputField.this.currentState.selectionEnd = a;
                }
                if (InputField.this.currentState.selectionStart == InputField.this.currentState.selectionEnd) {
                    InputField.this.currentState.selection = false;
                }
            }
            
            @Override
            public void onMouseMove(final MouseMoveEvent event) {
            }
            
            @Override
            public void onScroll(final MouseScrollEvent event) {
            }
        });
        this.addRenderListener(new RenderListener() {
            @Override
            public void onPreRender() {
                if (InputField.this.startRail == 0L) {
                    return;
                }
                if (!InputField.this.rail) {
                    InputField.this.railT = (float)(System.currentTimeMillis() - InputField.this.startRail);
                    if (InputField.this.railT > InputField.this.railDelay) {
                        InputField.this.rail = true;
                        InputField.this.startRail = System.currentTimeMillis();
                    }
                }
                else {
                    InputField.this.railT = (float)(System.currentTimeMillis() - InputField.this.startRail);
                    if (InputField.this.railT > InputField.this.railRepeat) {
                        InputField.this.inputListener.onKeyDown(new KeyListener.KeyEvent(InputField.this.railChar));
                        InputField.this.startRail = System.currentTimeMillis();
                    }
                }
            }
            
            @Override
            public void onPostRender() {
            }
        });
    }
    
    public InputField() {
        this("");
    }
    
    public InputField(final int width) {
        this("");
    }
    
    public InputState getCurrentState() {
        return this.currentState;
    }
    
    public void type(final String text) {
        try {
            this.setText(this.getText().substring(0, this.currentState.getCursorRow()) + text + this.getText().substring(this.currentState.getCursorRow()));
            final InputState currentState = this.currentState;
            currentState.cursorRow += text.length();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        this.scroll();
    }
    
    public void remove(int back) {
        back = Math.min(back, this.currentState.getCursorRow());
        final boolean a = this.setText(this.getText().substring(0, Math.max(this.currentState.getCursorRow() - back, 0)) + this.getText().substring(this.currentState.getCursorRow()));
        if (!a) {
            final InputState currentState = this.currentState;
            currentState.cursorRow -= back;
        }
        this.scroll();
    }
    
    private void scroll() {
        int aX = 0;
        int i = 0;
        String a = "";
        for (final char c : this.getText().toCharArray()) {
            aX += this.getFontRenderer().getStringWidth(c + "");
            ++i;
            a += c;
            if (i >= this.currentState.cursorRow) {
                break;
            }
        }
        final int diff = aX - this.scrollX;
        if (diff > this.getWidth()) {
            this.scrollX = aX - this.getWidth() + 8;
        }
        else if (diff < 0) {
            this.scrollX = aX + 8;
        }
        if (this.currentState.cursorRow == 0) {
            this.scrollX = 0;
        }
    }
    
    public int getCursorRow() {
        return this.currentState.getCursorRow();
    }
    
    private void pushUndo() {
        ++this.undoT;
        if (this.undoT > 3) {
            this.undoT = 0;
            this.undoMap.add(0, this.currentState.clone());
        }
    }
    
    public String getText() {
        return this.currentState.getText();
    }
    
    public String getDisplayText() {
        return this.isEchoCharSet() ? this.getText().replaceAll(".", this.getEchoChar() + "") : this.getText();
    }
    
    public boolean setText(final String text) {
        this.currentState.text = text;
        this.callPoof(InputFieldTextPoof.class, null);
        if (this.currentState.cursorRow > this.currentState.text.length()) {
            this.currentState.cursorRow = this.currentState.text.length();
            this.scroll();
            return true;
        }
        return false;
    }
    
    public char getEchoChar() {
        return this.echoChar;
    }
    
    public InputField setEchoChar(final char echoChar) {
        this.echoChar = echoChar;
        return this;
    }
    
    public boolean isEchoCharSet() {
        return this.echoChar != '\0';
    }
    
    public class InputState
    {
        String text;
        int cursorRow;
        boolean selection;
        int selectionStart;
        int selectionEnd;
        
        public InputState(final String text, final int cursorRow, final boolean selection, final int selectionStart, final int selectionEnd) {
            this.text = text;
            this.cursorRow = cursorRow;
            this.selection = selection;
            this.selectionStart = selectionStart;
            this.selectionEnd = selectionEnd;
        }
        
        @Override
        protected InputState clone() {
            return new InputState(this.getText(), this.getCursorRow(), this.isSelection(), this.getSelectionStart(), this.getSelectionEnd());
        }
        
        public String getText() {
            return this.text;
        }
        
        public void setText(final String text) {
            this.text = text;
        }
        
        public int getCursorRow() {
            return this.cursorRow;
        }
        
        public void setCursorRow(final int cursorRow) {
            this.cursorRow = cursorRow;
            InputField.this.scroll();
        }
        
        public boolean isSelection() {
            return this.selection;
        }
        
        public void setSelection(final boolean selection) {
            this.selection = selection;
        }
        
        public int getSelectionStart() {
            return this.selectionStart;
        }
        
        public void setSelectionStart(final int selectionStart) {
            this.selectionStart = selectionStart;
        }
        
        public int getSelectionEnd() {
            return this.selectionEnd;
        }
        
        public void setSelectionEnd(final int selectionEnd) {
            this.selectionEnd = selectionEnd;
        }
    }
    
    public abstract static class InputFieldTextPoof<T extends InputField, S extends PoofInfo> extends Poof<T, S>
    {
    }
}
