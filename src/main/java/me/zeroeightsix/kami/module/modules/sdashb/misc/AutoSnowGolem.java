package me.zeroeightsix.kami.module.modules.sdashb.misc;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.combat.CrystalAura;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import static me.zeroeightsix.kami.module.modules.combat.CrystalAura.getPlayerPos;
import static me.zeroeightsix.kami.module.modules.player.Scaffold.faceVectorPacketInstant;

/**
 * @author hub/blockparole
 * Created by @S-B99 on 25/11/19
 */
@Module.Info(name = "AutoSnowGolem", category = Module.Category.MISC, description = "Automatically creates snowgolems")
public class AutoSnowGolem extends Module {

    private static final List<Block> blackList = Arrays.asList(
            Blocks.ENDER_CHEST,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.CRAFTING_TABLE,
            Blocks.ANVIL,
            Blocks.BREWING_STAND,
            Blocks.HOPPER,
            Blocks.DROPPER,
            Blocks.DISPENSER,
            Blocks.TRAPDOOR
    );

    private static final List<Block> shulkerList = Arrays.asList(
            Blocks.WHITE_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.SILVER_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX
    );

    private static final DecimalFormat df = new DecimalFormat("#.#");

    private Setting<Double> placeRange = this.register(Settings.doubleBuilder("Place range").withMinimum(1.0).withValue(4.0).withMaximum(10.0).build());
    private Setting<Boolean> placeCloseToEnemy = register(Settings.b("Place close to enemy", false));
    private Setting<Boolean> fastMode = register(Settings.b("Disable after placing", false));
    private Setting<PlaceMode> placeMode = register(Settings.e("Place Mode", PlaceMode.AUTO));
    //private Setting<Boolean> debugMessages = register(Settings.b("Debug Messages", true));
    private Setting<DebugMsgs> debugMsgs = register(Settings.e("Debug Messages", DebugMsgs.IMPORTANT));


    private int swordSlot;
    private static boolean isSneaking;

    private enum PlaceMode {
        AUTO, LOOK
    }
    private enum DebugMsgs {
        NONE, IMPORTANT, ALL
    }
    @Override
    public void onUpdate() {

        if (isDisabled() || mc.player == null || ModuleManager.isModuleEnabled("Freecam")) {
            this.disable();
            Command.sendChatMessage("[AutoSnowGolem] Freecam enabled, disabling");
            return;
        }

        df.setRoundingMode(RoundingMode.CEILING);

        int snowSlot = -1;
        int pumpkinSlot = -1;
        swordSlot = mc.player.inventory.currentItem;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }

            Block block = ((ItemBlock) stack.getItem()).getBlock();

