// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.setting.builder.numerical;

import me.zeroeightsix.kami.setting.builder.SettingBuilder;
import me.zeroeightsix.kami.setting.Setting;
import java.util.function.Predicate;
import java.util.function.BiConsumer;
import me.zeroeightsix.kami.setting.impl.numerical.IntegerSetting;
import me.zeroeightsix.kami.setting.impl.numerical.NumberSetting;

public class IntegerSettingBuilder extends NumericalSettingBuilder<Integer>
{
    @Override
    public NumberSetting build() {
        return new IntegerSetting((Integer)this.initialValue, this.predicate(), this.consumer(), this.name, this.visibilityPredicate(), (Integer)this.min, (Integer)this.max);
    }
    
    @Override
    public IntegerSettingBuilder withMinimum(final Integer minimum) {
        return (IntegerSettingBuilder)super.withMinimum(minimum);
    }
    
    @Override
    public NumericalSettingBuilder withName(final String name) {
        return super.withName(name);
    }
    
    @Override
    public IntegerSettingBuilder withListener(final BiConsumer<Integer, Integer> consumer) {
        return (IntegerSettingBuilder)super.withListener(consumer);
    }
    
    @Override
    public IntegerSettingBuilder withMaximum(final Integer maximum) {
        return (IntegerSettingBuilder)super.withMaximum(maximum);
    }
    
    @Override
    public IntegerSettingBuilder withRange(final Integer minimum, final Integer maximum) {
        return (IntegerSettingBuilder)super.withRange(minimum, maximum);
    }
    
    @Override
    public IntegerSettingBuilder withValue(final Integer value) {
        return (IntegerSettingBuilder)super.withValue(value);
    }
    
    @Override
    public IntegerSettingBuilder withConsumer(final BiConsumer<Integer, Integer> consumer) {
        return (IntegerSettingBuilder)super.withConsumer(consumer);
    }
    
    @Override
    public IntegerSettingBuilder withRestriction(final Predicate<Integer> predicate) {
        return (IntegerSettingBuilder)super.withRestriction(predicate);
    }
    
    @Override
    public IntegerSettingBuilder withVisibility(final Predicate<Integer> predicate) {
        return (IntegerSettingBuilder)super.withVisibility(predicate);
    }
}
