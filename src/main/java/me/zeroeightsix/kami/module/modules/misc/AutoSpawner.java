package me.zeroeightsix.kami.module.modules.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.combat.CrystalAura;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
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

import java.util.List;
import java.util.Random;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.util.BlockInteractionHelper.*;

/**
 * Created 26 November 2019 by hub
 * Updated 27 November 2019 by hub
 */
@Module.Info(name = "AutoSpawner", category = Module.Category.MISC, description = "Automatically spawns Withers, Iron Golems and Snowmen")
public class AutoSpawner extends Module {

    private Setting<UseMode> useMode = register(Settings.e("Use Mode", UseMode.SPAM));
    private Setting<Boolean> party = register(Settings.b("Party", false));
    private Setting<Boolean> partyWithers = register(Settings.booleanBuilder("Withers").withValue(false).withVisibility(v -> party.getValue()).build());
    private Setting<EntityMode> entityMode = register(Settings.enumBuilder(EntityMode.class).withName("Entity Mode").withValue(EntityMode.SNOW).withVisibility(v -> !party.getValue()).build());
    private Setting<Boolean> nametagWithers = register(Settings.booleanBuilder("Nametag").withValue(true).withVisibility(v -> party.getValue() && partyWithers.getValue() || !party.getValue() && entityMode.getValue().equals(EntityMode.WITHER)).build());
    private Setting<Float> placeRange = register(Settings.floatBuilder("Place Range").withMinimum(2.0f).withValue(3.5f).withMaximum(10.0f).build());
    private Setting<Integer> delay = register(Settings.integerBuilder("Delay").withMinimum(12).withValue(20).withMaximum(100).withVisibility(v -> useMode.getValue().equals(UseMode.SPAM)).build());
    private Setting<Boolean> rotate = register(Settings.b("Rotate", true));
    private Setting<Boolean> debug = register(Settings.b("Debug", false));

    private static boolean isSneaking;

    private BlockPos placeTarget;
    private boolean rotationPlaceableX;
    private boolean rotationPlaceableZ;

    private int bodySlot;
    private int headSlot;

    private int buildStage;
    private int delayStep;