            if (block == Blocks.SNOW) {
                snowSlot = i;
            }
            else if (block == Blocks.PUMPKIN) {
                pumpkinSlot = i;
            }

        }

        if (snowSlot == -1) {
            if (debugMsgs.getValue().equals(DebugMsgs.IMPORTANT)) {
                Command.sendChatMessage("[AutoSnowGolem] Snow missing, disabling.");
            }
            this.disable();
            return;
        }

        if (pumpkinSlot == -1) {
            if (debugMsgs.getValue().equals(DebugMsgs.IMPORTANT)) {
                Command.sendChatMessage("[AutoSnowGolem] Pumpkin missing, disabling.");
            }
            this.disable();
            return;
        }

        int range = (int) Math.ceil(placeRange.getValue());

        CrystalAura crystalAura = (CrystalAura) ModuleManager.getModuleByName("CrystalAura");

        BlockPos lookPos = mc.objectMouseOver.getBlockPos();

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
                if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                    Command.sendChatMessage("[AutoSnowGolem] Placing close to Enemy");
                }
                // Get Key with lowest Value (closest to enemies)
                placeTarget = Collections.min(placeTargetMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            } else {
                if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                    Command.sendChatMessage("[AutoSnowGolem] Placing far from Enemy");
                }
                // Get Key with highest Value (furthest away from enemies)
                placeTarget = Collections.max(placeTargetMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            }

        } else {

            if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                Command.sendChatMessage("[AutoSnowGolem] No enemy nearby, placing at first valid position.");
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
            if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
                Command.sendChatMessage("[AutoSnowGolem] No valid position in range to place!");
            }
            this.disable();
            return;
        }

        if (debugMsgs.getValue().equals(DebugMsgs.ALL)) {
            Command.sendChatMessage("[AutoSnowGolem] Place Target: " + placeTarget.x + " " + placeTarget.y + " " + placeTarget.z + " Distance: " + df.format(mc.player.getPositionVector().distanceTo(new Vec3d(placeTarget))));
        }

        if (placeMode.getValue().equals(PlaceMode.AUTO)) {
            mc.player.inventory.currentItem = snowSlot;
            placeBlock(new BlockPos(placeTarget));

            mc.player.inventory.currentItem = snowSlot;
            placeBlock(new BlockPos(placeTarget.add(0, 1, 0)));

            mc.player.inventory.currentItem = pumpkinSlot;
            placeBlock(new BlockPos(placeTarget.add(0, 2, 0)));
        }
        else if (placeMode.getValue().equals(PlaceMode.LOOK) && isAreaPlacableLook(mc.objectMouseOver.getBlockPos())) {
            mc.player.inventory.currentItem = snowSlot;

            Command.sendWarningMessage("Trying to place snow");
            placeBlock(new BlockPos(mc.objectMouseOver.getBlockPos().add(0, 0, 0)));
            placeBlock(new BlockPos(mc.objectMouseOver.getBlockPos().add(0, 1, 0)));
            Command.sendChatMessage("Placed snow");

            mc.player.inventory.currentItem = pumpkinSlot;

            Command.sendWarningMessage("Trying to place pumpkin");
            placeBlock(new BlockPos(mc.objectMouseOver.getBlockPos().add(0, 2, 0)));
            Command.sendChatMessage("Placed pumpkin");
        }

        if (isSneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            isSneaking = false;
        }
        mc.player.inventory.currentItem = swordSlot;

        if (fastMode.getValue()) {
            this.disable();
            return;
        }
    }

    private boolean isAreaPlaceable(BlockPos blockPos) {
        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(blockPos))) {
            if (entity instanceof EntityLivingBase) return false; } // entity on block
        if (!mc.world.getBlockState(blockPos).getMaterial().isReplaceable()) return false; // space for hopper
        if (!mc.world.getBlockState(blockPos.add(0, 1, 0)).getMaterial().isReplaceable()) return false; // space for shulker
        if (mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock() instanceof BlockAir) return false; // air below hopper
        if (mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock() instanceof BlockLiquid) return false; // liquid below
        if (mc.player.getPositionVector().distanceTo(new Vec3d(blockPos)) > placeRange.getValue()) return false; // out of range
        Block block = mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock();
        if (blackList.contains(block) || shulkerList.contains(block)) return false; // needs sneak
        return !(mc.player.getPositionVector().distanceTo(new Vec3d(blockPos).add(0, 1, 0)) > placeRange.getValue());
    }
    private boolean isAreaPlacableLook(BlockPos lookPos) {
        lookPos = mc.objectMouseOver.getBlockPos();
        Command.sendWarningMessage("Trying to place");
        if (lookPos == null) return false;
        Command.sendChatMessage("Placed");
//        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(lookPos))) {
//            if (entity instanceof EntityLivingBase) return false; }
        if (!mc.world.getBlockState(lookPos).getMaterial().isReplaceable()) return false;
        if (!mc.world.getBlockState(lookPos.add(0, 1, 0)).getMaterial().isReplaceable()) return false;
        if (!mc.world.getBlockState(lookPos.add(0, 2, 0)).getMaterial().isReplaceable()) return false;
        if (mc.world.getBlockState(lookPos.add(0, -1, 0)).getBlock() instanceof BlockAir) return false;
        if (mc.world.getBlockState(lookPos.add(0, -1, 0)).getBlock() instanceof BlockLiquid) return false;
        if (mc.player.getPositionVector().distanceTo(new Vec3d(lookPos)) > placeRange.getValue()) return false;
        Block block = mc.world.getBlockState(lookPos.add(0, -1, 0)).getBlock();
        if (blackList.contains(block) || shulkerList.contains(block)) return false;
        return !(mc.player.getPositionVector().distanceTo(new Vec3d(lookPos).add(0, 1, 0)) > placeRange.getValue());
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
}
