package me.zeroeightsix.kami.util;

import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 25/03/20
 */
public class ColourTextFormatting {
    public enum ColourEnum {
        BLACK(new Color(0,0, 0)),
        DARK_BLUE(new Color(0, 0, 170)),
        DARK_GREEN(new Color(0, 170, 0)),
        DARK_AQUA(new Color(0, 170, 170)),
        DARK_RED(new Color(170, 0, 0)),
        DARK_PURPLE(new Color(170, 0, 170)),
        GOLD(new Color(255, 170, 0)),
        GRAY(new Color(170, 170, 170)),
        DARK_GRAY(new Color(85, 85, 85)),
        BLUE(new Color(85, 85, 255)),
        GREEN(new Color(85, 255, 85)),
        AQUA(new Color(85, 225, 225)),
        RED(new Color(255, 85, 85)),
        LIGHT_PURPLE(new Color(255, 85, 255)),
        YELLOW(new Color(255, 255, 85)),
        WHITE(new Color(255, 255, 255));

        public Color colorLocal;

        ColourEnum(Color colorLocal) { this.colorLocal = colorLocal; }
    }

    public static Map<TextFormatting, ColourEnum> colourEnumMap = new HashMap<TextFormatting, ColourEnum>(){{
        put(TextFormatting.BLACK, ColourEnum.BLACK);
        put(TextFormatting.DARK_BLUE, ColourEnum.DARK_BLUE);
        put(TextFormatting.DARK_GREEN, ColourEnum.DARK_GREEN);
        put(TextFormatting.DARK_AQUA, ColourEnum.DARK_AQUA);
        put(TextFormatting.DARK_RED, ColourEnum.DARK_RED);
        put(TextFormatting.DARK_PURPLE, ColourEnum.DARK_PURPLE);
        put(TextFormatting.GOLD, ColourEnum.GOLD);
        put(TextFormatting.GRAY, ColourEnum.GRAY);
        put(TextFormatting.DARK_GRAY, ColourEnum.DARK_GRAY);
        put(TextFormatting.BLUE, ColourEnum.BLUE);
        put(TextFormatting.GREEN, ColourEnum.GREEN);
        put(TextFormatting.AQUA, ColourEnum.AQUA);
        put(TextFormatting.RED, ColourEnum.RED);
        put(TextFormatting.LIGHT_PURPLE, ColourEnum.LIGHT_PURPLE);
        put(TextFormatting.YELLOW, ColourEnum.YELLOW);
        put(TextFormatting.WHITE, ColourEnum.WHITE);
    }};

    public enum ColourCode {
        BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE
    }

    public static Map<ColourCode, TextFormatting> toTextMap = new HashMap<ColourCode, TextFormatting>(){{
        put(ColourCode.BLACK, TextFormatting.BLACK);
        put(ColourCode.DARK_BLUE, TextFormatting.DARK_BLUE);
        put(ColourCode.DARK_GREEN, TextFormatting.DARK_GREEN);
        put(ColourCode.DARK_AQUA, TextFormatting.DARK_AQUA);
        put(ColourCode.DARK_RED, TextFormatting.DARK_RED);
        put(ColourCode.DARK_PURPLE, TextFormatting.DARK_PURPLE);
        put(ColourCode.GOLD, TextFormatting.GOLD);
        put(ColourCode.GRAY, TextFormatting.GRAY);
        put(ColourCode.DARK_GRAY, TextFormatting.DARK_GRAY);
        put(ColourCode.BLUE, TextFormatting.BLUE);
        put(ColourCode.GREEN, TextFormatting.GREEN);
        put(ColourCode.AQUA, TextFormatting.AQUA);
        put(ColourCode.RED, TextFormatting.RED);
        put(ColourCode.LIGHT_PURPLE, TextFormatting.LIGHT_PURPLE);
        put(ColourCode.YELLOW, TextFormatting.YELLOW);
        put(ColourCode.WHITE, TextFormatting.WHITE);
    }};

}
