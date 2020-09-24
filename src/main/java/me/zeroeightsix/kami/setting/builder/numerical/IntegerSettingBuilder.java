package me.zeroeightsix.kami.setting.builder.numerical;

import me.zeroeightsix.kami.setting.impl.numerical.IntegerSetting;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 13/10/2018.
 */
public class IntegerSettingBuilder extends NumericalSettingBuilder<Integer> {
    @Override
    public IntegerSetting build() {
        return new IntegerSetting(initialValue, predicate(), consumer(), name, visibilityPredicate(), min, max, step);
    }

    @Override
    public IntegerSettingBuilder withMinimum(Integer minimum) {
        return (IntegerSettingBuilder) super.withMinimum(minimum);
    }

    @Override
    public IntegerSettingBuilder withName(String name) {
        return (IntegerSettingBuilder) super.withName(name);
    }

    @Override
    public IntegerSettingBuilder withListener(BiConsumer<Integer, Integer> consumer) {
        return (IntegerSettingBuilder) super.withListener(consumer);
    }

    @Override
    public IntegerSettingBuilder withMaximum(Integer maximum) {
        return (IntegerSettingBuilder) super.withMaximum(maximum);
    }

    @Override
    public IntegerSettingBuilder withRange(Integer minimum, Integer maximum) {
        return (IntegerSettingBuilder) super.withRange(minimum, maximum);
    }

    @Override
    public IntegerSettingBuilder withStep(Integer step) {
        return (IntegerSettingBuilder) super.withStep(step);
    }

    @Override
    public IntegerSettingBuilder withValue(Integer value) {
        return (IntegerSettingBuilder) super.withValue(value);
    }

    @Override
    public IntegerSettingBuilder withConsumer(BiConsumer<Integer, Integer> consumer) {
        return (IntegerSettingBuilder) super.withConsumer(consumer);
    }

    @Override
    public IntegerSettingBuilder withRestriction(Predicate<Integer> predicate) {
        return (IntegerSettingBuilder) super.withRestriction(predicate);
    }

    @Override
    public IntegerSettingBuilder withVisibility(Predicate<Integer> predicate) {
        return (IntegerSettingBuilder) super.withVisibility(predicate);
    }
}
