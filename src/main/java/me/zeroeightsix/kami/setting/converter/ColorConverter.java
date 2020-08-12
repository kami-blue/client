package me.zeroeightsix.kami.setting.converter;

import com.google.common.base.Converter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.awt.Color;

/**
 * Created by Guac on 10/8/2020.
 */
public class ColorConverter extends Converter<Color, JsonElement> {

    @Override
    protected JsonElement doForward(Color rgbaColor) {
        JsonArray clr = new JsonArray();
        clr.add(rgbaColor.getRed());
        clr.add(rgbaColor.getGreen());
        clr.add(rgbaColor.getBlue());
        clr.add(rgbaColor.getAlpha());
        return clr;
    }

    @Override
    protected Color doBackward(JsonElement s) {
        JsonArray color = s.getAsJsonArray();
        Integer r = color.get(0).getAsInt();
        Integer g = color.get(1).getAsInt();
        Integer b = color.get(2).getAsInt();
        Integer a = color.get(3).getAsInt();
        return new Color(r, g, b, a);
    }

}
