package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.*;
import net.minecraft.util.EnumHand;

import java.util.Comparator;

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 05/04/20
 * updated by ionar2 on 04/05/20
 * Updated by dominikaaaa on 05/06/20
 * Updated by Superfly_Johnson on 07/21/20
 */
@Module.Info(
        name = "AutoRemount",
        description = "Automatically remounts your horse",
        category = Module.Category.MOVEMENT
)
public class AutoRemount extends Module {
    private Setting<Boolean> boat = register(Settings.b("Boats", true));
    private Setting<Boolean> horse = register(Settings.b("Horse", true));
    private Setting<Boolean> skeletonHorse = register(Settings.b("SkeletonHorse", true));
    private Setting<Boolean> donkey = register(Settings.b("Donkey", true));
    private Setting<Boolean> mule = register(Settings.b("Mule", true));
    private Setting<Boolean> pig = register(Settings.b("Pig", true));
    private Setting<Boolean> llama = register(Settings.b("Llama", true));
    private Setting<Float> range = register(Settings.floatBuilder("Range").withMinimum(1.0f).withValue(2.0f).withMaximum(10.0f).build());
    private Setting<Integer> delay = register(Settings.integerBuilder("Delay").withMinimum(-1).withValue(5).withMaximum(100).build());

    private int timer = delay.getValue();

    public void onUpdate() {
        if (mc.player.isRiding())
            return;

        // Bad. There is no way to check if a setting has been changed, this workaround works
        // but results in the value of timer being set to -1 every cycle. A onSettingChanged event
        // would fix this.
        if (timer>0){timer--;return;}
        else {timer=delay.getValue();}

        mc.world.loadedEntityList.stream()
                .filter(this::isValidEntity)
                .min(Comparator.comparing(en -> mc.player.getDistance(en)))
                .ifPresent(entity -> mc.playerController.interactWithEntity(mc.player, entity, EnumHand.MAIN_HAND));

    }

    private boolean isValidEntity(Entity entity) {
        if (mc.player.isRiding()) return false;

        if (entity.getDistance(mc.player) > range.getValue()) return false;

        if (entity instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse) entity;

            /// no animal abuse done in this module, no thanks.
            if (horse.isChild()) return false;
        }

        if (entity instanceof EntityBoat && boat.getValue()) return true;

        if (entity instanceof EntityHorse && horse.getValue()) return true;

        if (entity instanceof EntitySkeletonHorse && skeletonHorse.getValue()) return true;

        if (entity instanceof EntityDonkey && donkey.getValue()) return true;

        if (entity instanceof EntityMule && mule.getValue()) return true;

        if (entity instanceof EntityPig && pig.getValue()) {
            EntityPig pig = (EntityPig) entity;
            return pig.getSaddled();
        }

        return entity instanceof EntityLlama && llama.getValue();
    }
}
