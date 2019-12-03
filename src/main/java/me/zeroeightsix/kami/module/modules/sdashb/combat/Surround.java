package me.zeroeightsix.kami.module.modules.sdashb.combat;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.BlockInteractionHelper;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;


/**
 * @author Unknown, LGPL licensed
 * Updated by S-B99 on 20/11/19
 */
@Module.Info(name = "Surround", category = Module.Category.COMBAT, description = "Surrounds you with obsidian")
public class Surround extends Module {
    //private final Vec3d[] surroundTargetsCritical = new Vec3d[]{new Vec3d(0.0D, 0.0D, 0.0D), new Vec3d(1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, 1.0D), new Vec3d(-1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, -1.0D)};
    private Setting toggleable = this.register(Settings.b("Toggleable", true));
    private Setting spoofRotations = this.register(Settings.b("Spoof Rotations", true));
    private Setting spoofHotbar = this.register(Settings.b("Spoof Hotbar", true));
    private Setting<Double> blockPerTick = this.register(Settings.doubleBuilder("Blocks per Tick").withMinimum(1.0).withValue(4.0).withMaximum(10.0).build());
    //private Setting<PlaceMode> placeMode = register(Settings.e("Mode", PlaceMode.HALF));
    private Setting<DebugMsgs> debugMsgs = register(Settings.e("Debug Messages", DebugMsgs.IMPORTANT));


    private final Vec3d[] surroundTargets = new Vec3d[]{new Vec3d(0.0D, 0.0D, 0.0D), new Vec3d(1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, 1.0D), new Vec3d(-1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, -1.0D), new Vec3d(1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, 1.0D), new Vec3d(-1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, -1.0D), new Vec3d(1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, 1.0D), new Vec3d(-1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, -1.0D)};
    private final Vec3d[] surroundTargetsFull = new Vec3d[]{new Vec3d(0.0D, 0.0D, 0.0D), new Vec3d(1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, 1.0D), new Vec3d(-1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, -1.0D), new Vec3d(1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, 1.0D), new Vec3d(-1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, -1.0D), new Vec3d(1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, 1.0D), new Vec3d(-1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, -1.0D)};

    private BlockPos basePos;
    private int offsetStep = 0;
    private int playerHotbarSlot = -1;
    private int lastHotbarSlot = -1;


//    private enum PlaceMode {
//        HALF, FULL
//    }

    private enum DebugMsgs {
        NONE, IMPORTANT, ALL
    }

