package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.misc.AutoTool;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.LagCompensator;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * Created by 086 on 12/12/2017.
 * Updated by hub on 31 October 2019
 * Updated by S-B99 on 20/12/2019
 */
@Module.Info(name = "Aura", category = Module.Category.COMBAT, description = "Hits entities around you")
public class Aura extends Module {

    private Setting<Boolean> attackPlayers = register(Settings.b("Players", true));
    private Setting<Boolean> attackMobs = register(Settings.b("Mobs", false));
    private Setting<Boolean> attackAnimals = register(Settings.b("Animals", false));
    private Setting<Double> hitRange = register(Settings.d("Hit Range", 5.5d));
    private Setting<Boolean> ignoreWalls = register(Settings.b("Ignore Walls", true));
    private Setting<WaitMode> waitMode = register(Settings.e("Mode", WaitMode.TICK));
    private Setting<Double> waitTick = register(Settings.doubleBuilder("Tick Delay").withMinimum(0.1).withValue(2.0).withMaximum(20.0).build());
    private Setting<Boolean> autoWait = register(Settings.b("Auto Tick Delay", true));
    private Setting<SwitchMode> switchMode = register(Settings.e("Autoswitch", SwitchMode.ALL));
    private Setting<HitMode> hitMode = register(Settings.e("Tool", HitMode.SWORD));
    private Setting<Boolean> infoMsg = register(Settings.b("Info Message", true));
    //private Setting<Boolean> onlyUse32k = register(Settings.b("32k Only", false));

    private int waitCounter;

    private enum SwitchMode {
        NONE, ALL, ONLY32K
    }

