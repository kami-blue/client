// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami.component;

import me.zeroeightsix.kami.util.Bind;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.KeyListener;
import me.zeroeightsix.kami.module.Module;

public class BindButton extends EnumButton
{
    static String[] lookingFor;
    static String[] none;
    boolean waiting;
    Module m;
    boolean ctrl;
    boolean shift;
    boolean alt;
    
    public BindButton(final String name, final Module m) {
        super(name, BindButton.none);
        this.waiting = false;
        this.ctrl = false;
        this.shift = false;
        this.alt = false;
        this.m = m;
        final Bind bind = m.getBind();
        this.modes = new String[] { bind.toString() };
        this.addKeyListener(new KeyListener() {
            @Override
            public void onKeyDown(final KeyEvent event) {
                if (!BindButton.this.waiting) {
                    return;
                }
                final int key = event.getKey();
                if (BindButton.this.isShift(key)) {
                    BindButton.this.shift = true;
                    BindButton.this.modes = new String[] { (BindButton.this.ctrl ? "Ctrl+" : "") + (BindButton.this.alt ? "Alt+" : "") + "Shift+" };
                }
                else if (BindButton.this.isCtrl(key)) {
                    BindButton.this.ctrl = true;
                    BindButton.this.modes = new String[] { "Ctrl+" + (BindButton.this.alt ? "Alt+" : "") + (BindButton.this.shift ? "Shift+" : "") };
                }
                else if (BindButton.this.isAlt(key)) {
                    BindButton.this.alt = true;
                    BindButton.this.modes = new String[] { (BindButton.this.ctrl ? "Ctrl+" : "") + "Alt+" + (BindButton.this.shift ? "Shift+" : "") };
                }
                else if (key == 14) {
                    m.getBind().setCtrl(false);
                    m.getBind().setShift(false);
                    m.getBind().setAlt(false);
                    m.getBind().setKey(-1);
                    BindButton.this.modes = new String[] { m.getBind().toString() };
                    BindButton.this.waiting = false;
                }
                else {
                    m.getBind().setCtrl(BindButton.this.ctrl);
                    m.getBind().setShift(BindButton.this.shift);
                    m.getBind().setAlt(BindButton.this.alt);
                    m.getBind().setKey(key);
                    BindButton.this.modes = new String[] { m.getBind().toString() };
                    final BindButton this$0 = BindButton.this;
                    final BindButton this$2 = BindButton.this;
                    final BindButton this$3 = BindButton.this;
                    final boolean ctrl = false;
                    this$3.shift = ctrl;
                    this$2.alt = ctrl;
                    this$0.ctrl = ctrl;
                    BindButton.this.waiting = false;
                }
            }
            
            @Override
            public void onKeyUp(final KeyEvent event) {
            }
        });
        this.addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(final MouseButtonEvent event) {
                BindButton.this.setModes(BindButton.lookingFor);
                BindButton.this.waiting = true;
            }
            
            @Override
            public void onMouseRelease(final MouseButtonEvent event) {
            }
            
            @Override
            public void onMouseDrag(final MouseButtonEvent event) {
            }
            
            @Override
            public void onMouseMove(final MouseMoveEvent event) {
            }
            
            @Override
            public void onScroll(final MouseScrollEvent event) {
            }
        });
    }
    
    private boolean isAlt(final int key) {
        return key == 56 || key == 184;
    }
    
    private boolean isCtrl(final int key) {
        return key == 29 || key == 157;
    }
    
    private boolean isShift(final int key) {
        return key == 42 || key == 54;
    }
    
    static {
        BindButton.lookingFor = new String[] { "_" };
        BindButton.none = new String[] { "NONE" };
    }
}
