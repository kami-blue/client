package me.zeroeightsix.kami.module.modules.hidden;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.combat.Aura;
import me.zeroeightsix.kami.module.modules.combat.CrystalAura;
import me.zeroeightsix.kami.module.modules.player.Freecam;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.module.modules.combat.CrystalAura.getPlayerPos;
import static me.zeroeightsix.kami.util.BlockInteractionHelper.*;

/**
 * Created by hub on 7 August 2019
 * Updated by hub on 21 November 2019
 */
@Module.Info(name = "Hidden:Auto32k", category = Module.Category.HIDDEN, description = "Places blocks to dispense a 32k")
public class Auto32k extends Module {
    private static final DecimalFormat df = new DecimalFormat("#.#");

    private Setting<Boolean> moveToHotbar = register(Settings.b("Move 32k to Hotbar", true));
    private Setting<Boolean> autoEnableHitAura = register(Settings.b("Auto enable Hit Aura", true));
    //private Setting<Double> placeRange = register(Settings.d("Place Range", 4.0d));
    private Setting<Double> placeRange = register(Settings.doubleBuilder("Place range").withMinimum(1.0).withValue(4.0).withMaximum(10.0).build());
    private Setting<Integer> yOffset = register(Settings.i("Y Offset (Hopper)", 2));
    private Setting<Boolean> placeCloseToEnemy = register(Settings.b("Place close to enemy", false));
    private Setting<Boolean> placeObiOnTop = register(Settings.b("Place Obi on Top", true));
    private Setting<Boolean> debugMessages = register(Settings.b("Debug Messages", true));

    private int swordSlot;
    private static boolean isSneaking;

    @Override
    protected void onEnable() {

        if (isDisabled() || mc.player == null || MODULE_MANAGER.isModuleEnabled(Freecam.class)) {
            disable();
            return;
        }

        df.setRoundingMode(RoundingMode.CEILING);

        int hopperSlot = -1;
        int shulkerSlot = -1;
        int obiSlot = -1;
        swordSlot = -1;

        for (int i = 0; i < 9; i++) {

            if (hopperSlot != -1 && shulkerSlot != -1 && obiSlot != -1) {
                break;
            }

            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }

            Block block = ((ItemBlock) stack.getItem()).getBlock();

            if (block == Blocks.HOPPER) {
                hopperSlot = i;
            } else if (shulkerList.contains(block)) {
                shulkerSlot = i;
            } else if (block == Blocks.OBSIDIAN) {
                obiSlot = i;
            }

        }

        if (hopperSlot == -1) {
            if (debugMessages.getValue()) {
                Command.sendChatMessage(getChatName() + "Hopper missing, disabling.");
            }
            disable();
            return;
        }

        if (shulkerSlot == -1) {
            if (debugMessages.getValue()) {
                Command.sendChatMessage(getChatName() + "Shulker missing, disabling.");
            }
            disable();
            return;
        }

        int range = (int) Math.ceil(placeRange.getValue());

        CrystalAura crystalAura = (CrystalAura) MODULE_MANAGER.getModule(CrystalAura.class);
        List<BlockPos> placeTargetList = crystalAura.getSphere(getPlayerPos(), range, range, false, true, 0);
        Map<BlockPos, Double> placeTargetMap = new HashMap<>();

        BlockPos placeTarget = null;
        boolean useRangeSorting = false;

        for (BlockPos placeTargetTest : placeTargetList) {
            for (Entity entity : mc.world.loadedEntityList) {

                if (!(entity instanceof EntityPlayer)) {
                    continue;
                }

                if (entity == mc.player) {
                    continue;
                }

                if (Friends.isFriend(entity.getName())) {
                    continue;
                }

                if (yOffset.getValue() != 0) {
                    if (Math.abs(mc.player.getPosition().y - placeTargetTest.y) > Math.abs(yOffset.getValue())) {
                        continue;
                    }
                }

                if (isAreaPlaceable(placeTargetTest)) {
                    useRangeSorting = true;
                    double distanceToEntity = entity.getDistance(placeTargetTest.x, placeTargetTest.y, placeTargetTest.z);
                    // Add distance to Map Value of placeTarget Key
                    placeTargetMap.put(placeTargetTest, placeTargetMap.containsKey(placeTargetTest) ? placeTargetMap.get(placeTargetTest) + distanceToEntity : distanceToEntity);
                    useRangeSorting = true;
                }

            }
        }

        if (placeTargetMap.size() > 0) {

            placeTargetMap.forEach((k, v) -> {
                if (!isAreaPlaceable(k)) {
                    placeTargetMap.remove(k);
                }
            });

            if (placeTargetMap.size() == 0) {
                useRangeSorting = false;
            }

        }

