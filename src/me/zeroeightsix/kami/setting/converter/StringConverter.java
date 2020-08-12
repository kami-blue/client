// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.setting.converter;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonElement;
import com.google.common.base.Converter;

public class StringConverter extends Converter<String, JsonElement>
{
    protected JsonElement doForward(final String s) {
        return (JsonElement)new JsonPrimitive(s);
    }
    
    protected String doBackward(final JsonElement s) {
        return s.getAsString();
    }
}
