package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;

/**
 * Created by Dewy on the 16th of April, 2020
 */
@Module.Info(name = "AimBot", description = "Automatically aims at entities for you.", category = Module.Category.COMBAT)
public class AimBot extends Module {

    private Setting<Integer> range = register(Settings.integerBuilder("Range").withMinimum(4).withMaximum(24).withValue(16));
    private Setting<Boolean> useBow = register(Settings.booleanBuilder("Use Bow").withValue(true));
    private Setting<Boolean> ignoreWalls = register(Settings.booleanBuilder("Ignore Walls").withValue(true));
    private Setting<Boolean> targetPlayers = register(Settings.booleanBuilder("Target Players").withValue(true));
    private Setting<Boolean> targetFriends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility(v -> targetPlayers.getValue().equals(true)));
    private Setting<Boolean> targetSleeping = register(Settings.booleanBuilder("Sleeping").withValue(false).withVisibility(v -> targetPlayers.getValue().equals(true)));
    private Setting<Boolean> targetMobs = register(Settings.booleanBuilder("Target Mobs").withValue(false));
    private Setting<Boolean> targetHostileMobs = register(Settings.booleanBuilder("Hostile").withValue(true).withVisibility(v -> targetMobs.getValue().equals(true)));
    private Setting<Boolean> targetPassiveMobs = register(Settings.booleanBuilder("Passive").withValue(false).withVisibility(v -> targetMobs.getValue().equals(true)));

    @Override
    public void onUpdate() {
        if (KamiMod.MODULE_MANAGER.getModuleT(Aura.class).isEnabled()) {
            return;
        }

        if (useBow.getValue()) {
            int bowSlot = -1;

            for (int i = 0; i < 9; i++) {
                ItemStack potentialBow = mc.player.inventory.getStackInSlot(i);

                if ((potentialBow.getItem() instanceof ItemBow)) {
                    bowSlot = mc.player.inventory.getSlotFor(potentialBow);
                }
            }

            mc.player.inventory.currentItem = bowSlot;
            mc.playerController.syncCurrentPlayItem();
        }

        for(Entity entity : mc.world.loadedEntityList) {

            if(entity instanceof EntityLivingBase) {
                EntityLivingBase potentialTarget = (EntityLivingBase) entity;

                if (!(potentialTarget instanceof EntityPlayerSP) && mc.player.getDistance(potentialTarget) <= range.getValue() && !(potentialTarget.getHealth() <= 0)) {
                    if (!ignoreWalls.getValue()) {
                        if (!mc.player.canEntityBeSeen(potentialTarget)) {
                            return;
                        }
                    }

                    if (targetMobs.getValue()) {
                        if (targetHostileMobs.getValue() && potentialTarget.getSoundCategory().equals(SoundCategory.HOSTILE)) {
                            faceEntity(potentialTarget);
                        }

                        if (targetPassiveMobs.getValue() && potentialTarget instanceof EntityAnimal) {
                            faceEntity(potentialTarget);
                        }
                    }

                    if (targetPlayers.getValue()) {
                        if (potentialTarget.isPlayerSleeping() && potentialTarget instanceof EntityPlayer && targetSleeping.getValue()) {
                            faceEntity(potentialTarget);
                        }

                        if (!targetFriends.getValue()) {
                            for (Friends.Friend friend : Friends.friends.getValue()) {
                                if (!friend.getUsername().equals(potentialTarget.getName())) {
                                    faceEntity(potentialTarget);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void faceEntity(Entity entity) {
        double diffX = entity.posX - mc.player.posX;
        double diffZ = entity.posZ - mc.player.posZ;

        double diffY = mc.player.posY + (double) mc.player.getEyeHeight() - (entity.posY + (double) entity.getEyeHeight());
        double xz = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) normalizeAngle((Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f);
        float pitch = (float) normalizeAngle((-Math.atan2(diffY, xz) * 180.0 / Math.PI));

        mc.player.setPositionAndRotation(mc.player.posX, mc.player.posY, mc.player.posZ, yaw, -pitch);
    }

    private double normalizeAngle(double angleIn) {
        while (angleIn <= -180.0) {
            angleIn += 360.0;
        }

        while (angleIn > 180.0) {
            angleIn -= 360.0;
        }

        return angleIn;
    }
}
