package me.zeroeightsix.kami.setting.impl;

import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.converter.StringConverter;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 12/10/2018.
 */
public class StringSetting extends Setting<String> {

    private static final StringConverter converter = new StringConverter();

    public StringSetting(String value, Predicate<String> restriction, BiConsumer<String, String> consumer, String name, Predicate<String> visibilityPredicate) {
        super(value, restriction, consumer, name, visibilityPredicate);
    }

    @Override
    public StringConverter converter() {
        return converter;
    }

    @Override
    public String getValueAsString() {
        return getValue();
    }

    @Override
    public void setValueFromString(String s, boolean isBoolean) {
        setValue(s);
    }
}
