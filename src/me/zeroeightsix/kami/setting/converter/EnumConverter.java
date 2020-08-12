// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.setting.converter;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonElement;
import com.google.common.base.Converter;

public class EnumConverter extends Converter<Enum, JsonElement>
{
    Class<? extends Enum> clazz;
    
    public EnumConverter(final Class<? extends Enum> clazz) {
        this.clazz = clazz;
    }
    
    protected JsonElement doForward(final Enum anEnum) {
        return (JsonElement)new JsonPrimitive(anEnum.toString());
    }
    
    protected Enum doBackward(final JsonElement jsonElement) {
        return (Enum)Enum.valueOf(this.clazz, jsonElement.getAsString());
    }
}
