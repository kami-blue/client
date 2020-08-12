// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.setting.converter;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonElement;
import com.google.common.base.Converter;

public class BooleanConverter extends Converter<Boolean, JsonElement>
{
    protected JsonElement doForward(final Boolean aBoolean) {
        return (JsonElement)new JsonPrimitive(aBoolean);
    }
    
    protected Boolean doBackward(final JsonElement s) {
        return s.getAsBoolean();
    }
}
