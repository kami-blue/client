package me.zeroeightsix.kami.module.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.event.events.PlayerTravelEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendErrorMessage;

/**
 * Created by 086 on 11/04/2018.
 * Updated by Itistheend on 28/12/19.
 * Updated by S-B99 on 15/04/20
 */
@Module.Info(name = "ElytraFlight", description = "Modifies elytras to fly at custom velocities and fall speeds", category = Module.Category.MOVEMENT)
public class ElytraFlight extends Module {
    private Setting<ElytraFlightMode> mode = register(Settings.e("Mode", ElytraFlightMode.HIGHWAY));
    private Setting<Boolean> defaultSetting = register(Settings.b("Defaults", false));
    private Setting<Boolean> easyTakeOff = register(Settings.booleanBuilder("Easy Takeoff H").withValue(true).withVisibility(v -> mode.getValue().equals(ElytraFlightMode.HIGHWAY)).build());
    private Setting<Boolean> hoverControl = register(Settings.booleanBuilder("Hover").withValue(true).withVisibility(v -> mode.getValue().equals(ElytraFlightMode.CONTROL)).build());
    private Setting<Boolean> easyTakeOffControl = register(Settings.booleanBuilder("Easy Takeoff C").withValue(false).withVisibility(v -> mode.getValue().equals(ElytraFlightMode.CONTROL)).build());
    private Setting<Boolean> timerControl = register(Settings.booleanBuilder("Takeoff Timer").withValue(false).withVisibility(v -> easyTakeOffControl.getValue() && mode.getValue().equals(ElytraFlightMode.CONTROL)).build());
    private Setting<TakeoffMode> takeOffMode = register(Settings.enumBuilder(TakeoffMode.class).withName("Takeoff Mode").withValue(TakeoffMode.PACKET).withVisibility(v -> easyTakeOff.getValue() && mode.getValue().equals(ElytraFlightMode.HIGHWAY)).build());
    private Setting<Boolean> overrideMaxSpeed = register(Settings.booleanBuilder("Over Max Speed").withValue(false).withVisibility(v -> mode.getValue().equals(ElytraFlightMode.HIGHWAY)).build());
    private Setting<Float> speedHighway = register(Settings.floatBuilder("Speed H").withValue(1.8f).withMaximum(1.8f).withVisibility(v -> !overrideMaxSpeed.getValue() && mode.getValue().equals(ElytraFlightMode.HIGHWAY)).build());
    private Setting<Float> speedHighwayOverride = register(Settings.floatBuilder("Speed H O").withValue(1.8f).withVisibility(v -> overrideMaxSpeed.getValue() && mode.getValue().equals(ElytraFlightMode.HIGHWAY)).build());
    private Setting<Float> speedControl = register(Settings.floatBuilder("Speed C").withValue(1.8f).withVisibility(v -> mode.getValue().equals(ElytraFlightMode.CONTROL)).build());
    private Setting<Float> fallSpeedHighway = register(Settings.floatBuilder("Fall Speed H").withValue(0.000050000002f).withVisibility(v -> mode.getValue().equals(ElytraFlightMode.HIGHWAY)).build());
    private Setting<Float> fallSpeedControl = register(Settings.floatBuilder("Fall Speed C").withValue(0.001f).withMaximum(0.3f).withMinimum(0.0f).withVisibility(v -> mode.getValue().equals(ElytraFlightMode.CONTROL)).build());
    private Setting<Float> fallSpeed = register(Settings.floatBuilder("Fall Speed").withValue(-.003f).withVisibility(v -> !mode.getValue().equals(ElytraFlightMode.CONTROL) && !mode.getValue().equals(ElytraFlightMode.HIGHWAY)).build());
    private Setting<Float> upSpeedBoost = register(Settings.floatBuilder("Up Speed B").withValue(0.08f).withVisibility(v -> mode.getValue().equals(ElytraFlightMode.BOOST)).build());
    private Setting<Float> downSpeedBoost = register(Settings.floatBuilder("Down Speed B").withValue(0.04f).withVisibility(v -> mode.getValue().equals(ElytraFlightMode.BOOST)).build());
    private Setting<Double> downSpeedControl = register(Settings.doubleBuilder("Down Speed C").withMaximum(10.0).withMinimum(0.0).withValue(2.0).withVisibility(v -> mode.getValue().equals(ElytraFlightMode.CONTROL)).build());

    private ElytraFlightMode enabledMode;
    private boolean hasDoneWarning;

    /* Control mode states */
    private double hoverTarget = -1.0;
    public float packetYaw = 0.0f;
    private boolean hoverState = false;