        if (useRangeSorting) {

            if (placeCloseToEnemy.getValue()) {
                if (debugMessages.getValue()) {
                    Command.sendChatMessage(getChatName() + "Placing close to Enemy");
                }
                // Get Key with lowest Value (closest to enemies)
                placeTarget = Collections.min(placeTargetMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            } else {
                if (debugMessages.getValue()) {
                    Command.sendChatMessage(getChatName() + "Placing far from Enemy");
                }
                // Get Key with highest Value (furthest away from enemies)
                placeTarget = Collections.max(placeTargetMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            }

        } else {

            if (debugMessages.getValue()) {
                Command.sendChatMessage(getChatName() + "No enemy nearby, placing at first valid position.");
            }

            // Use any place target position if no enemies are around
            for (BlockPos pos : placeTargetList) {
                if (isAreaPlaceable(pos)) {
                    placeTarget = pos;
                    break;
                }
            }

        }

        if (placeTarget == null) {
            if (debugMessages.getValue()) {
                Command.sendChatMessage(getChatName() + "No valid position in range to place!");
            }
            disable();
            return;
        }

        if (debugMessages.getValue()) {
            Command.sendChatMessage(getChatName() + "Place Target: " + placeTarget.x + " " + placeTarget.y + " " + placeTarget.z + " Distance: " + df.format(mc.player.getPositionVector().distanceTo(new Vec3d(placeTarget))));
        }

        mc.player.inventory.currentItem = hopperSlot;
        placeBlock(new BlockPos(placeTarget));

        mc.player.inventory.currentItem = shulkerSlot;
        placeBlock(new BlockPos(placeTarget.add(0, 1, 0)));

        if (placeObiOnTop.getValue() && obiSlot != -1) {
            mc.player.inventory.currentItem = obiSlot;
            placeBlock(new BlockPos(placeTarget.add(0, 2, 0)));
        }

        if (isSneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            isSneaking = false;
        }

        mc.player.inventory.currentItem = shulkerSlot;
        BlockPos hopperPos = new BlockPos(placeTarget);
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(hopperPos, EnumFacing.DOWN, EnumHand.MAIN_HAND, 0, 0, 0));
        swordSlot = shulkerSlot + 32;

    }

    @Override
    public void onUpdate() {

        if (isDisabled() || mc.player == null || MODULE_MANAGER.isModuleEnabled(Freecam.class)) {
            return;
        }

        if (!(mc.currentScreen instanceof GuiContainer)) {
            return;
        }

        if (!moveToHotbar.getValue()) {
            disable();
            return;
        }

        if (swordSlot == -1) {
            return;
        }

        boolean swapReady = true;

        if (((GuiContainer) mc.currentScreen).inventorySlots.getSlot(0).getStack().isEmpty) {
            swapReady = false;
        }

        if (!((GuiContainer) mc.currentScreen).inventorySlots.getSlot(swordSlot).getStack().isEmpty) {
            swapReady = false;
        }

        if (swapReady) {
            mc.playerController.windowClick(((GuiContainer) mc.currentScreen).inventorySlots.windowId, 0, swordSlot - 32, ClickType.SWAP, mc.player);
            if (autoEnableHitAura.getValue()) {
                MODULE_MANAGER.getModule(Aura.class).enable();
            }
            disable();
        }

    }

    private boolean isAreaPlaceable(BlockPos blockPos) {

        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(blockPos))) {
            if (entity instanceof EntityLivingBase) {
                return false; // entity on block
            }
        }

        if (!mc.world.getBlockState(blockPos).getMaterial().isReplaceable()) {
            return false; // no space for hopper
        }

        if (!mc.world.getBlockState(blockPos.add(0, 1, 0)).getMaterial().isReplaceable()) {
            return false; // no space for shulker
        }

        if (mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock() instanceof BlockAir) {
            return false; // air below hopper
        }

        if (mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock() instanceof BlockLiquid) {
            return false; // liquid below hopper
        }

        if (mc.player.getPositionVector().distanceTo(new Vec3d(blockPos)) > placeRange.getValue()) {
            return false; // out of range
        }

        Block block = mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock();
        if (blackList.contains(block) || shulkerList.contains(block)) {
            return false; // would need sneak
        }

        return !(mc.player.getPositionVector().distanceTo(new Vec3d(blockPos).add(0, 1, 0)) > placeRange.getValue()); // out of range

    }

    private static void placeBlock(BlockPos pos) {

        if (!mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
            return;
        }

        // check if we have a block adjacent to blockpos to click at
        if (!checkForNeighbours(pos)) {
            return;
        }

        for (EnumFacing side : EnumFacing.values()) {

            BlockPos neighbor = pos.offset(side);
            EnumFacing side2 = side.getOpposite();

            if (!mc.world.getBlockState(neighbor).getBlock().canCollideCheck(mc.world.getBlockState(neighbor), false)) {
                continue;
            }

            Vec3d hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));

            Block neighborPos = mc.world.getBlockState(neighbor).getBlock();
            if (blackList.contains(neighborPos) || shulkerList.contains(neighborPos)) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                isSneaking = true;
            }

            faceVectorPacketInstant(hitVec);
            mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            mc.rightClickDelayTimer = 4;

            return;

        }

    }

    private static boolean checkForNeighbours(BlockPos blockPos) {
        if (!hasNeighbour(blockPos)) {
            for (EnumFacing side : EnumFacing.values()) {
                BlockPos neighbour = blockPos.offset(side);
                if (hasNeighbour(neighbour)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private static boolean hasNeighbour(BlockPos blockPos) {
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbour = blockPos.offset(side);
            if (!mc.world.getBlockState(neighbour).getMaterial().isReplaceable()) {
                return true;
            }
        }
        return false;
    }

    //@Override
    //protected void onDisable() {
    //	if (autoEnableHitAura.getValue()) {
    //		MODULE_MANAGER.getModuleByName("Aura").disable();
    //	}
    //}

}
