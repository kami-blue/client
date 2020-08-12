// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami.component;

import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.poof.IPoof;
import me.zeroeightsix.kami.gui.rgui.component.use.Slider;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.component.AbstractComponent;

public class UnboundSlider extends AbstractComponent
{
    double value;
    String text;
    public int sensitivity;
    int originX;
    double originValue;
    boolean integer;
    double max;
    double min;
    
    public UnboundSlider(final double value, final String text, final boolean integer) {
        this.sensitivity = 5;
        this.max = Double.MAX_VALUE;
        this.min = Double.MIN_VALUE;
        this.value = value;
        this.text = text;
        this.integer = integer;
        this.addMouseListener(new MouseListener() {
            @Override
            public void onMouseDown(final MouseButtonEvent event) {
                UnboundSlider.this.originX = event.getX();
                UnboundSlider.this.originValue = UnboundSlider.this.getValue();
            }
            
            @Override
            public void onMouseRelease(final MouseButtonEvent event) {
                UnboundSlider.this.originValue = UnboundSlider.this.getValue();
                UnboundSlider.this.originX = event.getX();
            }
            
            @Override
            public void onMouseDrag(final MouseButtonEvent event) {
                final int diff = (UnboundSlider.this.originX - event.getX()) / UnboundSlider.this.sensitivity;
                UnboundSlider.this.setValue(Math.floor((UnboundSlider.this.originValue - diff * ((UnboundSlider.this.originValue == 0.0) ? 1.0 : (Math.abs(UnboundSlider.this.originValue) / 10.0))) * 10.0) / 10.0);
            }
            
            @Override
            public void onMouseMove(final MouseMoveEvent event) {
            }
            
            @Override
            public void onScroll(final MouseScrollEvent event) {
                UnboundSlider.this.setValue((double)Math.round(UnboundSlider.this.getValue() + (event.isUp() ? 1 : -1)));
                UnboundSlider.this.originValue = UnboundSlider.this.getValue();
            }
        });
    }
    
    public void setMax(final double max) {
        this.max = max;
    }
    
    public void setMin(final double min) {
        this.min = min;
    }
    
    public void setValue(double value) {
        if (this.min != Double.MIN_VALUE) {
            value = Math.max(value, this.min);
        }
        if (this.max != Double.MAX_VALUE) {
            value = Math.min(value, this.max);
        }
        final Slider.SliderPoof.SliderPoofInfo info = new Slider.SliderPoof.SliderPoofInfo(this.value, value);
        this.callPoof(Slider.SliderPoof.class, info);
        this.value = (this.integer ? Math.floor(info.getNewValue()) : info.getNewValue());
    }
    
    public double getValue() {
        return this.value;
    }
    
    public String getText() {
        return this.text;
    }
}
