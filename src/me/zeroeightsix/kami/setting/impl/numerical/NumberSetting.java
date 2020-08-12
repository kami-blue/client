// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.setting.impl.numerical;

import com.google.common.base.Converter;
import me.zeroeightsix.kami.setting.converter.AbstractBoxedNumberConverter;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import me.zeroeightsix.kami.setting.Setting;

public abstract class NumberSetting<T extends Number> extends Setting<T>
{
    private final T min;
    private final T max;
    
    public NumberSetting(final T value, final Predicate<T> restriction, final BiConsumer<T, T> consumer, final String name, final Predicate<T> visibilityPredicate, final T min, final T max) {
        super(value, restriction, consumer, name, visibilityPredicate);
        this.min = min;
        this.max = max;
    }
    
    public boolean isBound() {
        return this.min != null && this.max != null;
    }
    
    @Override
    public abstract AbstractBoxedNumberConverter converter();
    
    @Override
    public T getValue() {
        return super.getValue();
    }
    
    public T getMax() {
        return this.max;
    }
    
    public T getMin() {
        return this.min;
    }
}
