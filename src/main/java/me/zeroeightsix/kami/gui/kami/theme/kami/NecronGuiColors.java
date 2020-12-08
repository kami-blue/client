package me.zeroeightsix.kami.gui.kami.theme.kami;

import java.awt.*;

/**
 * @author l1ving
 * modified by TopiasL for NECRON
 * Class for all the main GUI colours used by the default necron theme
 * mfw I make it easier for skids to customize kami
 */
public class NecronGuiColors {
    public enum GuiC {
        bgColour(new Color(0, 180, 255)), // normal colored
        bgColourHover(new Color(0, 180, 255)), // light colored

        buttonPressed(new Color(0, 220, 80)),

        // N = normal T = toggled
        buttonIdleN(new Color(200, 200, 200)), // lighter grey
        buttonHoveredN(new Color(0, 160, 58)), // light grey

        buttonIdleT(new Color(0, 200, 75)), // lighter colored
        buttonHoveredT(new Color(196, 196, 196)),

        windowFilled(new Color(43, 43, 46, 230)),
        windowOutline(new Color(217, 14, 58)),

        pinnedWindow(new Color(151, 63, 79)),
        unpinnedWindow(new Color(168, 168, 168)),
        lineWindow(new Color(112, 112, 112)),

        sliderColour(new Color(213, 36, 17)),

        enumColour(new Color(139, 0, 199)),

        scrollBar(new Color(213, 36, 17));

        public Color color;

        GuiC(Color color) {
            this.color = color;
        }
    }
}