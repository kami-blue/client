package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

/**
 * Created by 086 on 24/12/2017.
 */
@Module.Info(
        name = "AutoJump",
        category = Module.Category.MOVEMENT,
        description = "Automatically jumps if possible"
)
public class AutoJump extends Module {
    private Setting<Integer> delay = register(Settings.integerBuilder("Tick Delay").withValue(10).build());

    @Override
    public void onUpdate() {
        if (mc.player.isInWater() || mc.player.isInLava()) mc.player.motionY = 0.1;
        else jump();
    }

    private void jump() {
        if (mc.player.onGround && timeout()) {
            mc.player.jump();
            startTime = 0;
        }
    }

    private static long startTime = 0;
    private boolean timeout() {
        if (startTime == 0) startTime = System.currentTimeMillis();
        if (startTime + ((delay.getValue() / 20) * 1000) <= System.currentTimeMillis()) { // 1 timeout = 1 second = 1000 ms
            startTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

}
