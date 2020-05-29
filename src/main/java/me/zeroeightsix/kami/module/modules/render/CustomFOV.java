package me.zeroeightsix.kami.module.modules.render;
// LittleDraily
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

@Module.Info(
        name = "CustomFOV",
        category = Module.Category.RENDER,
        description = "Get a custom FOV"
)
public class CustomFOV extends Module {
    private final Setting<Float> fov = register(Settings.floatBuilder("Value").withMinimum(30f).withValue(110f).withMaximum(360f).withRange(30f,360f));
    @Override
    public void onUpdate() {
        mc.gameSettings.fovSetting = fov.getValue();
    }
}
