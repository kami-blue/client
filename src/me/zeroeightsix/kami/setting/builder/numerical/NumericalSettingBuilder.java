// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.setting.builder.numerical;

import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.impl.numerical.NumberSetting;
import java.util.function.BiConsumer;
import me.zeroeightsix.kami.setting.builder.SettingBuilder;

public abstract class NumericalSettingBuilder<T extends Number> extends SettingBuilder<T>
{
    protected T min;
    protected T max;
    
    public NumericalSettingBuilder<T> withMinimum(final T minimum) {
        this.predicateList.add(t -> t.doubleValue() >= minimum.doubleValue());
        if (this.min == null || minimum.doubleValue() > this.min.doubleValue()) {
            this.min = minimum;
        }
        return this;
    }
    
    public NumericalSettingBuilder<T> withMaximum(final T maximum) {
        this.predicateList.add(t -> t.doubleValue() <= maximum.doubleValue());
        if (this.max == null || maximum.doubleValue() < this.max.doubleValue()) {
            this.max = maximum;
        }
        return this;
    }
    
    public NumericalSettingBuilder<T> withRange(final T minimum, final T maximum) {
        final double doubleValue;
        this.predicateList.add(t -> {
            doubleValue = t.doubleValue();
            return doubleValue >= minimum.doubleValue() && doubleValue <= maximum.doubleValue();
        });
        if (this.min == null || minimum.doubleValue() > this.min.doubleValue()) {
            this.min = minimum;
        }
        if (this.max == null || maximum.doubleValue() < this.max.doubleValue()) {
            this.max = maximum;
        }
        return this;
    }
    
    public NumericalSettingBuilder<T> withListener(final BiConsumer<T, T> consumer) {
        this.consumer = consumer;
        return this;
    }
    
    @Override
    public NumericalSettingBuilder<T> withValue(final T value) {
        return (NumericalSettingBuilder)super.withValue(value);
    }
    
    @Override
    public NumericalSettingBuilder withName(final String name) {
        return (NumericalSettingBuilder)super.withName(name);
    }
    
    @Override
    public abstract NumberSetting build();
}
