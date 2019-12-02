package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.init.Blocks;

/**
 * Created on 26 October 2019 by hub
 * Updated 24 November 2019 by hub
 */
@Module.Info(name = "IceSpeed", description = "Ice Speed", category = Module.Category.MOVEMENT)
public class IceSpeed extends Module {

    private Setting<Float> slipperiness = register(Settings.floatBuilder("Slipperiness").withMinimum(0.2f).withValue(0.4f).withMaximum(1.0f).build());

    @Override
    public void onUpdate() {
        Blocks.ICE.slipperiness = slipperiness.getValue();
        Blocks.PACKED_ICE.slipperiness = slipperiness.getValue();
        Blocks.FROSTED_ICE.slipperiness = slipperiness.getValue();
    }

    @Override
    public void onDisable() {
        Blocks.ICE.slipperiness = 0.98f;
        Blocks.PACKED_ICE.slipperiness = 0.98f;
        Blocks.FROSTED_ICE.slipperiness = 0.98f;
    }

}
