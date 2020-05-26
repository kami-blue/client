package me.zeroeightsix.kami.module.modules.client;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import java.awt.*;


import static me.zeroeightsix.kami.util.ColourConverter.toF;

/**
 * @author Salt 5/26/2020
 * RGB Generation by dominikaaaa
 */
@Module.Info(
        name = "RGB Gui",
        description = "Makes your GUI RGB",
        category = Module.Category.CLIENT
)
public class RGB_Gui extends Module
{
    private static boolean rgbEnabled;
    private Setting<Integer> rainbowSpeed = register(Settings.integerBuilder().withName("Rainbow Speed").withValue(30).withMinimum(1).withMaximum(100).build());
    private static int[] rgbList = new int[3];

    public void onEnable()
    {
        rgbEnabled = true;
    }
    public void onDisable()
    {
        rgbEnabled = false;
    }

    public void onUpdate()
    {
        int newSpeed = rainbowSpeed.getValue();
        float[] hue = {System.currentTimeMillis() % (360 * newSpeed) / (360f * newSpeed)};
        int rgb = Color.HSBtoRGB(hue[0], toF(255), toF(255));
        rgbList[0] = (rgb >> 16) & 0xFF;
        rgbList[1] = (rgb >> 8) & 0xFF;
        rgbList[2] = rgb & 0xFF;
    }
    public static boolean guiRgbEnabled() {return rgbEnabled;}

    public static int[] getRgbList()
    {
        return rgbList;
    }

}
