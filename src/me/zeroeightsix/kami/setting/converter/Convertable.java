// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.setting.converter;

import com.google.gson.JsonElement;
import com.google.common.base.Converter;

public interface Convertable<T>
{
    Converter<T, JsonElement> converter();
}
