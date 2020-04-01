package me.zeroeightsix.kami.module.modules.experimental;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.combat.CrystalAura;
import me.zeroeightsix.kami.module.modules.player.NoBreakAnimation;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.BlockInteractionHelper;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.module.modules.combat.CrystalAura.getPlayerPos;
import static me.zeroeightsix.kami.util.EntityUtil.calculateLookAt;

/**
 * @author hub
 * @author polymer
 * Created by polymer on 12/03/20
 */

@Module.Info(name = "HoleFiller", category = Module.Category.EXPERIMENTAL, description="Fills holes around the player to make people easier to crystal.")
public class HoleFiller extends Module {
    private Setting<Double> distance = register(Settings.d("Range", 4.0));
    private Setting<Boolean> render = register(Settings.b("Render Filled Blocks", false));
    private Setting<Boolean> holeCheck = register(Settings.b("Only Fill in Hole", true));
    private Setting<Boolean> ignoreWalls = register(Settings.b("Ignore Walls", false));

    public List<BlockPos> blockPosList;
    List<Entity> entities = new ArrayList<>();

    public boolean isHole;
    static boolean isSpoofingAngles;
    static float yaw;
    static float pitch;

    private final BlockPos[] surroundOffset = {
        new BlockPos(0, -1, 0), // down
        new BlockPos(0, 0, -1), // north
        new BlockPos(1, 0, 0), // east
        new BlockPos(0, 0, 1), // south
        new BlockPos(-1, 0, 0) // west
    };

    private int findObiInHotbar() {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = Wrapper.getPlayer().inventory.getStackInSlot(i);
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

    @Override
    public void onUpdate() {
        /* mc.player can only be null if the world is null, so checking if the mc.player is null *should be sufficient */
        if (mc.player == null || mc.world == null) return;

        Vec3d[] holeOffset = {
            mc.player.getPositionVector().add(1, 0, 0),
            mc.player.getPositionVector().add(-1, 0, 0),
            mc.player.getPositionVector().add(0, 0, 1),
            mc.player.getPositionVector().add(0, 0, -1),
            mc.player.getPositionVector().add(0, -1, 0)
        };


        entities.addAll(mc.world.playerEntities.stream().filter(entityPlayer -> !Friends.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
        int range = (int) Math.ceil(distance.getValue());
        CrystalAura ca = (CrystalAura) MODULE_MANAGER.getModule(CrystalAura.class);
        blockPosList = ca.getSphere(getPlayerPos(), range, range, false, true, 0);
        for (Entity p : entities) {
            List<BlockPos> maybe = ca.getSphere(p.getPosition(), range, range, false, true, 0);
            for (BlockPos pos : maybe) {
                if (ignoreWalls.getValue()) {
                    blockPosList.add(pos);
                } else if (mc.world.rayTraceBlocks(new Vec3d(mc.player.getPosition().x, mc.player.getPosition().y + p.getEyeHeight(), mc.player.getPosition().z), new Vec3d(pos.x, pos.y + (double)p.getEyeHeight(), pos.z), false, true, false) != null) {
                    blockPosList.add(pos);
                }
            }
        }

        if (blockPosList == null) return;
        for (BlockPos p: blockPosList) {
            if (p == null) return;

            isHole = true;
            // block gotta be air
            if (!mc.world.getBlockState(p).getBlock().equals(Blocks.AIR)) continue;

            // block 1 above gotta be air
            if (!mc.world.getBlockState(p.add(0, 1, 0)).getBlock().equals(Blocks.AIR)) continue;

            // block 2 above gotta be air
            if (!mc.world.getBlockState(p.add(0, 2, 0)).getBlock().equals(Blocks.AIR)) continue;

            for (BlockPos o : surroundOffset) {
                Block block = mc.world.getBlockState(p.add(o)).getBlock();
                if (block != Blocks.BEDROCK && block != Blocks.OBSIDIAN && block != Blocks.ENDER_CHEST && block != Blocks.ANVIL) {
                    isHole = false;
                    break;
                }
            }

            int h = 0;
            if (holeCheck.getValue()) {
                for (Vec3d o: holeOffset) {
                    BlockPos q = new BlockPos (o.x, o.y, o.z);
                    Block b = mc.world.getBlockState(q).getBlock();
                    if (b == Blocks.OBSIDIAN || b == Blocks.BEDROCK) {
                        h++;
                    }
                }
                if (h != 5) return;
            }

            if (isHole) {
                if (mc.player.getPositionVector().squareDistanceTo(p.x, p.y, p.z) <= 1.2) {
                    return;
                }
                for (Entity e : entities) {
                    if (e.getPositionVector().squareDistanceTo(p.x, p.y, p.z) <= 1.2) {
                        return;
                    }
                }
                int oldSlot = mc.player.inventory.currentItem;
                int obiSlot = -1;
                obiSlot = findObiInHotbar();
                if (obiSlot == -1) {
                    Command.sendChatMessage("&cError: &rNo obsidian in hotbar! disabling.");
                    this.disable();
                }
                mc.player.connection.sendPacket(new CPacketHeldItemChange(obiSlot));
                lookAtPacket(p.x, p.y, p.z, mc.player);
                BlockInteractionHelper.placeBlockScaffold(p);
                if (MODULE_MANAGER.isModuleEnabled(NoBreakAnimation.class)) {
                    ((NoBreakAnimation) MODULE_MANAGER.getModule(NoBreakAnimation.class)).resetMining();
                }
                resetRotation();
                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
            }
        }
    }
    private void lookAtPacket(double px, double py, double pz, EntityPlayer me) {
        double[] v = calculateLookAt(px, py, pz, me);
        setYawAndPitch((float) v[0], (float) v[1]+1f);
    }
    private static void setYawAndPitch(float yaw1, float pitch1) {
        yaw = yaw1;
        pitch = pitch1;
        isSpoofingAngles = true;
    }

    private static void resetRotation() {
        if (isSpoofingAngles) {
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;
            isSpoofingAngles = false;
        }
    }

    @EventHandler
    private Listener<PacketEvent.Send> cPacketListener = new Listener<>(event -> {
        Packet packet = event.getPacket();
        if (packet instanceof CPacketPlayer) {
            if (isSpoofingAngles) {
                ((CPacketPlayer) packet).yaw = (float) yaw;
                ((CPacketPlayer) packet).pitch = (float) pitch;
            }
        }
    });
}