    private enum HitMode {
        SWORD, AXE
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            return;
        }
        if (autoWait.getValue() && infoMsg.getValue()) {
            Command.sendWarningMessage(getChatName() + " When Auto Tick Delay is turned on whatever you give Tick Delay doesn't matter, it uses the current TPS instead");
        }
    }

    @Override
    public void onUpdate() {

        double autoWaitTick = 0;

        if (mc.player.isDead || mc.player == null) {
            return;
        }

        if (autoWait.getValue()) {
            //String tpsString = Double.toString(Math.round(LagCompensator.INSTANCE.getTickRate() * 10) / 10.0);
            autoWaitTick = 20 - (Math.round(LagCompensator.INSTANCE.getTickRate() * 10) / 10.0);
            //String autoWaitString = Double.toString(autoWaitTick);

            //Command.sendWarningMessage(tpsString);
            //Command.sendWarningMessage(autoWaitString);
        }
        boolean shield = mc.player.getHeldItemOffhand().getItem().equals(Items.SHIELD) && mc.player.getActiveHand() == EnumHand.OFF_HAND;
        if (mc.player.isHandActive() && !shield) {
            return;
        }

        if (waitMode.getValue().equals(WaitMode.CPS)) {
            if (mc.player.getCooledAttackStrength(getLagComp()) < 1) {
                return;
            } else if (mc.player.ticksExisted % 2 != 0) {
                return;
            }
        }

        if (autoWait.getValue()) {
            if (waitMode.getValue().equals(WaitMode.TICK) && autoWaitTick > 0) {
                if (waitCounter < autoWaitTick) {
                    waitCounter++;
                    return;
                } else {
                    waitCounter = 0;
                }
            }
        } else {
            if (waitMode.getValue().equals(WaitMode.TICK) && waitTick.getValue() > 0) {
                if (waitCounter < waitTick.getValue()) {
                    waitCounter++;
                    return;
                } else {
                    waitCounter = 0;
                }
            }
        }

        Iterator<Entity> entityIterator = Minecraft.getMinecraft().world.loadedEntityList.iterator();
        while (entityIterator.hasNext()) {
            Entity target = entityIterator.next();
            if (!EntityUtil.isLiving(target)) {
                continue;
            }
            if (target == mc.player) {
                continue;
            }
            if (mc.player.getDistance(target) > hitRange.getValue()) {
                continue;
            }
            if (((EntityLivingBase) target).getHealth() <= 0) {
                continue;
            }
            if (waitMode.getValue().equals(WaitMode.CPS) && ((EntityLivingBase) target).hurtTime != 0) {
                continue;
            }
            if (!ignoreWalls.getValue() && (!mc.player.canEntityBeSeen(target) && !canEntityFeetBeSeen(target))) {
                continue; // If walls is on & you can't see the feet or head of the target, skip. 2 raytraces needed
            }
            if (attackPlayers.getValue() && target instanceof EntityPlayer && !Friends.isFriend(target.getName())) {
                attack(target);
                return;
            } else {
                if (EntityUtil.isPassive(target) ? attackAnimals.getValue() : (EntityUtil.isMobAggressive(target) && attackMobs.getValue())) {
                    // We want to skip this if switchTo32k.getValue() is true,
                    // because it only accounts for tools and weapons.
                    // Maybe someone could refactor this later? :3
                    if ((!switchMode.getValue().equals(SwitchMode.ONLY32K) || switchMode.getValue().equals(SwitchMode.ALL)) && MODULE_MANAGER.isModuleEnabled(AutoTool.class)) {
                        AutoTool.equipBestWeapon();
                    }
                    attack(target);
                    return;
                }
            }
        }

    }

    private boolean checkSharpness(ItemStack stack) {

        if (stack.getTagCompound() == null) {
            return false;
        }

        if (stack.getItem().equals(Items.DIAMOND_AXE) && hitMode.getValue().equals(HitMode.SWORD)) {
            return false;
        }

        if (stack.getItem().equals(Items.DIAMOND_SWORD) && hitMode.getValue().equals(HitMode.AXE)) {
            return false;
        }

        NBTTagList enchants = (NBTTagList) stack.getTagCompound().getTag("ench");

        // IntelliJ marks (enchants == null) as always false but this is not correct.
        // In case of a Hotbar Slot without any Enchantment on the Stack it contains,
        // this will throw a NullPointerException if not accounted for!
        //noinspection ConstantConditions
        if (enchants == null) {
            return false;
        }

        for (int i = 0; i < enchants.tagCount(); i++) {
            NBTTagCompound enchant = enchants.getCompoundTagAt(i);
            if (enchant.getInteger("id") == 16) {
                int lvl = enchant.getInteger("lvl");
                if (switchMode.getValue().equals(SwitchMode.ONLY32K)) {
                    if (lvl >= 42) { // dia sword against full prot 5 armor is deadly somehere >= 34 sharpness iirc
                        return true;
                    }
                } else if (switchMode.getValue().equals(SwitchMode.ALL)) {
                    if (lvl >= 4) {
                        return true;
                    }
                } else if (switchMode.getValue().equals(SwitchMode.NONE)) {
                    return true;
                }
                break;
            }
        }

        return false;

    }

    private void attack(Entity e) {

        boolean holding32k = false;

        if (checkSharpness(mc.player.getHeldItemMainhand())) {
            holding32k = true;
        }

        if ((switchMode.getValue().equals(SwitchMode.ONLY32K) || switchMode.getValue().equals(SwitchMode.ALL)) && !holding32k) {

            int newSlot = -1;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (stack == ItemStack.EMPTY) {
                    continue;
                }
                if (checkSharpness(stack)) {
                    newSlot = i;
                    break;
                }
            }

            if (newSlot != -1) {
                mc.player.inventory.currentItem = newSlot;
                holding32k = true;
            }

        }

        if (switchMode.getValue().equals(SwitchMode.ONLY32K) && !holding32k) {
            return;
        }

        mc.playerController.attackEntity(mc.player, e);
        mc.player.swingArm(EnumHand.MAIN_HAND);

    }

    private float getLagComp() {
        if (waitMode.getValue().equals(WaitMode.CPS)) {
            return -(20 - LagCompensator.INSTANCE.getTickRate());
        }
        return 0.0F;
    }

    private boolean canEntityFeetBeSeen(Entity entityIn) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ), false, true, false) == null;
    }

    private enum WaitMode {
        CPS, TICK
    }

}