    private static void placeBlock(BlockPos pos, boolean rotate) {
        EnumFacing side = getPlaceableSide(pos);

        if (side == null) return;

        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();

        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));

        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();

        if (!isSneaking && (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            isSneaking = true;
        }

        if (rotate) faceVectorPacketInstant(hitVec);

        mc.playerController.processRightClickBlock(mc.player, mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND);
        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.rightClickDelayTimer = 4;
    }

    private void useNameTag() {
        int originalSlot = mc.player.inventory.currentItem;
        for (Entity w : mc.world.getLoadedEntityList()) {
            if (w instanceof EntityWither && w.getDisplayName().getUnformattedText().equalsIgnoreCase("Wither")) {
                final EntityWither wither = (EntityWither) w;
                if (mc.player.getDistance(wither) <= placeRange.getValue()) {
                    if (debug.getValue()) Command.sendChatMessage("Found Unnamed Wither");
                    selectNameTags();
                    mc.playerController.interactWithEntity(mc.player, wither, EnumHand.MAIN_HAND);
                }
            }
        }
        mc.player.inventory.currentItem = originalSlot;
    }

    private void selectNameTags() {
        int tagSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY || stack.getItem() instanceof ItemBlock) continue;
            Item tag = stack.getItem();
            if (tag instanceof ItemNameTag) tagSlot = i;
        }

        if (tagSlot == -1) {
            if (debug.getValue()) Command.sendErrorMessage(getChatName() + "Error: No nametags in hotbar");
            return;
        }

        mc.player.inventory.currentItem = tagSlot;
    }

    private static EnumFacing getPlaceableSide(BlockPos pos) {
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbour = pos.offset(side);

            if (!mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)) {
                continue;
            }

            IBlockState blockState = mc.world.getBlockState(neighbour);
            if (!blockState.getMaterial().isReplaceable() && !(blockState.getBlock() instanceof BlockTallGrass) && !(blockState.getBlock() instanceof BlockDeadBush)) {
                return side;
            }

        }

        return null;
    }

    @Override
    protected void onEnable() {
        if (mc.player == null) { disable(); return; }

        buildStage = 1;
        delayStep = 1;
    }

    private boolean checkBlocksInHotbar() {
        headSlot = -1;
        bodySlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack == ItemStack.EMPTY) {
                continue;
            }

            if (entityMode.getValue().equals(EntityMode.WITHER)) {

                if (stack.getItem() == Items.SKULL && stack.getItemDamage() == 1) {
                    if (mc.player.inventory.getStackInSlot(i).stackSize >= 3) {
                        headSlot = i;
                    }
                    continue;
                }

                if (!(stack.getItem() instanceof ItemBlock)) continue;

                Block block = ((ItemBlock) stack.getItem()).getBlock();
                if (block instanceof BlockSoulSand) {
                    if (mc.player.inventory.getStackInSlot(i).stackSize >= 4) {
                        bodySlot = i;
                    }
                }
            }

            if (entityMode.getValue().equals(EntityMode.IRON)) {
                if (!(stack.getItem() instanceof ItemBlock)) continue;

                Block block = ((ItemBlock) stack.getItem()).getBlock();

                if (block == Blocks.LIT_PUMPKIN || block == Blocks.PUMPKIN) {
                    if (mc.player.inventory.getStackInSlot(i).stackSize >= 1) {
                        headSlot = i;
                    }
                }

                if (block == Blocks.IRON_BLOCK) {
                    if (mc.player.inventory.getStackInSlot(i).stackSize >= 4) {
                        bodySlot = i;
                    }
                }
            }

            if (entityMode.getValue().equals(EntityMode.SNOW)) {
                if (!(stack.getItem() instanceof ItemBlock)) continue;

                Block block = ((ItemBlock) stack.getItem()).getBlock();

                if (block == Blocks.LIT_PUMPKIN || block == Blocks.PUMPKIN) {
                    if (mc.player.inventory.getStackInSlot(i).stackSize >= 1) {
                        headSlot = i;
                    }
                }

                if (block == Blocks.SNOW) {
                    if (mc.player.inventory.getStackInSlot(i).stackSize >= 2) {
                        bodySlot = i;
                    }
                }

            }
        }

        return (bodySlot != -1 && headSlot != -1);
    }

    private boolean testStructure() {
        if (entityMode.getValue().equals(EntityMode.WITHER)) return testWitherStructure();

        if (entityMode.getValue().equals(EntityMode.IRON)) return testIronGolemStructure();

        if (entityMode.getValue().equals(EntityMode.SNOW)) return testSnowGolemStructure();

        return false;
    }

    private boolean testWitherStructure() {
        boolean noRotationPlaceable = true;
        rotationPlaceableX = true;
        rotationPlaceableZ = true;
        boolean isShitGrass = false;

        // IntelliJ dumb, this can cause NPE!
        //noinspection ConstantConditions
        if (mc.world.getBlockState(placeTarget) == null) return false;

        // dont place on grass
        Block block = mc.world.getBlockState(placeTarget).getBlock();
        if ((block instanceof BlockTallGrass) || (block instanceof BlockDeadBush)) isShitGrass = true;

        if (getPlaceableSide(placeTarget.up()) == null) return false;

        for (BlockPos pos : BodyParts.bodyBase) {
            if (placingIsBlocked(placeTarget.add(pos))) {
                noRotationPlaceable = false;
            }
        }

        for (BlockPos pos : BodyParts.ArmsX) {
            if (placingIsBlocked(placeTarget.add(pos)) || placingIsBlocked(placeTarget.add(pos.down()))) {
                rotationPlaceableX = false;
            }
        }

        for (BlockPos pos : BodyParts.ArmsZ) {
            if (placingIsBlocked(placeTarget.add(pos)) || placingIsBlocked(placeTarget.add(pos.down()))) {
                rotationPlaceableZ = false;
            }
        }

        for (BlockPos pos : BodyParts.headsX) {
            if (placingIsBlocked(placeTarget.add(pos))) {
                rotationPlaceableX = false;
            }
        }

        for (BlockPos pos : BodyParts.headsZ) {
            if (placingIsBlocked(placeTarget.add(pos))) {
                rotationPlaceableZ = false;
            }
        }

        return !isShitGrass && noRotationPlaceable && (rotationPlaceableX || rotationPlaceableZ);
    }

    private boolean testIronGolemStructure() {
        boolean noRotationPlaceable = true;
        rotationPlaceableX = true;
        rotationPlaceableZ = true;
        boolean isShitGrass = false;

        // IntelliJ dumb, this can cause NPE!
        //noinspection ConstantConditions
        if (mc.world.getBlockState(placeTarget) == null) return false;

        // dont place on grass
        Block block = mc.world.getBlockState(placeTarget).getBlock();
        if ((block instanceof BlockTallGrass) || (block instanceof BlockDeadBush)) isShitGrass = true;

        if (getPlaceableSide(placeTarget.up()) == null) return false;

        for (BlockPos pos : BodyParts.bodyBase) {
            if (placingIsBlocked(placeTarget.add(pos))) {
                noRotationPlaceable = false;
            }
        }

        for (BlockPos pos : BodyParts.ArmsX) {
            if (placingIsBlocked(placeTarget.add(pos)) || placingIsBlocked(placeTarget.add(pos.down()))) {
                rotationPlaceableX = false;
            }
        }

        for (BlockPos pos : BodyParts.ArmsZ) {
            if (placingIsBlocked(placeTarget.add(pos)) || placingIsBlocked(placeTarget.add(pos.down()))) {
                rotationPlaceableZ = false;
            }
        }

        for (BlockPos pos : BodyParts.head) {
            if (placingIsBlocked(placeTarget.add(pos))) {
                noRotationPlaceable = false;
            }
        }

        return !isShitGrass && noRotationPlaceable && (rotationPlaceableX || rotationPlaceableZ);

    }

    private boolean testSnowGolemStructure() {

        boolean noRotationPlaceable = true;
        boolean isShitGrass = false;

        // IntelliJ dumb, this can cause NPE!
        //noinspection ConstantConditions
        if (mc.world.getBlockState(placeTarget) == null) {
            return false;
        }

        // dont place on grass
        Block block = mc.world.getBlockState(placeTarget).getBlock();
        if ((block instanceof BlockTallGrass) || (block instanceof BlockDeadBush)) {
            isShitGrass = true;
        }

        if (getPlaceableSide(placeTarget.up()) == null) {
            return false;
        }

        for (BlockPos pos : BodyParts.bodyBase) {
            if (placingIsBlocked(placeTarget.add(pos))) {
                noRotationPlaceable = false;
            }
        }

        for (BlockPos pos : BodyParts.head) {
            if (placingIsBlocked(placeTarget.add(pos))) {
                noRotationPlaceable = false;
            }
        }

        return !isShitGrass && noRotationPlaceable;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        if (nametagWithers.getValue() && (party.getValue() && partyWithers.getValue() || !party.getValue() && entityMode.getValue().equals(EntityMode.WITHER))) useNameTag();

        if (buildStage == 1) {
            isSneaking = false;
            rotationPlaceableX = false;
            rotationPlaceableZ = false;

            if (party.getValue()) {
                Random random = new Random();
                int partyMode;

                if (partyWithers.getValue()) {
                    partyMode = random.nextInt(3);
                } else {
                    partyMode = random.nextInt(2);
                }

                if (partyMode == 0) {
                    entityMode.setValue(EntityMode.SNOW);
                } else if (partyMode == 1) {
                    entityMode.setValue(EntityMode.IRON);
                } else if (partyMode == 2) {
                    entityMode.setValue(EntityMode.WITHER);
                }
            }

            if (!checkBlocksInHotbar()) {
                if (!party.getValue()) {
                    if (debug.getValue()) {
                        Command.sendChatMessage(getChatName() + ChatFormatting.RED.toString() + "Blocks missing for: " + ChatFormatting.RESET.toString() + entityMode.getValue().toString() + ChatFormatting.RED.toString() + ", disabling.");
                    }
                    disable();
                }
                return;
            }

            CrystalAura crystalAura = (CrystalAura) MODULE_MANAGER.getModule(CrystalAura.class);
            List<BlockPos> blockPosList = crystalAura.getSphere(mc.player.getPosition().down(), placeRange.getValue(), placeRange.getValue().intValue(), false, true, 0);

            boolean noPositionInArea = true;

            for (BlockPos pos : blockPosList) {
                placeTarget = pos.down();
                if (testStructure()) {
                    noPositionInArea = false;
                    break;
                }
            }

            if (noPositionInArea) {
                if (useMode.getValue().equals(UseMode.SINGLE)) {
                    if (debug.getValue()) {
                        Command.sendChatMessage(getChatName() + ChatFormatting.RED.toString() + "Position not valid, disabling.");
                    }
                    disable();
                }
                return;
            }

            mc.player.inventory.currentItem = bodySlot;

            for (BlockPos pos : BodyParts.bodyBase) placeBlock(placeTarget.add(pos), rotate.getValue());

            if (entityMode.getValue().equals(EntityMode.WITHER) || entityMode.getValue().equals(EntityMode.IRON)) {
                if (rotationPlaceableX) {
                    for (BlockPos pos : BodyParts.ArmsX) {
                        placeBlock(placeTarget.add(pos), rotate.getValue());
                    }
                } else if (rotationPlaceableZ) {
                    for (BlockPos pos : BodyParts.ArmsZ) {
                        placeBlock(placeTarget.add(pos), rotate.getValue());
                    }
                }
            }

            buildStage = 2;

        } else if (buildStage == 2) {
            mc.player.inventory.currentItem = headSlot;

            if (entityMode.getValue().equals(EntityMode.WITHER)) {
                if (rotationPlaceableX) {
                    for (BlockPos pos : BodyParts.headsX) {
                        placeBlock(placeTarget.add(pos), rotate.getValue());
                    }
                } else if (rotationPlaceableZ) {
                    for (BlockPos pos : BodyParts.headsZ) {
                        placeBlock(placeTarget.add(pos), rotate.getValue());
                    }
                }
            }

            if (entityMode.getValue().equals(EntityMode.IRON) || entityMode.getValue().equals(EntityMode.SNOW)) {
                for (BlockPos pos : BodyParts.head) placeBlock(placeTarget.add(pos), rotate.getValue());
            }

            if (isSneaking) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                isSneaking = false;
            }

            if (useMode.getValue().equals(UseMode.SINGLE)) disable();

            buildStage = 3;
        } else if (buildStage == 3) {
            if (delayStep < delay.getValue()) {
                delayStep++;
            } else {
                delayStep = 1;
                buildStage = 1;
            }
        }
    }

    private boolean placingIsBlocked(BlockPos pos) {
        // check if block is already placed
        Block block = mc.world.getBlockState(pos).getBlock();
        if (!(block instanceof BlockAir)) return true;

        // check if entity blocks placing
        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
            if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getHudInfo() {
        if (party.getValue()) {
            if (partyWithers.getValue()) {
                return "PARTY WITHER";
            }
            return "PARTY";
        } else {
            return entityMode.getValue().toString();
        }
    }

    private enum UseMode {
        SINGLE, SPAM
    }

    private enum EntityMode {
        SNOW, IRON, WITHER
    }

    private static class BodyParts {
        private static final BlockPos[] bodyBase = {
                new BlockPos(0, 1, 0),
                new BlockPos(0, 2, 0),
        };

        private static final BlockPos[] ArmsX = {
                new BlockPos(-1, 2, 0),
                new BlockPos(1, 2, 0)
        };

        private static final BlockPos[] ArmsZ = {
                new BlockPos(0, 2, -1),
                new BlockPos(0, 2, 1)
        };

        private static final BlockPos[] headsX = {
                new BlockPos(0, 3, 0),
                new BlockPos(-1, 3, 0),
                new BlockPos(1, 3, 0)
        };

        private static final BlockPos[] headsZ = {
                new BlockPos(0, 3, 0),
                new BlockPos(0, 3, -1),
                new BlockPos(0, 3, 1)
        };

        private static final BlockPos[] head = {
                new BlockPos(0, 3, 0)
        };
    }
}
