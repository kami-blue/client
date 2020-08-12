// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.setting.impl.numerical;

import com.google.common.base.Converter;
import me.zeroeightsix.kami.setting.converter.AbstractBoxedNumberConverter;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import me.zeroeightsix.kami.setting.converter.BoxedDoubleConverter;

public class DoubleSetting extends NumberSetting<Double>
{
    private static final BoxedDoubleConverter converter;
    
    public DoubleSetting(final Double value, final Predicate<Double> restriction, final BiConsumer<Double, Double> consumer, final String name, final Predicate<Double> visibilityPredicate, final Double min, final Double max) {
        super(value, restriction, consumer, name, visibilityPredicate, min, max);
    }
    
    @Override
    public AbstractBoxedNumberConverter converter() {
        return DoubleSetting.converter;
    }
    
    static {
        converter = new BoxedDoubleConverter();
    }
}
