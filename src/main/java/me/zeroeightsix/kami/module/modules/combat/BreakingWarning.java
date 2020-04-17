package me.zeroeightsix.kami.module.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.util.math.BlockPos;

import static me.zeroeightsix.kami.gui.kami.DisplayGuiScreen.getScale;

/**
 * @author Antonio32A
 * Updated by S-B99 on 31/03/20
 *
 * Antonio32A created the pastDistance method, used by ForgeHax here:
 * https://github.com/fr1kin/ForgeHax/blob/2011740/src/main/java/com/matt/forgehax/mods/CoordsFinder.java#L126
 */
@Module.Info(name = "BreakingWarning", category = Module.Category.COMBAT, description = "Notifies you when someone is breaking a block near you.")
public class BreakingWarning extends Module {
    private Setting<Double> minRange = register(Settings.doubleBuilder("Min Range").withMinimum(0.0).withValue(1.5).withMaximum(10.0).build());
    private Setting<Boolean> obsidianOnly = register(Settings.b("Obsidian Only", true));
    private Setting<Boolean> pickaxeOnly = register(Settings.b("Pickaxe Only", true));

    private Boolean warn = false;
    private String playerName;
    private int delay;

    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (event.getPacket() instanceof SPacketBlockBreakAnim) {
            SPacketBlockBreakAnim packet = (SPacketBlockBreakAnim) event.getPacket();

            int progress = packet.getProgress();
            int breakerId = packet.getBreakerId();

            BlockPos pos = packet.getPosition();
            Block block = mc.world.getBlockState(pos).getBlock();
            EntityPlayer breaker = (EntityPlayer) mc.world.getEntityByID(breakerId);

            if (breaker == null) return;

            if (obsidianOnly.getValue() && !block.equals(Blocks.OBSIDIAN)) return;

            if (pickaxeOnly.getValue()) {
                if (breaker.itemStackMainHand.isEmpty() || !(breaker.itemStackMainHand.getItem() instanceof ItemPickaxe)) return;
            }

            if (pastDistance(mc.player, pos, minRange.getValue())) {
                playerName = breaker.getName();

                warn = true;
                delay = 0;
                if (progress == 255) warn = false;
            }
        }
    });

    @Override
    public void onRender() {
        if (!warn) return;
        if (delay++ > 100) warn = false;

        String text = playerName + " is breaking blocks near you!";
        FontRenderer renderer = Wrapper.getFontRenderer();

        int divider = getScale();
        renderer.drawStringWithShadow(mc.displayWidth / divider / 2 - renderer.getStringWidth(text) / 2, mc.displayHeight / divider / 2 - 16, 240, 87, 70, text);
    }

    private boolean pastDistance(EntityPlayer player, BlockPos pos, double dist) {
        return player.getDistanceSqToCenter(pos) <= Math.pow(dist, 2);
    }
}
