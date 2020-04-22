package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by 086 on 12/12/2017.
 */
@Module.Info(
        name = "Chams",
        category = Module.Category.RENDER,
        description = "See entities through walls"
)
public class Chams extends Module {

    private static Setting<Boolean> players = Settings.b("Players", true);
    private static Setting<Boolean> animals = Settings.b("Animals", false);
    private static Setting<Boolean> mobs = Settings.b("Mobs", false);

    public Chams() {
        registerAll(players, animals, mobs);
    }

    public static boolean renderChams(Entity entity) {
        return (entity instanceof EntityPlayer ? players.getValue() : (EntityUtil.isPassive(entity) ? animals.getValue() : mobs.getValue()));
    }

}
