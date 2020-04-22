package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

/**
 * Created by 086 on 19/11/2017.
 * Updated by dominikaaaa on 05/03/20
 */
@Module.Info(
        category = Module.Category.PLAYER,
        description = "Prevents fall damage", name = "NoFall"
)
public class NoFall extends Module {

    private Setting<FallMode> fallMode = register(Settings.e("Mode", FallMode.PACKET));
    private Setting<Boolean> pickup = register(Settings.booleanBuilder("Pickup").withValue(true).withVisibility(v -> fallMode.getValue().equals(FallMode.BUCKET)).build());
    private Setting<Integer> distance = register(Settings.integerBuilder("Distance").withValue(3).withMinimum(1).withMaximum(10).withVisibility(v -> fallMode.getValue().equals(FallMode.BUCKET)).build());
    private Setting<Integer> pickupDelay = register(Settings.integerBuilder("Pickup Delay").withValue(300).withMinimum(100).withMaximum(1000).withVisibility(v -> fallMode.getValue().equals(FallMode.BUCKET) && pickup.getValue()).build());

    private long last = 0;

    @EventHandler
    public Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if ((fallMode.getValue().equals(FallMode.PACKET)) && event.getPacket() instanceof CPacketPlayer) {
            ((CPacketPlayer) event.getPacket()).onGround = true;
        }
    });

    @Override
    public void onUpdate() {
        if ((fallMode.getValue().equals(FallMode.BUCKET)) && mc.player.fallDistance >= distance.getValue() && !EntityUtil.isAboveWater(mc.player) && System.currentTimeMillis() - last > 100) {
            Vec3d posVec = mc.player.getPositionVector();
            RayTraceResult result = mc.world.rayTraceBlocks(posVec, posVec.add(0, -5.33f, 0), true, true, false);
            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                EnumHand hand = EnumHand.MAIN_HAND;
                if (mc.player.getHeldItemOffhand().getItem() == Items.WATER_BUCKET) hand = EnumHand.OFF_HAND;
                else if (mc.player.getHeldItemMainhand().getItem() != Items.WATER_BUCKET) {
                    for (int i = 0; i < 9; i++)
                        if (mc.player.inventory.getStackInSlot(i).getItem() == Items.WATER_BUCKET) {
                            mc.player.inventory.currentItem = i;
                            mc.player.rotationPitch = 90;
                            last = System.currentTimeMillis();
                            return;
                        }
                    return;
                }
                mc.player.rotationPitch = 90;
                mc.playerController.processRightClick(mc.player, mc.world, hand);
            }
            if (pickup.getValue()) {
                new Thread(() -> {
                    try { Thread.sleep(pickupDelay.getValue()); } catch (InterruptedException ignored) { }
                    mc.player.rotationPitch = 90;
                    mc.rightClickMouse();
                }).start();
            }
        }
    }

    private enum FallMode {
        BUCKET, PACKET
    }
}
