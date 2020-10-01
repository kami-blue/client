package me.zeroeightsix.kami.setting.impl.numerical;

import me.zeroeightsix.kami.setting.converter.AbstractBoxedNumberConverter;
import me.zeroeightsix.kami.setting.converter.BoxedIntegerConverter;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 12/10/2018.
 */
public class IntegerSetting extends NumberSetting<Integer> {

    private static final BoxedIntegerConverter converter = new BoxedIntegerConverter();

    public IntegerSetting(Integer value, Predicate<Integer> restriction, BiConsumer<Integer, Integer> consumer, String name, Predicate<Integer> visibilityPredicate, Integer min, Integer max, Integer step) {
        super(value, restriction, consumer, name, visibilityPredicate, min, max, step);
    }

    @Override
    public AbstractBoxedNumberConverter converter() {
        return converter;
    }

}
