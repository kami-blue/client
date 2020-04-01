package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFalling;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.InputUpdateEvent;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.util.BlockInteractionHelper.*;

/**
 * Created by 086 on 20/01/19
 * Updated by Polymer on 16/01/20
 * Updated by S-B99 on 02/03/20
 * @see me.zeroeightsix.kami.mixin.client.MixinEntity
 */
@Module.Info(name = "Scaffold", category = Module.Category.PLAYER, description = "Places blocks under you")
public class Scaffold extends Module {

    private Setting<Boolean> placeBlocks = register(Settings.b("Place Blocks", true));
    private Setting<Mode> modeSetting = register(Settings.enumBuilder(Mode.class).withName("Mode").withValue(Mode.LEGIT).build());
    private Setting<Boolean> randomDelay = register(Settings.booleanBuilder("Random Delay").withValue(false).withVisibility(v -> modeSetting.getValue().equals(Mode.LEGIT)).build());
    private Setting<Integer> delayRange = register(Settings.integerBuilder("Delay Range").withMinimum(0).withValue(6).withMaximum(10).withVisibility(v -> modeSetting.getValue().equals(Mode.LEGIT) && randomDelay.getValue()).build());
    private Setting<Integer> ticks = register(Settings.integerBuilder("Ticks").withMinimum(0).withMaximum(60).withValue(2).withVisibility(v -> !modeSetting.getValue().equals(Mode.LEGIT)).build());

    private boolean shouldSlow = false;

    private static Scaffold INSTANCE;

    public Scaffold() {
        INSTANCE = this;
    }

    public static boolean shouldScaffold() {
        return INSTANCE.isEnabled();
    }

    private enum Mode {
        NEITHER, LEGIT
    }

    @EventHandler
    private Listener<InputUpdateEvent> eventListener = new Listener<>(event -> {
        if (modeSetting.getValue().equals(Mode.LEGIT) && shouldSlow) {
            if (randomDelay.getValue()) {
                event.getMovementInput().moveStrafe *= 0.2f + getRandomInRange();
                event.getMovementInput().moveForward *= 0.2f + getRandomInRange();
            }
            else {
                event.getMovementInput().moveStrafe *= 0.2f;
                event.getMovementInput().moveForward *= 0.2f;
            }
        }
    });

    @Override
    public void onUpdate() {
        if (mc.player == null || MODULE_MANAGER.isModuleEnabled(Freecam.class)) return;
        shouldSlow = false;

        Vec3d vec3d = EntityUtil.getInterpolatedPos(mc.player, ticks.getValue());
        if (modeSetting.getValue().equals(Mode.LEGIT)) vec3d = EntityUtil.getInterpolatedPos(mc.player, 0);

        BlockPos blockPos = new BlockPos(vec3d).down();
        BlockPos belowBlockPos = blockPos.down();
        BlockPos legitPos = new BlockPos(EntityUtil.getInterpolatedPos(mc.player, 2));

        /* when legitBridge is enabled */
        /* check if block behind player is air or other replaceable block and if it is, make the player crouch */
        if (modeSetting.getValue().equals(Mode.LEGIT) && Wrapper.getWorld().getBlockState(legitPos.down()).getMaterial().isReplaceable() && mc.player.onGround) {
            shouldSlow = true;
            mc.player.movementInput.sneak = true;
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
        }

        /* check if block is already placed */
        if (!Wrapper.getWorld().getBlockState(blockPos).getMaterial().isReplaceable()) {
            return;
        }

        setSlotToBlocks(belowBlockPos);

        /* check if we don't have a block adjacent to the blockPos */
        if (!checkForNeighbours(blockPos)) return;

        /* place the block */
        if (placeBlocks.getValue()) placeBlockScaffold(blockPos);
        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
        shouldSlow = false;
    }

    private float getRandomInRange() {
        return 0.11f + (float) Math.random() * ((delayRange.getValue() / 10.0f) - 0.11f);
    }

    private void setSlotToBlocks(BlockPos belowBlockPos) {
        /* search blocks in hotbar */
        int newSlot = -1;
        for (int i = 0; i < 9; i++) {
            /* filter out non-block items */
            ItemStack stack = Wrapper.getPlayer().inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) continue;

            Block block = ((ItemBlock) stack.getItem()).getBlock();
            if (blackList.contains(block) || block instanceof BlockContainer) continue;

            /* filter out non-solid blocks */
            if (!Block.getBlockFromItem(stack.getItem()).getDefaultState().isFullBlock()) continue;

            /* don't use falling blocks if it'd fall */
            if (((ItemBlock) stack.getItem()).getBlock() instanceof BlockFalling) {
                if (Wrapper.getWorld().getBlockState(belowBlockPos).getMaterial().isReplaceable()) continue;
            }
            newSlot = i;
            break;
        }
        /* check if any blocks were found, and if they were then set the slot */
        int oldSlot = 1; /* make it 1, instead of -1 so you don't get kicked if it was -1 */
        if (newSlot != -1) {
            oldSlot = Wrapper.getPlayer().inventory.currentItem;
            Wrapper.getPlayer().inventory.currentItem = newSlot;
        }
        /* reset slot back to the original one */
        Wrapper.getPlayer().inventory.currentItem = oldSlot;
    }
}