    public void onUpdate() {
        if (!this.isDisabled() && mc.player != null && !ModuleManager.isModuleEnabled("Freecam")) {
            if (this.offsetStep == 0) {
                this.basePos = (new BlockPos(mc.player.getPositionVector())).down();
                this.playerHotbarSlot = Wrapper.getPlayer().inventory.currentItem;
                if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                    Command.sendChatMessage("[Surround] Starting Loop, current Player Slot: " + this.playerHotbarSlot);
                }

                if (!(Boolean)this.spoofHotbar.getValue()) {
                    this.lastHotbarSlot = mc.player.inventory.currentItem;
                }
            }

            for(int i = 0; i < (int)Math.floor(this.blockPerTick.getValue()); ++i) {
                if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                    Command.sendChatMessage("[Surround] Loop iteration: " + this.offsetStep);
                }

                if (this.offsetStep >= this.surroundTargets.length) {
                    this.endLoop();
                    return;
                }

                Vec3d offset = this.surroundTargets[this.offsetStep];
                this.placeBlock(new BlockPos(this.basePos.add(offset.x, offset.y, offset.z)));
                ++this.offsetStep;
            }

        }
    }

    protected void onEnable() {
        if (mc.player == null) {
            this.disable();
        } else {
            if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                Command.sendChatMessage("[Surround] Enabling");
            }

            this.playerHotbarSlot = Wrapper.getPlayer().inventory.currentItem;
            this.lastHotbarSlot = -1;
            if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                Command.sendChatMessage("[Surround] Saving initial Slot  = " + this.playerHotbarSlot);
            }

        }
    }

    protected void onDisable() {
        if (mc.player != null) {
            if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                Command.sendChatMessage("[Surround] Disabling");
            }

            if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
                if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                    //Command.sendChatMessage("[Surround] Setting Slot to  = " + this.playerHotbarSlot);
                }

                if ((Boolean)this.spoofHotbar.getValue()) {
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(this.playerHotbarSlot));
                } else {
                    Wrapper.getPlayer().inventory.currentItem = this.playerHotbarSlot;
                }
            }

            this.playerHotbarSlot = -1;
            this.lastHotbarSlot = -1;
        }
    }

    private void endLoop() {
        this.offsetStep = 0;
        if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
            Command.sendChatMessage("[Surround] Ending Loop");
        }

        if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
            if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                Command.sendChatMessage("[Surround] Setting Slot back to  = " + this.playerHotbarSlot);
            }

            if ((Boolean)this.spoofHotbar.getValue()) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(this.playerHotbarSlot));
            } else {
                Wrapper.getPlayer().inventory.currentItem = this.playerHotbarSlot;
            }

            this.lastHotbarSlot = this.playerHotbarSlot;
        }

        if (!(Boolean)this.toggleable.getValue()) {
            this.disable();
        }

    }

    private void placeBlock(BlockPos blockPos) {
        if (!Wrapper.getWorld().getBlockState(blockPos).getMaterial().isReplaceable()) {
            if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                Command.sendChatMessage("[Surround] Block is already placed, skipping");
            }

        } else if (!BlockInteractionHelper.checkForNeighbours(blockPos)) {
            if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                Command.sendChatMessage("[Surround] !checkForNeighbours(blockPos), disabling! ");
            }

        } else {
            this.placeBlockExecute(blockPos);
        }
    }

    private int findObiInHotbar() {
        int slot = -1;

        for(int i = 0; i < 9; ++i) {
            ItemStack stack = Wrapper.getPlayer().inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock)stack.getItem()).getBlock();
                if (block instanceof BlockObsidian) {
                    slot = i;
                    break;
                }
            }
        }

        return slot;
    }

    public void placeBlockExecute(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(Wrapper.getPlayer().posX, Wrapper.getPlayer().posY + (double)Wrapper.getPlayer().getEyeHeight(), Wrapper.getPlayer().posZ);
        EnumFacing[] var3 = EnumFacing.values();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            EnumFacing side = var3[var5];
            BlockPos neighbor = pos.offset(side);
            EnumFacing side2 = side.getOpposite();
            if (!canBeClicked(neighbor)) {
                if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                    Command.sendChatMessage("[Surround] No neighbor to click at!");
                }
            } else {
                Vec3d hitVec = (new Vec3d(neighbor)).add(0.5D, 0.5D, 0.5D).add((new Vec3d(side2.getDirectionVec())).scale(0.5D));
                if (eyesPos.squareDistanceTo(hitVec) <= 18.0625D) {
                    if ((Boolean)this.spoofRotations.getValue()) {
                        faceVectorPacketInstant(hitVec);
                    }

                    boolean needSneak = false;
                    Block blockBelow = mc.world.getBlockState(neighbor).getBlock();
                    if (BlockInteractionHelper.blackList.contains(blockBelow) || BlockInteractionHelper.shulkerList.contains(blockBelow)) {
                        if (debugMsgs.getValue().equals(DebugMsgs.IMPORTANT)) {
                            Command.sendChatMessage("[Surround] Sneak enabled!");
                        }

                        needSneak = true;
                    }

                    if (needSneak) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
                    }

                    int obiSlot = this.findObiInHotbar();
                    if (obiSlot == -1) {
                        if (debugMsgs.getValue().equals(DebugMsgs.IMPORTANT)) {
                            Command.sendChatMessage("[Surround] No Obi in Hotbar, disabling!");
                        }

                        this.disable();
                        return;
                    }

                    if (this.lastHotbarSlot != obiSlot) {
                        if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                            Command.sendChatMessage("[Surround] Setting Slot to Obi at  = " + obiSlot);
                        }

                        if ((Boolean)this.spoofHotbar.getValue()) {
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(obiSlot));
                        } else {
                            Wrapper.getPlayer().inventory.currentItem = obiSlot;
                        }

                        this.lastHotbarSlot = obiSlot;
                    }

                    mc.playerController.processRightClickBlock(Wrapper.getPlayer(), mc.world, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
                    mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                    if (needSneak) {
                        if (debugMsgs.getValue().equals(DebugMsgs.IMPORTANT)) {
                            Command.sendChatMessage("[Surround] Sneak disabled!");
                        }

                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
                    }

                    return;
                }

                if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                    Command.sendChatMessage("[Surround] Distance > 4.25 blocks!");
                }
            }
        }

    }

    private static boolean canBeClicked(BlockPos pos) {
        return getBlock(pos).canCollideCheck(getState(pos), false);
    }

    private static Block getBlock(BlockPos pos) {
        return getState(pos).getBlock();
    }

    private static IBlockState getState(BlockPos pos) {
        return Wrapper.getWorld().getBlockState(pos);
    }

    private static void faceVectorPacketInstant(Vec3d vec) {
        float[] rotations = getLegitRotations(vec);
        Wrapper.getPlayer().connection.sendPacket(new Rotation(rotations[0], rotations[1], Wrapper.getPlayer().onGround));
    }

    private static float[] getLegitRotations(Vec3d vec) {
        Vec3d eyesPos = getEyesPos();
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{Wrapper.getPlayer().rotationYaw + MathHelper.wrapDegrees(yaw - Wrapper.getPlayer().rotationYaw), Wrapper.getPlayer().rotationPitch + MathHelper.wrapDegrees(pitch - Wrapper.getPlayer().rotationPitch)};
    }

    private static Vec3d getEyesPos() {
        return new Vec3d(Wrapper.getPlayer().posX, Wrapper.getPlayer().posY + (double)Wrapper.getPlayer().getEyeHeight(), Wrapper.getPlayer().posZ);
    }


}