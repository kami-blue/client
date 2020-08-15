package me.zeroeightsix.kami.gui.rgui.component.use;

import me.zeroeightsix.kami.gui.rgui.component.AbstractComponent;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.poof.use.Poof;
import net.minecraft.util.math.MathHelper;
import me.zeroeightsix.kami.util.HSBColourHolder;

/**
 * Created by Guac on 10/8/2020.
 */
public class ColorSlider extends AbstractComponent {

    HSBColourHolder value;
    double minimum;
    double maximum;
    double step;
    String text;

    public ColorSlider(HSBColourHolder value, String text) {
        this.value = value;
        this.minimum = 0.0;
        this.maximum = 359.0;
        this.step = 0.5;
        this.text = text;

        addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(MouseButtonEvent event) { setValue(calculateValue(event.getX())); }

            @Override
            public void onMouseRelease(MouseButtonEvent event) { }

            @Override
            public void onMouseDrag(MouseButtonEvent event) { setValue(calculateValue(event.getX())); }

            @Override
            public void onMouseMove(MouseMoveEvent event) { }

            @Override
            public void onScroll(MouseScrollEvent event) { }
        });
    }

    private double calculateValue(double x) {
        double d1 = x / getWidth();
        double d2 = (maximum - minimum);
        double s = d1 * d2 + minimum;
        double val = Math.floor((Math.round(s / step) * step) * 100) / 100;
        if (val > maximum) { val = maximum - 0.5; }
        return MathHelper.clamp(val, minimum, maximum); // round to 2 decimals & clamp min and max
    }

    public String getText() { return text; }

    public double getStep() { return step; }

    public HSBColourHolder getValue() { return value; }

    public double getMaximum() { return maximum; }

    public double getMinimum() { return minimum; }

    public void setValue(double hue) {
        HSBColourHolder old = this.value;
        this.value.setH((float)hue / 359);
        ColorSlider.ColorPoof.ColorPoofInfo info = new ColorSlider.ColorPoof.ColorPoofInfo(old, this.value);
        callPoof(ColorSlider.ColorPoof.class, info);
        HSBColourHolder newValue = info.getNewValue();
        this.value = newValue;
    }

    public static abstract class ColorPoof<T extends Component, S extends ColorSlider.ColorPoof.ColorPoofInfo> extends Poof<T, S> {
        public static class ColorPoofInfo extends PoofInfo {
            HSBColourHolder oldValue;
            HSBColourHolder newValue;

            public ColorPoofInfo(HSBColourHolder oldValue, HSBColourHolder newValue) {
                ColorSlider.ColorPoof.ColorPoofInfo.this.oldValue = oldValue;
                ColorSlider.ColorPoof.ColorPoofInfo.this.newValue = newValue;
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

}
