// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.setting.converter;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonElement;
import com.google.common.base.Converter;

public abstract class AbstractBoxedNumberConverter<T extends Number> extends Converter<T, JsonElement>
{
    protected JsonElement doForward(final T t) {
        return (JsonElement)new JsonPrimitive((Number)t);
    }
}
