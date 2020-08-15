package me.zeroeightsix.kami.gui.rgui.component.use;

import me.zeroeightsix.kami.gui.rgui.component.AbstractComponent;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.poof.use.Poof;
import me.zeroeightsix.kami.util.HSBColourHolder;
import net.minecraft.util.math.MathHelper;

/**
 * Created by Guac on 10/8/2020.
 */
public class ColorSquare extends AbstractComponent {

    HSBColourHolder value;
    double xMinimum;
    double xMaximum;
    double yMinimum;
    double yMaximum;
    double step;
    String text;

    public ColorSquare(HSBColourHolder clr, String txt) {
        this.value = clr;
        this.xMinimum = 0;
        this.xMaximum = 100.0;
        this.yMinimum = 0;
        this.yMaximum = 100.0;
        this.step = 0.2;
        this.text = txt;

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
        double val = Math.floor((Math.round(s / step) * step) * 100) / 100;
        
        return MathHelper.clamp(val, yMinimum, yMaximum); // round to 2 decimals & clamp min and max
    }


    public String getText() { return text; }

    public double getStep() { return step; }

    public HSBColourHolder getValue() { return value; }

    public double getxMaximum() { return xMaximum; }

    public double getxMinimum() { return xMinimum; }

    public double getyMaximum() { return yMaximum; }

    public double getyMinimum() { return yMinimum; }

    public void setValue(double saturation, double brightness) {
        HSBColourHolder old = this.value;
        this.value.setS((float)saturation / 100);
        this.value.setB(1 - ((float)brightness / 100));
        ColorSquare.ColorPoof.ColorPoofInfo info = new ColorSquare.ColorPoof.ColorPoofInfo(old, this.value);
        callPoof(ColorSquare.ColorPoof.class, info);
        HSBColourHolder newValue = info.getNewValue();
        this.value = newValue;
    }

    public static abstract class ColorPoof<T extends Component, S extends ColorSquare.ColorPoof.ColorPoofInfo> extends Poof<T, S> {
        public static class ColorPoofInfo extends PoofInfo {
            HSBColourHolder oldValue;
            HSBColourHolder newValue;

            public ColorPoofInfo(HSBColourHolder oldValue, HSBColourHolder newValue) {
                ColorSquare.ColorPoof.ColorPoofInfo.this.oldValue = oldValue;
                ColorSquare.ColorPoof.ColorPoofInfo.this.newValue = newValue;
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
