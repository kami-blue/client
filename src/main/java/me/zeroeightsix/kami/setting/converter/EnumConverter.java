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

    public EnumConverter(Class<? extends Enum> clazz) {
        this.clazz = clazz;
    }

    @Override
    protected JsonElement doForward(Enum anEnum) {
        return new JsonPrimitive(anEnum.toString());
    }

    @Override
    protected Enum doBackward(JsonElement jsonElement) {
        Enum[] enums = clazz.getEnumConstants();
        if (Arrays.toString(clazz.getEnumConstants()).contains(jsonElement.getAsString()))
        {
            return Enum.valueOf(clazz, jsonElement.getAsString());
        }
        else
        {
            return Enum.valueOf(clazz,enums[0].toString());
        }
    }
}
