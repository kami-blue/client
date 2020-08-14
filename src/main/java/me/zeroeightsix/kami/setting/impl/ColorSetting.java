package me.zeroeightsix.kami.setting.impl;

import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.converter.ColorConverter;
import me.zeroeightsix.kami.util.HSBColourHolder;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by Guac on 10/8/2020.
 */
public class ColorSetting extends Setting<HSBColourHolder> {

    private static final ColorConverter converter = new ColorConverter();

    public ColorSetting(HSBColourHolder value, Predicate<HSBColourHolder> restriction, BiConsumer<HSBColourHolder, HSBColourHolder> consumer, String name, Predicate<HSBColourHolder> visibilityPredicate) {
        super(value, restriction, consumer, name, visibilityPredicate);
    }

    @Override
    public ColorConverter converter() {
        return converter;
    }
}
