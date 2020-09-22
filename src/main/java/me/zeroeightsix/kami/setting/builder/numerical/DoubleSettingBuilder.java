package me.zeroeightsix.kami.setting.builder.numerical;

import me.zeroeightsix.kami.setting.impl.numerical.DoubleSetting;
import me.zeroeightsix.kami.setting.impl.numerical.NumberSetting;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 13/10/2018.
 */
public class DoubleSettingBuilder extends NumericalSettingBuilder<Double> {
    @Override
    public DoubleSetting build() {
        return new DoubleSetting(initialValue, predicate(), consumer(), name, visibilityPredicate(), min, max, step);
    }

    @Override
    public DoubleSettingBuilder withVisibility(Predicate<Double> predicate) {
        return (DoubleSettingBuilder) super.withVisibility(predicate);
    }

    @Override
    public DoubleSettingBuilder withRestriction(Predicate<Double> predicate) {
        return (DoubleSettingBuilder) super.withRestriction(predicate);
    }

    @Override
    public DoubleSettingBuilder withConsumer(BiConsumer<Double, Double> consumer) {
        return (DoubleSettingBuilder) super.withConsumer(consumer);
    }

    @Override
    public DoubleSettingBuilder withValue(Double value) {
        return (DoubleSettingBuilder) super.withValue(value);
    }

    @Override
    public DoubleSettingBuilder withRange(Double minimum, Double maximum) {
        return (DoubleSettingBuilder) super.withRange(minimum, maximum);
    }

    @Override
    public DoubleSettingBuilder withStep(Double step) {
        return (DoubleSettingBuilder) super.withStep(step);
    }

    @Override
    public DoubleSettingBuilder withMaximum(Double maximum) {
        return (DoubleSettingBuilder) super.withMaximum(maximum);
    }

    @Override
    public DoubleSettingBuilder withListener(BiConsumer<Double, Double> consumer) {
        return (DoubleSettingBuilder) super.withListener(consumer);
    }

    @Override
    public DoubleSettingBuilder withName(String name) {
        return (DoubleSettingBuilder) super.withName(name);
    }

    @Override
    public DoubleSettingBuilder withMinimum(Double minimum) {
        return (DoubleSettingBuilder) super.withMinimum(minimum);
    }

}
