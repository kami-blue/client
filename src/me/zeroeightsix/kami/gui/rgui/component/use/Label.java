// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.rgui.component.use;

import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent;

public class Label extends AlignedComponent
{
    String text;
    boolean multiline;
    boolean shadow;
    FontRenderer fontRenderer;
    
    public Label(final String text) {
        this(text, false);
    }
    
    public Label(final String text, final boolean multiline) {
        this.text = text;
        this.multiline = multiline;
        this.setAlignment(Alignment.LEFT);
    }
    
    public String getText() {
        return this.text;
    }
    
    public String[] getLines() {
        String[] lines;
        if (this.isMultiline()) {
            lines = this.getText().split(System.lineSeparator());
        }
        else {
            lines = new String[] { this.getText() };
        }
        return lines;
    }
    
    public void setText(final String text) {
        this.text = text;
        this.getTheme().getUIForComponent(this).handleSizeComponent(this);
    }
    
    public void addText(final String add) {
        this.setText(this.getText() + add);
    }
    
    public void addLine(final String add) {
        if (this.getText().isEmpty()) {
            this.setText(add);
        }
        else {
            this.setText(this.getText() + System.lineSeparator() + add);
            this.multiline = true;
        }
    }
    
    public boolean isMultiline() {
        return this.multiline;
    }
    
    public void setMultiline(final boolean multiline) {
        this.multiline = multiline;
    }
    
    public boolean isShadow() {
        return this.shadow;
    }
    
    public FontRenderer getFontRenderer() {
        return this.fontRenderer;
    }
    
    public void setFontRenderer(final FontRenderer fontRenderer) {
        this.fontRenderer = fontRenderer;
    }
    
    @Override
    public void setTheme(final Theme theme) {
        super.setTheme(theme);
        this.setFontRenderer(theme.getFontRenderer());
        this.getTheme().getUIForComponent(this).handleSizeComponent(this);
    }
    
    public void setShadow(final boolean shadow) {
        this.shadow = shadow;
    }
}
