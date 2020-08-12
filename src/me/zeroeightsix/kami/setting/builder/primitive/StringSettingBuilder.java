// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.setting.builder.primitive;

import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.impl.StringSetting;
import me.zeroeightsix.kami.setting.builder.SettingBuilder;

public class StringSettingBuilder extends SettingBuilder<String>
{
    @Override
    public StringSetting build() {
        return new StringSetting((String)this.initialValue, this.predicate(), this.consumer(), this.name, this.visibilityPredicate());
    }
}
