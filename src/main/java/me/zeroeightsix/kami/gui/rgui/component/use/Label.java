package me.zeroeightsix.kami.gui.rgui.component.use;

import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;

/**
 * Created by 086 on 2/08/2017.
 */
public class Label extends AlignedComponent {

    String text;
    boolean multiline;

    boolean shadow;

    public Label(String text) {
        this(text, false);
    }

    public Label(String text, boolean multiline) {
        this.text = text;
        this.multiline = multiline;
        setAlignment(AlignedComponent.Alignment.LEFT);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        getTheme().getUIForComponent(this).handleSizeComponent(this);
    }

    public String[] getLines() {
        String[] lines;
        if (isMultiline()) {
            lines = getText().split(System.lineSeparator());
        } else {
            lines = new String[]{getText()};
        }
        return lines;
    }

    public void addText(String add) {
        setText(getText() + add);
    }

    public void addLine(String add) {
        if (getText().isEmpty()) {
            setText(add);
        } else {
            setText(getText() + System.lineSeparator() + add);
            multiline = true;
        }
    }

    public boolean isMultiline() {
        return multiline;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public boolean isShadow() {
        return shadow;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    @Override
    public void setTheme(Theme theme) {
        super.setTheme(theme);
        getTheme().getUIForComponent(this).handleSizeComponent(this);
    }
}
