package me.zeroeightsix.kami.gui.kami.theme.kami;

import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent;
import me.zeroeightsix.kami.gui.rgui.component.use.Label;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.util.graphics.font.KamiFontRenderer;
import org.lwjgl.opengl.GL11;

/**
 * Created by 086 on 2/08/2017.
 */
public class RootLabelUI<T extends Label> extends AbstractComponentUI<Label> {

    @Override
    public void renderComponent(Label component) {
        String[] lines = component.getLines();
        float y = 0;
        boolean shadow = component.isShadow();
        for (String s : lines) {
            float x = 0;
            if (component.getAlignment() == AlignedComponent.Alignment.CENTER)
                x = component.getWidth() / 2f - KamiFontRenderer.INSTANCE.getStringWidth(s) / 2f;
            else if (component.getAlignment() == AlignedComponent.Alignment.RIGHT)
                x = component.getWidth() - KamiFontRenderer.INSTANCE.getStringWidth(s);

            KamiFontRenderer.INSTANCE.drawString(s, x, y, shadow);
            y += KamiFontRenderer.INSTANCE.getFontHeight() + 3;
        }
    }

    @Override
    public void handleSizeComponent(Label component) {
        String[] lines = component.getLines();
        float y = 0;
        float w = 0;
        for (String s : lines) {
            w = Math.max(w, KamiFontRenderer.INSTANCE.getStringWidth(s));
            y += KamiFontRenderer.INSTANCE.getFontHeight() + 3f;
        }
        component.setWidth((int) w);
        component.setHeight((int) y);
    }
}
