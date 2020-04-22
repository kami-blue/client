package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.event.events.PlayerMoveEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;

/**
 * Created by 086 on 22/12/2017.
 */
@Module.Info(
        name = "Freecam",
        category = Module.Category.PLAYER,
        description = "Leave your body and trascend into the realm of the gods"
)
public class Freecam extends Module {

    private Setting<Integer> speed = register(Settings.i("Speed", 5)); // /100 in practice
    private Setting<Boolean> packetCancel = register(Settings.b("Packet Cancel", false));
    private double posX, posY, posZ;
    private float pitch, yaw;

    private EntityOtherPlayerMP clonedPlayer;

    private boolean isRidingEntity;
    private Entity ridingEntity;

    @Override
    protected void onEnable() {
        if (mc.player != null) {
            isRidingEntity = mc.player.getRidingEntity() != null;

            if (mc.player.getRidingEntity() == null) {
                posX = mc.player.posX;
                posY = mc.player.posY;
                posZ = mc.player.posZ;
            } else {
                ridingEntity = mc.player.getRidingEntity();
                mc.player.dismountRidingEntity();
            }

            pitch = mc.player.rotationPitch;
            yaw = mc.player.rotationYaw;

            clonedPlayer = new EntityOtherPlayerMP(mc.world, mc.getSession().getProfile());
            clonedPlayer.copyLocationAndAnglesFrom(mc.player);
            clonedPlayer.rotationYawHead = mc.player.rotationYawHead;
            mc.world.addEntityToWorld(-100, clonedPlayer);
            mc.player.capabilities.isFlying = true;
            mc.player.capabilities.setFlySpeed(speed.getValue() / 100f);
            mc.player.noClip = true;
            // WebringOfTheDamned
            // This is needed for some reason, as is the converse in onDisable.
            mc.renderChunksMany = false;
            mc.renderGlobal.loadRenderers();
        }
    }

    @Override
    protected void onDisable() {
        EntityPlayer localPlayer = mc.player;
        if (localPlayer != null) {
            mc.player.setPositionAndRotation(posX, posY, posZ, yaw, pitch);
            mc.world.removeEntityFromWorld(-100);
            clonedPlayer = null;
            posX = posY = posZ = 0.D;
            pitch = yaw = 0.f;
            mc.player.capabilities.isFlying = false; //getModManager().getMod("ElytraFlight").isEnabled();
            mc.player.capabilities.setFlySpeed(0.05f);
            mc.player.noClip = false;
            mc.player.motionX = mc.player.motionY = mc.player.motionZ = 0.f;

            if (isRidingEntity) {
                mc.player.startRiding(ridingEntity, true);
            }
            // WebringOfTheDamned
            // This is needed for some reason, as is the converse in onEnable.
            mc.renderChunksMany = true;
            mc.renderGlobal.loadRenderers();
        }
    }

    @Override
    public void onUpdate() {
        mc.player.capabilities.isFlying = true;
        mc.player.capabilities.setFlySpeed(speed.getValue() / 100f);
        mc.player.noClip = true;
        mc.player.onGround = false;
        mc.player.fallDistance = 0;
    }

    @EventHandler
    private Listener<PlayerMoveEvent> moveListener = new Listener<>(event -> mc.player.noClip = true);

    @EventHandler
    private Listener<PlayerSPPushOutOfBlocksEvent> pushListener = new Listener<>(event -> event.setCanceled(true));

    @EventHandler
    private Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketPlayer || event.getPacket() instanceof CPacketInput) {
            event.cancel();
        }
        if (packetCancel.getValue() && (event.getPacket() instanceof CPacketUseEntity || event.getPacket() instanceof CPacketVehicleMove)) {
            event.cancel();
        }

    });

}
