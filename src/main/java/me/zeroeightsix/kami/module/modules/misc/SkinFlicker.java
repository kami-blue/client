package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.entity.player.EnumPlayerModelParts;

import java.util.Random;

/**
 * Created by 086 on 30/01/2018.
 */
@Module.Info(
        name = "SkinFlicker",
        description = "Toggle your skin layers rapidly for a cool skin effect",
        category = Module.Category.MISC
)
public class SkinFlicker extends Module {

    private Setting<FlickerMode> mode = register(Settings.e("Mode", FlickerMode.HORIZONTAL));
    private Setting<Integer> slowness = register(Settings.integerBuilder().withName("Slowness").withValue(2).withMinimum(1).build());

    private final static EnumPlayerModelParts[] PARTS_HORIZONTAL = new EnumPlayerModelParts[]{
            EnumPlayerModelParts.LEFT_SLEEVE,
            EnumPlayerModelParts.JACKET,
            EnumPlayerModelParts.HAT,
            EnumPlayerModelParts.LEFT_PANTS_LEG,
            EnumPlayerModelParts.RIGHT_PANTS_LEG,
            EnumPlayerModelParts.RIGHT_SLEEVE
    };

    private final static EnumPlayerModelParts[] PARTS_VERTICAL = new EnumPlayerModelParts[]{
            EnumPlayerModelParts.HAT,
            EnumPlayerModelParts.JACKET,
            EnumPlayerModelParts.LEFT_SLEEVE,
            EnumPlayerModelParts.RIGHT_SLEEVE,
            EnumPlayerModelParts.LEFT_PANTS_LEG,
            EnumPlayerModelParts.RIGHT_PANTS_LEG,
    };

    private Random r = new Random();
    private int len = EnumPlayerModelParts.values().length;

    @Override
    public void onUpdate() {
        switch (mode.getValue()) {
            case RANDOM:
                if (mc.player.ticksExisted % slowness.getValue() != 0) return;
                mc.gameSettings.switchModelPartEnabled(EnumPlayerModelParts.values()[r.nextInt(len)]);
                break;
            case VERTICAL:
            case HORIZONTAL:
                int i = (mc.player.ticksExisted / slowness.getValue()) % (PARTS_HORIZONTAL.length * 2); // *2 for on/off
                boolean on = false;
                if (i >= PARTS_HORIZONTAL.length) {
                    on = true;
                    i -= PARTS_HORIZONTAL.length;
                }
                mc.gameSettings.setModelPartEnabled(mode.getValue() == FlickerMode.VERTICAL ? PARTS_VERTICAL[i] : PARTS_HORIZONTAL[i], on);
        }
    }

    public enum FlickerMode {
        HORIZONTAL, VERTICAL, RANDOM
    }

    public void onDisable() {
        for (int i = 0 ; i < EnumPlayerModelParts.values().length ; i++) {
            mc.gameSettings.setModelPartEnabled(PARTS_VERTICAL[i], true);
        }
    }

}
