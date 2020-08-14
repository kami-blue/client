package me.zeroeightsix.kami.setting.converter;

import com.google.common.base.Converter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import me.zeroeightsix.kami.util.HSBColourHolder;

/**
 * Created by Guac on 10/8/2020.
 */
public class ColorConverter extends Converter<HSBColourHolder, JsonElement> {

    @Override
    protected JsonElement doForward(HSBColourHolder c) {
        JsonArray clr = new JsonArray();
        clr.add(c.getH());
        clr.add(c.getB());
        clr.add(c.getS());
        clr.add(c.getAlpha());
        return clr;
    }

    @Override
    protected HSBColourHolder doBackward(JsonElement c) {
        JsonArray color = c.getAsJsonArray();
        Float h = color.get(0).getAsFloat();
        Float s = color.get(1).getAsFloat();
        Float b = color.get(2).getAsFloat();
        Float a = color.get(3).getAsFloat();
        return new HSBColourHolder(h, s, b, a);
    }

}