    /* Control Mode */
    @EventHandler
    private Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if (!mode.getValue().equals(ElytraFlightMode.CONTROL) || mc.player == null) return;
        if (event.getPacket() instanceof CPacketPlayer) {
            if (!mc.player.isElytraFlying()) return;
            CPacketPlayer packet = (CPacketPlayer) event.getPacket();
            packet.pitch = 0.0f;
            packet.yaw = packetYaw;
        }
        if (event.getPacket() instanceof CPacketEntityAction && ((CPacketEntityAction) event.getPacket()).getAction() == CPacketEntityAction.Action.START_FALL_FLYING) {
            hoverTarget = mc.player.posY + 0.35;
        }
    });

    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (!mode.getValue().equals(ElytraFlightMode.CONTROL) || mc.player == null || !mc.player.isElytraFlying()) return;
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
            packet.pitch = ElytraFlight.mc.player.rotationPitch;
        }
    });

    @EventHandler
    private Listener<PlayerTravelEvent> playerTravelListener = new Listener<>(event -> {
        if (!mode.getValue().equals(ElytraFlightMode.CONTROL) || mc.player == null) return;
        boolean doHover;
        if (!mc.player.isElytraFlying()) {
            if (easyTakeOffControl.getValue() && !mc.player.onGround && mc.player.motionY < -0.04) {
                Objects.requireNonNull(mc.getConnection()).sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                if (timerControl.getValue()) mc.timer.tickLength = 200.0f;
                event.cancel();
                return;
            }
            return;
        }

        mc.timer.tickLength = 50.0f;
        if (hoverTarget < 0.0) hoverTarget = mc.player.posY;

        /* this is horrible but what other way to store these for later */
        boolean moveForward = mc.gameSettings.keyBindForward.isKeyDown();
        boolean moveBackward = mc.gameSettings.keyBindBack.isKeyDown();
        boolean moveLeft = mc.gameSettings.keyBindLeft.isKeyDown();
        boolean moveRight = mc.gameSettings.keyBindRight.isKeyDown();
        boolean moveUp = mc.gameSettings.keyBindJump.isKeyDown();
        boolean moveDown = mc.gameSettings.keyBindSneak.isKeyDown();
        float moveForwardFactor = moveForward ? 1.0f : (float) (moveBackward ? -1 : 0);
        float yawDeg = mc.player.rotationYaw;

        if (moveLeft && (moveForward || moveBackward)) {
            yawDeg -= 40.0f * moveForwardFactor;
        } else if (moveRight && (moveForward || moveBackward)) {
            yawDeg += 40.0f * moveForwardFactor;
        } else if (moveLeft) {
            yawDeg -= 90.0f;
        } else if (moveRight) {
            yawDeg += 90.0f;
        }
        if (moveBackward) yawDeg -= 180.0f;

        packetYaw = yawDeg;
        float yaw = (float) Math.toRadians(yawDeg);
        double motionAmount = Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ);
        hoverState = hoverState ? mc.player.posY < hoverTarget + 0.1 : mc.player.posY < hoverTarget + 0.0;
        doHover = hoverState && hoverControl.getValue();
        if (moveUp || moveForward || moveBackward || moveLeft || moveRight || MODULE_MANAGER.isModuleEnabled(AutoWalk.class)) {
            if ((moveUp || doHover) && motionAmount > 1.0) {
                if (mc.player.motionX == 0.0 && mc.player.motionZ == 0.0) {
                    mc.player.motionY = downSpeedControl.getValue();
                } else {
                    double calcMotionDiff = motionAmount * 0.008;
                    mc.player.motionY += calcMotionDiff * 3.2;
                    mc.player.motionX -= (double) (-MathHelper.sin(yaw)) * calcMotionDiff / 1.0;
                    mc.player.motionZ -= (double) MathHelper.cos(yaw) * calcMotionDiff / 1.0;
                    mc.player.motionX *= 0.99f;
                    mc.player.motionY *= 0.98f;
                    mc.player.motionZ *= 0.99f;
                }
            } else { /* runs when pressing wasd */
                mc.player.motionX = (double) (-MathHelper.sin(yaw)) * speedControl.getValue();
                mc.player.motionY = -fallSpeedControl.getValue();
                mc.player.motionZ = (double) MathHelper.cos(yaw) * speedControl.getValue();
            }
        } else { /* Stop moving if no inputs are pressed */
            mc.player.motionX = 0.0;
            mc.player.motionY = 0.0;
            mc.player.motionZ = 0.0;
        }
        if (moveDown) {
            mc.player.motionY = -downSpeedControl.getValue();
        }
        if (moveUp || moveDown) {
            hoverTarget = mc.player.posY;
        }
        event.cancel();
    });
    /* End of Control Mode */

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        if (defaultSetting.getValue()) defaults();

        if (enabledMode != mode.getValue() && !hasDoneWarning) {
            sendErrorMessage("&l&cWARNING:&r Changing the mode while you're flying is not recommended. If you weren't flying you can ignore this message.");
            hasDoneWarning = true;
        }

        if (mode.getValue().equals(ElytraFlightMode.CONTROL)) return;

        takeOff();
        setFlySpeed();

