package me.zeroeightsix.kami.module.modules.player;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.util.math.MathHelper;

/**
 * Created by 086 on 16/12/2017.
 */
@Module.Info(
        name = "PitchLock",
        category = Module.Category.PLAYER,
        description = "Locks your camera pitch"
)
public class PitchLock extends Module {
    private Setting<Boolean> auto = register(Settings.b("Auto", true));
    private Setting<Float> pitch = register(Settings.f("Pitch", 180));
    private Setting<Integer> slice = register(Settings.i("Slice", 8));

    @Override
    public void onUpdate() {
        if (slice.getValue() == 0) return;
        if (auto.getValue()) {
            int angle = 360 / slice.getValue();
            float yaw = mc.player.rotationPitch;
            yaw = Math.round(yaw / angle) * angle;
            mc.player.rotationPitch = yaw;
            if (mc.player.isRiding()) mc.player.getRidingEntity().rotationPitch = yaw;
        } else {
            mc.player.rotationPitch = MathHelper.clamp(pitch.getValue() - 180, -180, 180);
        }
    }
}
