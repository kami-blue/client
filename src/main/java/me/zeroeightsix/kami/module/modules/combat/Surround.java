package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.player.Freecam;
import me.zeroeightsix.kami.module.modules.player.NoBreakAnimation;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.BlockInteractionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * @author hub
 * @see me.zeroeightsix.kami.module.modules.combat.AutoFeetPlace
 * Updated by Polymer on 09/01/20
 * Updated by S-B99 on 28/01/20
 */
@Module.Info(name = "Surround", category = Module.Category.COMBAT, description = "Surrounds you with obsidian to take less damage")
public class Surround extends Module {

    public Setting<Boolean> autoDisable = register(Settings.b("Disable on place", true));
    private Setting<Boolean> spoofRotations = register(Settings.b("Spoof Rotations", true));
    private Setting<Boolean> spoofHotbar = register(Settings.b("Spoof Hotbar", false));
    private Setting<Double> blockPerTick = register(Settings.doubleBuilder("Blocks per Tick").withMinimum(1.0).withValue(4.0).withMaximum(10.0).build());
    private Setting<DebugMsgs> debugMsgs = register(Settings.e("Debug Messages", DebugMsgs.IMPORTANT));
    private Setting<AutoCenter> autoCenter = register(Settings.e("Auto Center", AutoCenter.TP));
    private Setting<Boolean> placeAnimation = register(Settings.b("Place Animation", false));

    private final Vec3d[] surroundTargets = new Vec3d[]{new Vec3d(0.0D, 0.0D, 0.0D), new Vec3d(1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, 1.0D), new Vec3d(-1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, -1.0D), new Vec3d(1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, 1.0D), new Vec3d(-1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, -1.0D), new Vec3d(1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, 1.0D), new Vec3d(-1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, -1.0D)};

    private Vec3d playerPos;
    private BlockPos basePos;
    private int offsetStep = 0;
    private int playerHotbarSlot = -1;
    private int lastHotbarSlot = -1;

    private enum DebugMsgs {
        NONE, IMPORTANT, ALL
    }

    private enum AutoCenter {
        OFF, TP
    }

