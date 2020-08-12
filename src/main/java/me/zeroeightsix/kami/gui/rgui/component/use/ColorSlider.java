package me.zeroeightsix.kami.gui.rgui.component.use;

import me.zeroeightsix.kami.gui.rgui.component.AbstractComponent;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.poof.use.Poof;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

/**
 * Created by Guac on 10/8/2020.
 */
public class ColorSlider extends AbstractComponent {

    Color value;
    double minimum;
    double maximum;
    double step;
    String text;

    public ColorSlider(Color value, String text) {
        this.value = value;
        this.minimum = 0.0;
        this.maximum = 359.0;
        this.step = 1.0;
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

        return MathHelper.clamp(Math.floor((Math.round(s / step) * step) * 100) / 100, minimum, maximum); // round to 2 decimals & clamp min and max
    }

    public String getText() { return text; }

    public double getStep() { return step; }

    public Color getValue() { return value; }

    public double getMaximum() { return maximum; }

    public double getMinimum() { return minimum; }

    public void setValue(double hue) {
        float[] HSB = Color.RGBtoHSB(this.value.getRed(), this.value.getGreen(), this.value.getBlue(), null);
        Color clr = Color.getHSBColor(((float)hue / 359), HSB[1], HSB[2]);
        ColorSlider.ColorPoof.ColorPoofInfo info = new ColorSlider.ColorPoof.ColorPoofInfo(this.value, clr);
        callPoof(ColorSlider.ColorPoof.class, info);
        Color newValue = info.getNewValue();
        this.value = newValue;
    }

    public static abstract class ColorPoof<T extends Component, S extends ColorSlider.ColorPoof.ColorPoofInfo> extends Poof<T, S> {
        public static class ColorPoofInfo extends PoofInfo {
            Color oldValue;
            Color newValue;

            public ColorPoofInfo(Color oldValue, Color newValue) {
                ColorSlider.ColorPoof.ColorPoofInfo.this.oldValue = oldValue;
                ColorSlider.ColorPoof.ColorPoofInfo.this.newValue = newValue;
            }

            public Color getOldValue() {
                return oldValue;
            }

            public Color getNewValue() {
                return newValue;
            }

            public void setNewValue(Color newValue) { this.newValue = newValue; }
        }
    }

}
