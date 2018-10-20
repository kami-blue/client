package me.zeroeightsix.kami.module.modules.render;

import baritone.api.BaritoneAPI;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;

/**
 * Created by 086 on 25/01/2018.
 */

@Module.Info(name = "Pathfind", category = Module.Category.HIDDEN)
public class Pathfind extends Module {

    public Pathfind() {
        setEnabled(true);
//        BaritoneAPI.getSettings().chatDebug.value = false;
//        BaritoneAPI.getSettings().allowSprint.value = true;
//        BaritoneAPI.getSettings().antiCheatCompatibility.value = true;
//        BaritoneAPI.getSettings().walkWhileBreaking.value = false;
//        BaritoneAPI.getSettings().freeLook.value = false;
//        BaritoneAPI.getSettings().colorCurrentPath.value = Color.MAGENTA;
//        BaritoneAPI.getSettings().colorNextPath.value = Color.BLUE;
//        BaritoneAPI.getSettings().colorBestPathSoFar.value = Color.YELLOW;
//
//        BaritoneAPI.getPathingBehavior().setGoal(new GoalXZ(10000, 20000));
    }

    public static void tick() {
        if (mc.world == null) return;
        int fall = 3;
        fall += (0.5f * mc.player.getHealth()); // make sure falling wont kill the player
        BaritoneAPI.getSettings().maxFallHeightBucket.value = fall;
        BaritoneAPI.getSettings().assumeWalkOnWater.value = ModuleManager.isModuleEnabled("Jesus");
    }

}
