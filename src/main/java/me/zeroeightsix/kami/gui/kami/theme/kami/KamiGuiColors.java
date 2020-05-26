package me.zeroeightsix.kami.gui.kami.theme.kami;

import java.awt.*;

/**
 * @author dominikaaaa
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
        buttonHoveredN(new Color(190, 190, 190)), // light grey

        buttonIdleT(new Color(165, 158, 232)), // lighter colored
        buttonHoveredT((new Color(buttonIdleT.color.getRGB())).brighter()),

        windowOutline(new Color(116, 101, 247)),
        windowOutlineWidth(1.8f),

        pinnedWindow(new Color(116, 101, 247)),
        unpinnedWindow(168.3),
        lineWindow(112.2),

        sliderColour(new Color(155, 144, 255)),

        enumColour(new Color(116, 101, 247)),

        chatOutline(new Color(52, 43, 128)),

        scrollBar(new Color(116, 101, 247));

        public Color color;
        public float aFloat;
        public double aDouble;

        GuiC(Color color) {
            this.color = color;
        }

        GuiC(float aFloat) {
            this.aFloat = aFloat;
        }

        GuiC(double aDouble) {
            this.aDouble = aDouble;
        }
    }
}