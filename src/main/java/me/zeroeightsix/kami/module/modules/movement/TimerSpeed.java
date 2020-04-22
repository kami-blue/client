package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module.Category;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

import static me.zeroeightsix.kami.util.MathsUtils.round;

/**
 * @author TBM
 * Updated by dominikaaaa on 28/01/20
 */
@Module.Info(
        name = "TimerSpeed",
        description = "Automatically change your timer to go fast",
        category = Category.MOVEMENT
)
public class TimerSpeed extends Module {
    private float tickDelay = 0.0f;
    private static float curSpeed = 0.0f;
    private Setting<Float> minimumSpeed = register(Settings.floatBuilder("Minimum Speed").withMinimum(0.1F).withMaximum(10.0F).withValue(4.0F).build());
    private Setting<Float> maxSpeed = register(Settings.floatBuilder("Max Speed").withMinimum(0.1F).withMaximum(10.0F).withValue(7.0F).build());
    private Setting<Float> attemptSpeed = register(Settings.floatBuilder("Attempt Speed").withMinimum(1.0F).withMaximum(10.0F).withValue(4.2F).build());
    private Setting<Float> fastSpeed = register(Settings.floatBuilder("Fast Speed").withMinimum(1.0F).withMaximum(10.0F).withValue(5.0F).build());

    public static String returnGui() {
        return "" + round(curSpeed, 2);
    }

    public void onUpdate() {
        if (tickDelay == minimumSpeed.getValue()) {
            curSpeed = fastSpeed.getValue();
            mc.timer.tickLength = 50.0F / fastSpeed.getValue();
        }

        if (tickDelay >= maxSpeed.getValue()) {
            tickDelay = 0;
            curSpeed = attemptSpeed.getValue();
            mc.timer.tickLength = 50.0F / attemptSpeed.getValue();
        }
        ++tickDelay;
    }
    public void onDisable() {
        mc.timer.tickLength = 50.0F;
    }

}