//        sendChatMessage("Rotation: " + mc.player.rotationPitch + " Camera: " + mc.player.cameraPitch);

        /* required on some servers in order to land */
        if (mc.player.onGround) mc.player.capabilities.allowFlying = false;

        if (!mc.player.isElytraFlying()) return;

        if (mode.getValue() == ElytraFlightMode.BOOST) {
            if (mc.player.isInWater()) {
                Objects.requireNonNull(mc.getConnection()).sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                return;
            }

            if (mc.gameSettings.keyBindJump.isKeyDown())
                mc.player.motionY += upSpeedBoost.getValue();
            else
                if (mc.gameSettings.keyBindSneak.isKeyDown())
                    mc.player.motionY -= downSpeedBoost.getValue();

            if (mc.gameSettings.keyBindForward.isKeyDown()) {
                float yaw = (float) Math.toRadians(mc.player.rotationYaw);
                mc.player.motionX -= MathHelper.sin(yaw) * 0.05F;
                mc.player.motionZ += MathHelper.cos(yaw) * 0.05F;
            } else
                if (mc.gameSettings.keyBindBack.isKeyDown()) {
                    float yaw = (float) Math.toRadians(mc.player.rotationYaw);
                    mc.player.motionX += MathHelper.sin(yaw) * 0.05F;
                    mc.player.motionZ -= MathHelper.cos(yaw) * 0.05F;
                }
        } else {
            mc.player.capabilities.setFlySpeed(.915f);
            mc.player.capabilities.isFlying = true;

            if (mc.player.capabilities.isCreativeMode)
                return;
            mc.player.capabilities.allowFlying = true;
        }
    }

    private void setFlySpeed() {
        if (mc.player.capabilities.isFlying) {
            if (mode.getValue().equals(ElytraFlightMode.HIGHWAY)) {
                mc.player.setSprinting(false);
                mc.player.setVelocity(0, 0, 0);
                mc.player.setPosition(mc.player.posX, mc.player.posY - fallSpeedHighway.getValue(), mc.player.posZ);
                mc.player.capabilities.setFlySpeed(getHighwaySpeed());
            } else {
                mc.player.setVelocity(0, 0, 0);
                mc.player.capabilities.setFlySpeed(.915f);
                mc.player.setPosition(mc.player.posX, mc.player.posY - fallSpeed.getValue(), mc.player.posZ);
            }
        }
    }

    private void takeOff() {
        if (!(mode.getValue().equals(ElytraFlightMode.HIGHWAY) && easyTakeOff.getValue())) return;
        if (!mc.player.isElytraFlying() && !mc.player.onGround) {
            switch (takeOffMode.getValue()) {
                case CLIENT:
                    mc.player.capabilities.isFlying = true;
                case PACKET:
                    Objects.requireNonNull(mc.getConnection()).sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                default:
            }
        }

        if (mc.player.isElytraFlying()) {
            easyTakeOff.setValue(false);
            sendChatMessage(getChatName() + "Disabled takeoff!");
        }
    }

    @Override
    protected void onDisable() {
        mc.timer.tickLength = 50.0f;
        mc.player.capabilities.isFlying = false;
        mc.player.capabilities.setFlySpeed(0.05f);
        if (mc.player.capabilities.isCreativeMode) return;
        mc.player.capabilities.allowFlying = false;
    }

    @Override
    protected void onEnable() {
        enabledMode = mode.getValue();
        hoverTarget = -1.0; /* For control mode */
    }

    private void defaults() {
        easyTakeOff.setValue(true);
        hoverControl.setValue(true);
        easyTakeOffControl.setValue(false);
        timerControl.setValue(false);
        takeOffMode.setValue(TakeoffMode.PACKET);
        overrideMaxSpeed.setValue(false);
        speedHighway.setValue(1.8f);
        speedHighwayOverride.setValue(1.8f);
        speedControl.setValue(1.8f);
        fallSpeedHighway.setValue(.000050000002f);
        fallSpeedControl.setValue(0.001f);
        fallSpeed.setValue(-.003f);
        upSpeedBoost.setValue(0.08f);
        downSpeedBoost.setValue(0.04f);
        downSpeedControl.setValue(2.0);
        defaultSetting.setValue(false);
        sendChatMessage(getChatName() + "Set to defaults!");
        closeSettings();
    }

    private float getHighwaySpeed() {
        if (overrideMaxSpeed.getValue()) {
            return speedHighwayOverride.getValue();
        } else {
            return speedHighway.getValue();
        }
    }

    private enum ElytraFlightMode { BOOST, FLY, CONTROL, HIGHWAY }
    private enum TakeoffMode { CLIENT, PACKET }
}
