package me.zeroeightsix.kami.gui.rgui.component.use;

import me.zeroeightsix.kami.gui.rgui.component.AbstractComponent;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.poof.use.Poof;
import net.minecraft.util.math.MathHelper;

/**
 * Created by 086 on 8/08/2017.
 */
public class Slider extends AbstractComponent {

    double value;
    double minimum;
    double maximum;
    double step;
    String text;
    boolean integer;

    public Slider(double value, double minimum, double maximum, double step, String text, boolean integer) {
        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.step = step;
        this.text = text;
        this.integer = integer;

        addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(MouseButtonEvent event) {
                setValue(calculateValue(event.getX()));
            }

            @Override
            public void onMouseRelease(MouseButtonEvent event) {

            }

            @Override
            public void onMouseDrag(MouseButtonEvent event) {
                setValue(calculateValue(event.getX()));
            }

            @Override
            public void onMouseMove(MouseMoveEvent event) {

            }

            @Override
            public void onScroll(MouseScrollEvent event) {

            }
        });
    }

    public Slider(double value, double minimum, double maximum, String text) {
        this(value, minimum, maximum, getDefaultStep(minimum, maximum), text, false);
    }

    private double calculateValue(double x){
        double d1 = x / getWidth();
        double d2 = (maximum - minimum);
        double s = d1 * d2 + minimum;

        return MathHelper.clamp(Math.floor((Math.round(s/step)*step)*100)/100, minimum, maximum); // round to 2 decimals & clamp min and max
    }

    public static double getDefaultStep(double min, double max){
        double s = gcd(min, max);
        if (s == max){
            s = max / 20;
        }
        if (max > 10){
            s = Math.round(s);
        }
        if (s == 0) s = max;
        return s;
    }

    public String getText() {
        return text;
    }

    public double getStep() {
        return step;
    }

    public double getValue() {
        return value;
    }

    public double getMaximum() {
        return maximum;
    }

    public double getMinimum() {
        return minimum;
    }

    public void setValue(double value) {
        SliderPoof.SliderPoofInfo info = new SliderPoof.SliderPoofInfo(this.value, value);
        callPoof(SliderPoof.class, info);
        double newValue = info.getNewValue();
        this.value = integer ? (int) newValue : newValue;
    }

    public static abstract class SliderPoof<T extends Component, S extends SliderPoof.SliderPoofInfo> extends Poof<T,S> {
        public static class SliderPoofInfo extends PoofInfo {
            double oldValue;
            double newValue;

            public SliderPoofInfo(double oldValue, double newValue) {
                SliderPoofInfo.this.oldValue = oldValue;
                SliderPoofInfo.this.newValue = newValue;
            }

            public double getOldValue() {
                return oldValue;
            }

            public double getNewValue() {
                return newValue;
            }

            public void setNewValue(double newValue) {
                this.newValue = newValue;
            }
        }
    }

    public static double gcd(double a, double b)
    {
        a = Math.floor(a);
        b = Math.floor(b);
        if(a == 0 || b == 0) return a+b; // base case
        return gcd(b,a%b);
    }

}
