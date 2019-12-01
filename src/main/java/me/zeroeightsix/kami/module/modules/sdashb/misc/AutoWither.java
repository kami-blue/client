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
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemNameTag;
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
 * Updated by cats on 1/12/19
 */
@Module.Info(name = "AutoWither", category = Module.Category.MISC, description = "Automatically creates withers")
public class AutoWither extends Module {

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

    private Setting<Double> placeRange = register(Settings.doubleBuilder("Place range").withMinimum(1.0).withValue(4.0).withMaximum(10.0).build());
    private Setting<Boolean> placeCloseToEnemy = register(Settings.b("Place close to enemy", false));
    private Setting<Boolean> fastMode = register(Settings.b("Disable after placing", true));
    private Setting<Boolean> debugMessages = register(Settings.b("Debug Messages", true));
    private Setting<Boolean> nametag = register(Settings.b("Name Wither", false));

    private int swordSlot;
    private static boolean isSneaking;

    @Override
    protected void onEnable() {

        Command.sendChatMessage("[AutoWither] Please make sure the wither skulls are in slot 2");
        Command.sendChatMessage("[AutoWither] This will be fixed soon");

        if (isDisabled() || mc.player == null || ModuleManager.isModuleEnabled("Freecam")) {
            this.disable();
            return;
        }

        df.setRoundingMode(RoundingMode.CEILING);

        int skullSlot = 1;
        int soulsandSlot = -1;
        swordSlot = mc.player.inventory.currentItem;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }

            Block block = ((ItemBlock) stack.getItem()).getBlock();

            if (block == Blocks.SOUL_SAND) {
                soulsandSlot = i;
            }
//            else if (block == Blocks.SKULL) {
//                skullSlot = i;
//            }

        }

//        if (skullSlot == -1) {
//            if (debugMessages.getValue()) {
//                Command.sendChatMessage("[AutoWither] Wither Skull missing, disabling.");
//            }
//            this.disable();
//            return;
//        }

        if (soulsandSlot == -1) {
            if (debugMessages.getValue()) {
                Command.sendChatMessage("[AutoWither] Soul Sand missing, disabling.");
            }
            this.disable();
            return;
        }

        int range = (int) Math.ceil(placeRange.getValue());

        CrystalAura crystalAura = (CrystalAura) ModuleManager.getModuleByName("CrystalAura");
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
                if (debugMessages.getValue()) {
                    Command.sendChatMessage("[AutoWither] Placing close to Enemy");
                }
                // Get Key with lowest Value (closest to enemies)
                placeTarget = Collections.min(placeTargetMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            } else {
                if (debugMessages.getValue()) {
                    Command.sendChatMessage("[AutoWither] Placing far from Enemy");
                }
                // Get Key with highest Value (furthest away from enemies)
                placeTarget = Collections.max(placeTargetMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            }

        } else {

            if (debugMessages.getValue()) {
                Command.sendChatMessage("[AutoWither] No enemy nearby, placing at first valid position.");
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
                Command.sendChatMessage("[AutoWither] No valid position in range to place!");
            }
            this.disable();
            return;
        }

        if (debugMessages.getValue()) {
            Command.sendChatMessage("[AutoWither] Place Target: " + placeTarget.x + " " + placeTarget.y + " " + placeTarget.z + " Distance: " + df.format(mc.player.getPositionVector().distanceTo(new Vec3d(placeTarget))));
        }

        if (soulsandSlot == -1 && skullSlot == -1) {
            Command.sendChatMessage("[AutoWither] Error: No required blocks in inventory");
        }
        else if (soulsandSlot != -1) {
            mc.player.inventory.currentItem = soulsandSlot;
            placeBlock(new BlockPos(placeTarget.add(0, 0, 0)));
            placeBlock(new BlockPos(placeTarget.add(0, 1, 0)));
            placeBlock(new BlockPos(placeTarget.add(-1, 1, 0)));
            placeBlock(new BlockPos(placeTarget.add(1, 1, 0)));
        }
        else {
            Command.sendChatMessage("[AutoWither] Error: No Soul Sand found");
        }

        if (skullSlot != -1) {
            mc.player.inventory.currentItem = skullSlot;
            placeBlock(new BlockPos(placeTarget.add(0, 2, 0)));
            placeBlock(new BlockPos(placeTarget.add(-1, 2, 0)));
            placeBlock(new BlockPos(placeTarget.add(1, 2, 0)));
        }
        else {
            Command.sendChatMessage("[AutoWither] Error: No Soul Sand found");
        }

        if (isSneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            isSneaking = false;
        }
        mc.player.inventory.currentItem = swordSlot;

        if (nametag.getValue()) {
            return;
        } else if (fastMode.getValue()) {
            this.disable();
            return;
        }
    }

    @Override
    public void onUpdate() {
        if (nametag.getValue()) {
            int tagslot = -1;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);

                if (stack == ItemStack.EMPTY || stack.getItem() instanceof ItemBlock) {
                    continue;
                }

                Item tag = stack.getItem();

                if (tag instanceof ItemNameTag) {
                    tagslot = i;
                }
            }
            if (tagslot == -1 && fastMode.getValue()) {
                Command.sendChatMessage("[AutoWither] Error: No nametags in inventory, disabling module");
                this.disable();
                return;
            }
            for (Entity w : mc.world.getLoadedEntityList()) {
                if (w instanceof EntityWither) {
                    final EntityWither wither = (EntityWither) w;
                    if (mc.player.getDistance(wither) <= placeRange.getValue()) {
                        if (debugMessages.getValue()) {Command.sendChatMessage("Registered Wither");}
                        if (tagslot != -1) {
                            mc.player.inventory.currentItem = tagslot;
                            mc.playerController.interactWithEntity(mc.player, wither, EnumHand.MAIN_HAND);
                            if (fastMode.getValue()) {
                                this.disable();
                            }
                        }
                    }
                }
            }
            mc.player.inventory.currentItem = swordSlot;
        }

        if (isDisabled() || mc.player == null || ModuleManager.isModuleEnabled("Freecam")) {
            return;
        }

        if (!(mc.currentScreen instanceof GuiContainer)) {
            return;
        }

        if (swordSlot == -1) {
            return;
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
}
