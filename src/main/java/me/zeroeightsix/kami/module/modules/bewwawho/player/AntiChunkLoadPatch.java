package me.zeroeightsix.kami.module.modules.bewwawho.player;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

/***
 * Created by S-B99 on 11/01/20
 */
@Module.Info(name = "AntiChunkLoadPatch", category = Module.Category.EXPERIMENTAL, description = "Prevents loading of overloaded chunks", showOnArray = Module.ShowOnArray.OFF)
public class AntiChunkLoadPatch extends Module {
//    public Setting<Boolean> startupGlobal = register(Settings.b("Enable Automatically", true));
    private static AntiChunkLoadPatch INSTANCE = new AntiChunkLoadPatch();

    public AntiChunkLoadPatch() {
        INSTANCE = this;
    }

    public static boolean enabled() {
        return INSTANCE.isEnabled();
    }
}
