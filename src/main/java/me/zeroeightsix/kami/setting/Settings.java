package me.zeroeightsix.kami.setting;

import com.google.common.base.Converter;
import me.zeroeightsix.kami.setting.builder.SettingBuilder;
import me.zeroeightsix.kami.setting.builder.numerical.DoubleSettingBuilder;
import me.zeroeightsix.kami.setting.builder.numerical.FloatSettingBuilder;
import me.zeroeightsix.kami.setting.builder.numerical.IntegerSettingBuilder;
import me.zeroeightsix.kami.setting.builder.numerical.NumericalSettingBuilder;
import me.zeroeightsix.kami.setting.builder.primitive.BooleanSettingBuilder;
import me.zeroeightsix.kami.setting.builder.primitive.EnumSettingBuilder;
import me.zeroeightsix.kami.setting.builder.primitive.StringSettingBuilder;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 13/10/2018.
 */
public class Settings {

    public static FloatSettingBuilder floatBuilder() {
        return new FloatSettingBuilder();
    }

    public static DoubleSettingBuilder doubleBuilder() {
        return new DoubleSettingBuilder();
    }

    public static IntegerSettingBuilder integerBuilder() {
        return new IntegerSettingBuilder();
    }

    public static BooleanSettingBuilder booleanBuilder() {
        return new BooleanSettingBuilder();
    }

    public static StringSettingBuilder stringBuilder() {
        return new StringSettingBuilder();
    }

    public static EnumSettingBuilder enumBuilder(Class<? extends Enum> clazz) {
        return new EnumSettingBuilder(clazz);
    }

    public static <T extends Enum> EnumSettingBuilder<T> enumBuilder(Class<T> clazz, String name) {
        return new EnumSettingBuilder(clazz).withName(name);
    }

    public static Setting<Float> f(String name, float value) {
        return floatBuilder(name).withValue(value).build();
    }

    public static Setting<Double> d(String name, double value) {
        return doubleBuilder(name).withValue(value).build();
    }

    public static Setting<Integer> i(String name, int value) {
        return integerBuilder(name).withValue(value).build();
    }

    public static Setting<Boolean> b(String name, boolean value) {
        return booleanBuilder(name).withValue(value).build();
    }

    public static Setting<Boolean> b(String name) {
        return booleanBuilder(name).withValue(true).build();
    }

    public static Setting<String> s(String name, String value) {
        return stringBuilder(name).withValue(value).build();
    }

    public static <T extends Enum> Setting<T> e(String name, Enum value) {
        return enumBuilder(value.getClass()).withName(name).withValue(value).build();
    }

    public static NumericalSettingBuilder<Float> floatBuilder(String name) {
        return new FloatSettingBuilder().withName(name);
    }

    public static NumericalSettingBuilder<Double> doubleBuilder(String name) {
        return new DoubleSettingBuilder().withName(name);
    }

    public static NumericalSettingBuilder<Integer> integerBuilder(String name) {
        return new IntegerSettingBuilder().withName(name);
    }

    public static BooleanSettingBuilder booleanBuilder(String name) {
        return new BooleanSettingBuilder().withName(name);
    }

    public static StringSettingBuilder stringBuilder(String name) {
        return (StringSettingBuilder) new StringSettingBuilder().withName(name);
    }

    public static <T> SettingBuilder<T> custom(String name, T initialValue, Converter converter, Predicate<T> restriction, BiConsumer<T, T> consumer, Predicate<T> visibilityPredicate) {
        return new SettingBuilder<T>() {
            @Override
            public Setting<T> build() {
                return new Setting<T>(initialValue, predicate(), consumer, name, visibilityPredicate()) {
                    @Override
                    public Converter converter() {
                        return converter;
                    }
                };
            }
        }.withName(name).withValue(initialValue).withConsumer(consumer).withVisibility(visibilityPredicate).withRestriction(restriction);
    }

    public static <T> SettingBuilder<T> custom(String name, T initialValue, Converter converter, Predicate<T> restriction, BiConsumer<T, T> consumer, boolean hidden) {
        return custom(name, initialValue, converter, restriction, consumer, t -> !hidden);
    }

    public static <T> SettingBuilder<T> custom(String name, T initialValue, Converter converter, Predicate<T> restriction, boolean hidden) {
        return custom(name, initialValue, converter, restriction, (t, t2) -> {
        }, hidden);
    }

    public static <T> SettingBuilder<T> custom(String name, T initialValue, Converter converter, boolean hidden) {
        return custom(name, initialValue, converter, input -> true, (t, t2) -> {
        }, hidden);
    }

    public static <T> SettingBuilder<T> custom(String name, T initialValue, Converter converter) {
        return custom(name, initialValue, converter, input -> true, (t, t2) -> {
        }, false);
    }

}
