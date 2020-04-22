package me.zeroeightsix.kami.module.modules.player;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

/**
 * Code like this does not have an author, as it is literally one function. It's nothing unique.
 * See BowSpam for example. It's just one thing. Anybody can write it the exact same way on accident.
 * There is nothing to credit here.
 * This message is here because clowns decided to argue with me that they should be credited even though they did not come up with the code.
 * Updated by dominikaaaa on 01/03/20
 */
@Module.Info(
        name = "Timer",
        category = Module.Category.PLAYER,
        description = "Changes your client tick speed"
)
public class Timer extends Module {
    private Setting<Boolean> slow = register(Settings.b("Slow Mode", false));
    private Setting<Float> tickNormal = register(Settings.floatBuilder("Tick N").withMinimum(1f).withMaximum(10f).withValue(2.0f).withVisibility(v -> !slow.getValue()).build());
    private Setting<Float> tickSlow = register(Settings.floatBuilder("Tick S").withMinimum(1f).withMaximum(10f).withValue(8f).withVisibility(v -> slow.getValue()).build());

    public void onDisable() {
        mc.timer.tickLength = 50.0f;
    }

    public void onUpdate() {
        if (!slow.getValue()) {
            mc.timer.tickLength = 50.0f / tickNormal.getValue();
        }
        else {
            mc.timer.tickLength = 50.0f / (tickSlow.getValue() / 10.0f);
        }
    }
}
