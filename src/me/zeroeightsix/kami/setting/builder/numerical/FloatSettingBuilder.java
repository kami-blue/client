// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.setting.builder.numerical;

import me.zeroeightsix.kami.setting.builder.SettingBuilder;
import me.zeroeightsix.kami.setting.Setting;
import java.util.function.Predicate;
import java.util.function.BiConsumer;
import me.zeroeightsix.kami.setting.impl.numerical.FloatSetting;
import me.zeroeightsix.kami.setting.impl.numerical.NumberSetting;

public class FloatSettingBuilder extends NumericalSettingBuilder<Float>
{
    @Override
    public NumberSetting build() {
        return new FloatSetting((Float)this.initialValue, this.predicate(), this.consumer(), this.name, this.visibilityPredicate(), (Float)this.min, (Float)this.max);
    }
    
    @Override
    public FloatSettingBuilder withMinimum(final Float minimum) {
        return (FloatSettingBuilder)super.withMinimum(minimum);
    }
    
    @Override
    public FloatSettingBuilder withName(final String name) {
        return (FloatSettingBuilder)super.withName(name);
    }
    
    @Override
    public FloatSettingBuilder withListener(final BiConsumer<Float, Float> consumer) {
        return (FloatSettingBuilder)super.withListener(consumer);
    }
    
    @Override
    public FloatSettingBuilder withMaximum(final Float maximum) {
        return (FloatSettingBuilder)super.withMaximum(maximum);
    }
    
    @Override
    public FloatSettingBuilder withRange(final Float minimum, final Float maximum) {
        return (FloatSettingBuilder)super.withRange(minimum, maximum);
    }
    
    @Override
    public FloatSettingBuilder withConsumer(final BiConsumer<Float, Float> consumer) {
        return (FloatSettingBuilder)super.withConsumer(consumer);
    }
    
    @Override
    public FloatSettingBuilder withValue(final Float value) {
        return (FloatSettingBuilder)super.withValue(value);
    }
    
    @Override
    public FloatSettingBuilder withVisibility(final Predicate<Float> predicate) {
        return (FloatSettingBuilder)super.withVisibility(predicate);
    }
    
    @Override
    public FloatSettingBuilder withRestriction(final Predicate<Float> predicate) {
        return (FloatSettingBuilder)super.withRestriction(predicate);
    }
}
