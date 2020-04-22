package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;

/**
 * Created by 086 on 25/08/2017.
 */
@Module.Info(
        category = Module.Category.MOVEMENT,
        description = "Makes the player fly", name = "Flight"
)
public class Flight extends Module {

    private Setting<Float> speed = register(Settings.f("Speed", 10));
    private Setting<FlightMode> mode = register(Settings.e("Mode", FlightMode.VANILLA));

    @Override
    protected void onEnable() {
        if (mc.player == null) return;
        switch (mode.getValue()) {
            case VANILLA:
                mc.player.capabilities.isFlying = true;
                if (mc.player.capabilities.isCreativeMode) return;
                mc.player.capabilities.allowFlying = true;
                break;
        }
    }

    @Override
    public void onUpdate() {
        switch (mode.getValue()) {
            case STATIC:
                mc.player.capabilities.isFlying = false;
                mc.player.motionX = 0;
                mc.player.motionY = 0;
                mc.player.motionZ = 0;
                mc.player.jumpMovementFactor = speed.getValue();

                if (mc.gameSettings.keyBindJump.isKeyDown())
                    mc.player.motionY += speed.getValue();
                if (mc.gameSettings.keyBindSneak.isKeyDown())
                    mc.player.motionY -= speed.getValue();
                break;
            case VANILLA:
                mc.player.capabilities.setFlySpeed(speed.getValue() / 100f);
                mc.player.capabilities.isFlying = true;
                if (mc.player.capabilities.isCreativeMode) return;
                mc.player.capabilities.allowFlying = true;
                break;
            case PACKET:
                int angle;

                boolean forward = mc.gameSettings.keyBindForward.isKeyDown();
                boolean left = mc.gameSettings.keyBindLeft.isKeyDown();
                boolean right = mc.gameSettings.keyBindRight.isKeyDown();
                boolean back = mc.gameSettings.keyBindBack.isKeyDown();

                if (left && right) angle = forward ? 0 : back ? 180 : -1;
                else if (forward && back) angle = left ? -90 : (right ? 90 : -1);
                else {
                    angle = left ? -90 : (right ? 90 : 0);
                    if (forward) angle /= 2;
                    else if (back) angle = 180 - (angle / 2);
                }

                if (angle != -1 && (forward || left || right || back)) {
                    float yaw = mc.player.rotationYaw + angle;
                    mc.player.motionX = EntityUtil.getRelativeX(yaw) * 0.2f;
                    mc.player.motionZ = EntityUtil.getRelativeZ(yaw) * 0.2f;
                }

                mc.player.motionY = 0;
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + mc.player.motionX, mc.player.posY + (Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown() ? 0.0622 : 0) - (Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown() ? 0.0622 : 0), mc.player.posZ + mc.player.motionZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + mc.player.motionX, mc.player.posY - 42069, mc.player.posZ + mc.player.motionZ, mc.player.rotationYaw, mc.player.rotationPitch, true));
                break;
        }
    }

    @Override
    protected void onDisable() {
        switch (mode.getValue()) {
            case VANILLA:
                mc.player.capabilities.isFlying = false;
                mc.player.capabilities.setFlySpeed(0.05f);
                if (mc.player.capabilities.isCreativeMode) return;
                mc.player.capabilities.allowFlying = false;
                break;
        }
    }

    public double[] moveLooking() {
        return new double[]{mc.player.rotationYaw * 360.0F / 360.0F * 180.0F / 180.0F, 0.0D};
    }

    public enum FlightMode {
        VANILLA, STATIC, PACKET
    }

}
