package me.zeroeightsix.kami.setting.impl.numerical;

import me.zeroeightsix.kami.setting.converter.AbstractBoxedNumberConverter;
import me.zeroeightsix.kami.setting.converter.BoxedFloatConverter;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 12/10/2018.
 */
public class FloatSetting extends NumberSetting<Float> {

    private static final BoxedFloatConverter converter = new BoxedFloatConverter();

    public FloatSetting(Float value, Predicate<Float> restriction, BiConsumer<Float, Float> consumer, String name, Predicate<Float> visibilityPredicate, Float min, Float max, Float step) {
        super(value, restriction, consumer, name, visibilityPredicate, min, max, step);
    }

    @Override
    public AbstractBoxedNumberConverter converter() {
        return converter;
    }

}
