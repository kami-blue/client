package me.zeroeightsix.kami.setting.converter;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;

/**
 * Created by 086 on 14/10/2018.
 */
public class EnumConverter extends Converter<Enum, JsonElement> {

    Class<? extends Enum> clazz;
    Enum value;

    public EnumConverter(Class<? extends Enum> clazz, Enum value) {
        this.clazz = clazz;
        this.value = value;
    }

    @Override
    protected JsonElement doForward(Enum anEnum) {
        return new JsonPrimitive(anEnum.name());
    }

    @Override
    protected Enum doBackward(JsonElement jsonElement) { /* The try catch is really hacky workaround to get TextFormatting enums to work, see #595 */
        if (Arrays.toString(clazz.getEnumConstants()).contains(jsonElement.getAsString())) {
            try {
                return Enum.valueOf(clazz, jsonElement.getAsString());
            } catch (IllegalArgumentException e) {
                for (Enum enumConstant : clazz.getEnumConstants()) {
                    if (enumConstant.name().equalsIgnoreCase(jsonElement.getAsString())) return enumConstant;
                }
                return Enum.valueOf(clazz, "null");
            }
        }
        else {
            try {
                return Enum.valueOf(clazz, value.toString());
            } catch (IllegalArgumentException e) {
                for (Enum enumConstant : clazz.getEnumConstants()) {
                    if (enumConstant.name().equalsIgnoreCase(jsonElement.getAsString())) return enumConstant;
                }
                return Enum.valueOf(clazz, "null");
            }
        }
    }
}