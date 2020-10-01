package me.zeroeightsix.kami.setting.builder.numerical;

import me.zeroeightsix.kami.setting.impl.numerical.FloatSetting;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 13/10/2018.
 */
public class FloatSettingBuilder extends NumericalSettingBuilder<Float> {
    @Override
    public FloatSetting build() {
        return new FloatSetting(initialValue, predicate(), consumer(), name, visibilityPredicate(), min, max, step);
    }

    @Override
    public FloatSettingBuilder withMinimum(Float minimum) {
        return (FloatSettingBuilder) super.withMinimum(minimum);
    }

    @Override
    public FloatSettingBuilder withName(String name) {
        return (FloatSettingBuilder) super.withName(name);
    }

    @Override
    public FloatSettingBuilder withListener(BiConsumer<Float, Float> consumer) {
        return (FloatSettingBuilder) super.withListener(consumer);
    }

    @Override
    public FloatSettingBuilder withMaximum(Float maximum) {
        return (FloatSettingBuilder) super.withMaximum(maximum);
    }

    @Override
    public FloatSettingBuilder withRange(Float minimum, Float maximum) {
        return (FloatSettingBuilder) super.withRange(minimum, maximum);
    }

    @Override
    public FloatSettingBuilder withStep(Float step) {
        return (FloatSettingBuilder) super.withStep(step);
    }

    @Override
    public FloatSettingBuilder withConsumer(BiConsumer<Float, Float> consumer) {
        return (FloatSettingBuilder) super.withConsumer(consumer);
    }

    @Override
    public FloatSettingBuilder withValue(Float value) {
        return (FloatSettingBuilder) super.withValue(value);
    }

    @Override
    public FloatSettingBuilder withVisibility(Predicate<Float> predicate) {
        return (FloatSettingBuilder) super.withVisibility(predicate);
    }

    @Override
    public FloatSettingBuilder withRestriction(Predicate<Float> predicate) {
        return (FloatSettingBuilder) super.withRestriction(predicate);
    }

}
