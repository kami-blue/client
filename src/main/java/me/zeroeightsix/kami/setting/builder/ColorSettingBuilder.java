package me.zeroeightsix.kami.setting.builder;

import me.zeroeightsix.kami.setting.impl.ColorSetting;
import me.zeroeightsix.kami.util.HSBColourHolder;

/**
 * Created by Guac on 10/8/2020.
 */
public class ColorSettingBuilder extends SettingBuilder<HSBColourHolder> {
    @Override
    public ColorSetting build() {
        return new ColorSetting(initialValue, predicate(), consumer(), name, visibilityPredicate());
    }

    @Override
    public ColorSettingBuilder withName(String name) {
        return (ColorSettingBuilder) super.withName(name);
    }
}
