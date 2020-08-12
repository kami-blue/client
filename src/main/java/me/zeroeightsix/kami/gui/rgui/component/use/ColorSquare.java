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
public class ColorSquare extends AbstractComponent {

    Color value;
    double xMinimum;
    double xMaximum;
    double yMinimum;
    double yMaximum;
    double step;
    String text;

    public ColorSquare(Color value, String text) {
        this.value = value;
        this.xMinimum = 0.0;
        this.xMaximum = 100.0;
        this.yMinimum = 0.0;
        this.yMaximum = 100.0;
        this.step = 1.0;
        this.text = text;

        addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(MouseButtonEvent event) { setValue(calculateXValue(event.getX()), calculateYValue(event.getY())); }

            @Override
            public void onMouseRelease(MouseButtonEvent event) { }

            @Override
            public void onMouseDrag(MouseButtonEvent event) { setValue(calculateXValue(event.getX()), calculateYValue(event.getY())); }

            @Override
            public void onMouseMove(MouseMoveEvent event) { }

            @Override
            public void onScroll(MouseScrollEvent event) { }
        });
    }

    private double calculateXValue(double x) {
        double d1 = x / getWidth();
        double d2 = (xMaximum - xMinimum);
        double s = d1 * d2 + xMinimum;

        return MathHelper.clamp(Math.floor((Math.round(s / step) * step) * 100) / 100, xMinimum, xMaximum); // round to 2 decimals & clamp min and max
    }

    private double calculateYValue(double y) {
        double d1 = y / getHeight();
        double d2 = (yMaximum - yMinimum);
        double s = d1 * d2 + yMinimum;

        return MathHelper.clamp(Math.floor((Math.round(s / step) * step) * 100) / 100, yMinimum, yMaximum); // round to 2 decimals & clamp min and max
    }


    public String getText() { return text; }

    public double getStep() { return step; }

    public Color getValue() { return value; }

    public double getxMaximum() { return xMaximum; }

    public double getxMinimum() { return xMinimum; }

    public double getyMaximum() { return yMaximum; }

    public double getyMinimum() { return yMinimum; }

    public void setValue(double saturation, double brightness) {
        float[] HSB = Color.RGBtoHSB(this.value.getRed(), this.value.getGreen(), this.value.getBlue(), null);
        Color clr = Color.getHSBColor(HSB[0], (float)saturation, (float)brightness);
        ColorSquare.ColorPoof.ColorPoofInfo info = new ColorSquare.ColorPoof.ColorPoofInfo(this.value, clr);
        callPoof(ColorSquare.ColorPoof.class, info);
        Color newValue = info.getNewValue();
        this.value = newValue;
    }

    public static abstract class ColorPoof<T extends Component, S extends ColorSquare.ColorPoof.ColorPoofInfo> extends Poof<T, S> {
        public static class ColorPoofInfo extends PoofInfo {
            Color oldValue;
            Color newValue;

            public ColorPoofInfo(Color oldValue, Color newValue) {
                ColorSquare.ColorPoof.ColorPoofInfo.this.oldValue = oldValue;
                ColorSquare.ColorPoof.ColorPoofInfo.this.newValue = newValue;
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
