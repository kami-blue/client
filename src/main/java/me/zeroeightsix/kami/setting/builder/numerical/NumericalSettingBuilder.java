package me.zeroeightsix.kami.setting.builder.numerical;

import me.zeroeightsix.kami.setting.builder.SettingBuilder;
import me.zeroeightsix.kami.setting.impl.numerical.NumberSetting;

import java.util.function.BiConsumer;

/**
 * Created by 086 on 13/10/2018.
 */
public abstract class NumericalSettingBuilder<T extends Number> extends SettingBuilder<T> {

    protected T min;
    protected T max;

    public NumericalSettingBuilder<T> withMinimum(T minimum) {
        predicateList.add((t -> t.doubleValue() >= minimum.doubleValue()));
        if (min == null || minimum.doubleValue() > min.doubleValue())
            min = minimum;
        return this;
    }

    public NumericalSettingBuilder<T> withMaximum(T maximum) {
        predicateList.add((t -> t.doubleValue() <= maximum.doubleValue()));
        if (max == null || maximum.doubleValue() < max.doubleValue())
            max = maximum;
        return this;
    }

    public NumericalSettingBuilder<T> withRange(T minimum, T maximum) {
        predicateList.add((t -> {
            double doubleValue = t.doubleValue();
            return doubleValue >= minimum.doubleValue() && doubleValue <= maximum.doubleValue();
        }));
        if (min == null || minimum.doubleValue() > min.doubleValue())
            min = minimum;
        if (max == null || maximum.doubleValue() < max.doubleValue())
            max = maximum;
        return this;
    }

    public NumericalSettingBuilder<T> withListener(BiConsumer<T, T> consumer) {
        this.consumer = consumer;
        return this;
    }

    @Override
    public NumericalSettingBuilder<T> withValue(T value) {
        return (NumericalSettingBuilder<T>) super.withValue(value);
    }

    @Override
    public NumericalSettingBuilder<T> withName(String name) {
        return (NumericalSettingBuilder<T>) super.withName(name);
    }

    public abstract NumberSetting<T> build();

}
