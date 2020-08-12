package me.zeroeightsix.kami.setting.impl;

import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.converter.ColorConverter;

import java.awt.Color;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by Guac on 10/8/2020.
 */
public class ColorSetting extends Setting<Color> {

    private static final ColorConverter converter = new ColorConverter();

    public ColorSetting(Color value, Predicate<Color> restriction, BiConsumer<Color, Color> consumer, String name, Predicate<Color> visibilityPredicate) {
        super(value, restriction, consumer, name, visibilityPredicate);
    }

    @Override
    public ColorConverter converter() {
        return converter;
    }
}
