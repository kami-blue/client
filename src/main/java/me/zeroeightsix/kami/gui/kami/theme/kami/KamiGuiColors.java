package me.zeroeightsix.kami.gui.kami.theme.kami;

import java.awt.*;

/**
 * @author l1ving
 * Class for all the main GUI colours used by the default kami theme
 * mfw I make it easier for skids to customize kami
 */
public class KamiGuiColors {
    public enum GuiC {
        bgColour(new Color(67, 54, 191)), // normal colored
        bgColourHover(new Color(67, 54, 191)), // light colored

        buttonPressed(new Color(116, 101, 247)),

        // N = normal T = toggled
        buttonIdleN(new Color(200, 200, 200)), // lighter grey
        buttonHoveredN(new Color(123, 114, 204)), // light grey

        buttonIdleT(new Color(165, 158, 232)), // lighter colored
        buttonHoveredT((new Color(buttonIdleT.color.getRGB())).brighter()),

        windowFilled(new Color(43, 43, 46, 230)),
        windowOutline(new Color(116, 101, 247)),

        pinnedWindow(new Color(116, 101, 247)),
        unpinnedWindow(new Color(168, 168, 168)),
        lineWindow(new Color(112, 112, 112)),

        sliderColour(new Color(155, 144, 255)),

        enumColour(new Color(116, 101, 247)),

        scrollBar(new Color(116, 101, 247));

        public Color color;

        GuiC(Color color) {
            this.color = color;
        }
    }
}