    public void onUpdate() {
        if (mc.player != null && !MODULE_MANAGER.isModuleEnabled(Freecam.class)) {
            if (offsetStep == 0) {
                basePos = (new BlockPos(mc.player.getPositionVector())).down();
                playerHotbarSlot = mc.player.inventory.currentItem;
                if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                    Command.sendChatMessage(getChatName() + " Starting Loop, current Player Slot: " + playerHotbarSlot);
                }

                if (!spoofHotbar.getValue()) {
                    lastHotbarSlot = mc.player.inventory.currentItem;
                }
            }

            for (int i = 0; i < (int) Math.floor(blockPerTick.getValue()); ++i) {
                if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                    Command.sendChatMessage(getChatName() + " Loop iteration: " + offsetStep);
                }

                if (offsetStep >= surroundTargets.length) {
                    endLoop();
                    return;
                }

                Vec3d offset = surroundTargets[offsetStep];
                placeBlock(new BlockPos(basePos.add(offset.x, offset.y, offset.z)));
                ++offsetStep;
            }
        }
    }

    /* Autocenter */
    private void centerPlayer(double x, double y, double z) {
        if (debugMsgs.getValue().equals(DebugMsgs.ALL) && playerPos != null) {
            Command.sendChatMessage(getChatName() + " Player position is " + playerPos.toString());
        }
        else if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
            Command.sendChatMessage(getChatName() + " Player position is null");
        }
        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, true));
        mc.player.setPosition(x, y, z);
    }
    double getDst(Vec3d vec) {
        return playerPos.distanceTo(vec);
    }
    /* End of Autocenter */

    public void onEnable() {
        if (mc.player == null) return;
        /* Autocenter */
        BlockPos centerPos = mc.player.getPosition();
        playerPos = mc.player.getPositionVector();
        double y = centerPos.getY();
        double x = centerPos.getX();
        double z = centerPos.getZ();

        final Vec3d plusPlus = new Vec3d(x + 0.5, y, z + 0.5);
        final Vec3d plusMinus = new Vec3d(x + 0.5, y, z - 0.5);
        final Vec3d minusMinus = new Vec3d(x - 0.5, y, z - 0.5);
        final Vec3d minusPlus = new Vec3d(x - 0.5, y, z + 0.5);

        if (autoCenter.getValue().equals(AutoCenter.TP)) {
            if (getDst(plusPlus) < getDst(plusMinus) && getDst(plusPlus) < getDst(minusMinus) && getDst(plusPlus) < getDst(minusPlus)) {
                x = centerPos.getX() + 0.5;
                z = centerPos.getZ() + 0.5;
                centerPlayer(x, y, z);
            } if (getDst(plusMinus) < getDst(plusPlus) && getDst(plusMinus) < getDst(minusMinus) && getDst(plusMinus) < getDst(minusPlus)) {
                x = centerPos.getX() + 0.5;
                z = centerPos.getZ() - 0.5;
                centerPlayer(x, y, z);
            } if (getDst(minusMinus) < getDst(plusPlus) && getDst(minusMinus) < getDst(plusMinus) && getDst(minusMinus) < getDst(minusPlus)) {
                x = centerPos.getX() - 0.5;
                z = centerPos.getZ() - 0.5;
                centerPlayer(x, y, z);
            } if (getDst(minusPlus) < getDst(plusPlus) && getDst(minusPlus) < getDst(plusMinus) && getDst(minusPlus) < getDst(minusMinus)) {
                x = centerPos.getX() - 0.5;
                z = centerPos.getZ() + 0.5;
                centerPlayer(x, y, z);
            }
        }
        /* End of Autocenter*/

        playerHotbarSlot = mc.player.inventory.currentItem;
        lastHotbarSlot = -1;
        if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
            Command.sendChatMessage(getChatName() + " Saving initial Slot  = " + playerHotbarSlot);
        }
    }

    public void onDisable() {
        if (mc.player != null) {
            if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                Command.sendChatMessage(getChatName() + " Disabling");
            }

            if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1) {
                if (spoofHotbar.getValue()) {
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(playerHotbarSlot));
                } else {
                    mc.player.inventory.currentItem = playerHotbarSlot;
                }
            }
            playerHotbarSlot = -1;
            lastHotbarSlot = -1;
        }
    }

    private void endLoop() {
        offsetStep = 0;
        if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
            Command.sendChatMessage(getChatName() + " Ending Loop");
        }
        if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1) {
            if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                Command.sendChatMessage(getChatName() + " Setting Slot back to  = " + playerHotbarSlot);
            }

            if (spoofHotbar.getValue()) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(playerHotbarSlot));
            } else {
                mc.player.inventory.currentItem = playerHotbarSlot;
            }

            lastHotbarSlot = playerHotbarSlot;
        }
        if (autoDisable.getValue()) {
            disable();
        }
    }

    private void placeBlock(BlockPos blockPos) {
        if (!mc.world.getBlockState(blockPos).getMaterial().isReplaceable()) {
            if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                Command.sendChatMessage(getChatName() + " Block is already placed, skipping");
            }
        } else if (!BlockInteractionHelper.checkForNeighbours(blockPos) && debugMsgs.getValue().equals(DebugMsgs.ALL)) {
            Command.sendChatMessage(getChatName() + " !checkForNeighbours(blockPos), disabling! ");
        } else {
            if (placeAnimation.getValue()) mc.player.connection.sendPacket(new CPacketAnimation(mc.player.getActiveHand()));
            placeBlockExecute(blockPos);
        }
        if (MODULE_MANAGER.isModuleEnabled(NoBreakAnimation.class)) {
            ((NoBreakAnimation) MODULE_MANAGER.getModule(NoBreakAnimation.class)).resetMining();
        }
    }

    private int findObiInHotbar() {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) stack.getItem()).getBlock();
                if (block instanceof BlockObsidian) {
                    slot = i;
                    break;
                }
            }
        }
        return slot;
    }

    public void placeBlockExecute(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight(), mc.player.posZ);
        EnumFacing[] var3 = EnumFacing.values();

        for (EnumFacing side : var3) {
            BlockPos neighbor = pos.offset(side);
            EnumFacing side2 = side.getOpposite();
            if (!canBeClicked(neighbor)) {
                if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                    Command.sendChatMessage(getChatName() + " No neighbor to click at!");
                }
            } else {
                Vec3d hitVec = (new Vec3d(neighbor)).add(0.5D, 0.5D, 0.5D).add((new Vec3d(side2.getDirectionVec())).scale(0.5D));
                if (eyesPos.squareDistanceTo(hitVec) <= 18.0625D) {
                    if (spoofRotations.getValue()) {
                        faceVectorPacketInstant(hitVec);
                    }
                    boolean needSneak = false;
                    Block blockBelow = mc.world.getBlockState(neighbor).getBlock();
                    if (BlockInteractionHelper.blackList.contains(blockBelow) || BlockInteractionHelper.shulkerList.contains(blockBelow)) {
                        if (debugMsgs.getValue().equals(DebugMsgs.IMPORTANT)) {
                            Command.sendChatMessage(getChatName() + " Sneak enabled!");
                        }
                        needSneak = true;
                    }
                    if (needSneak) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
                    }

                    int obiSlot = findObiInHotbar();
                    if (obiSlot == -1) {
                        if (debugMsgs.getValue().equals(DebugMsgs.IMPORTANT)) {
                            Command.sendChatMessage(getChatName() + " No obsidian in hotbar, disabling!");
                        }
                        disable();
                        return;
                    }

                    if (lastHotbarSlot != obiSlot) {
                        if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                            Command.sendChatMessage(getChatName() + " Setting Slot to obsidian at  = " + obiSlot);
                        }
                        if (spoofHotbar.getValue()) {
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(obiSlot));
                        } else {
                            mc.player.inventory.currentItem = obiSlot;
                        }
                        lastHotbarSlot = obiSlot;
                    }

                    mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
                    mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                    if (needSneak) {
                        if (debugMsgs.getValue().equals(DebugMsgs.IMPORTANT)) {
                            Command.sendChatMessage(getChatName() + " Sneak disabled!");
                        }
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
                    }
                    return;
                }

                if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                    Command.sendChatMessage(getChatName() + " Distance > 4.25 blocks!");
                }
            }
        }
    }

    private static boolean canBeClicked(BlockPos pos) {
        return getBlock(pos).canCollideCheck(getState(pos), false);
    }

    public static Block getBlock(BlockPos pos) {
        return getState(pos).getBlock();
    }

    private static IBlockState getState(BlockPos pos) {
        return mc.world.getBlockState(pos);
    }

    private static void faceVectorPacketInstant(Vec3d vec) {
        float[] rotations = getLegitRotations(vec);
        mc.player.connection.sendPacket(new Rotation(rotations[0], rotations[1], mc.player.onGround));
    }

    private static float[] getLegitRotations(Vec3d vec) {
        Vec3d eyesPos = getEyesPos();
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw), mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch)};
    }

    private static Vec3d getEyesPos() {
        return new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight(), mc.player.posZ);
    }
}
