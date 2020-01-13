package me.zeroeightsix.kami.module.modules.zeroeightysix.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.MathHelper;

/**
 * Created by 086 on 11/04/2018.
 * Updated by Itistheend on 28/12/19.
 * Updated by S-B99 on 11/01/20
 */

@Module.Info(name = "ElytraFlight", description = "Allows infinite elytra flying", category = Module.Category.MOVEMENT)
public class ElytraFlight extends Module {

    private Setting<ElytraFlightMode> mode = register(Settings.e("Mode", ElytraFlightMode.FLY));
    private Setting<Float> speed = register(Settings.f("Speed", 1.8f));
    private Setting<Boolean> highway = register(Settings.b("Highway Mode", false));
    private Setting<Float> fallspeed = register(Settings.f("Fall Speed", .000050000002f));

//    private Float fallspeed = .000050000002f;

    @Override
    public void onUpdate() {

        if (highway.getValue()) {
            mode.setValue(ElytraFlightMode.FLY);
        }

        if (mc.player.capabilities.isFlying) {
            if (highway.getValue()) {
                mc.player.setVelocity(0, 0, 0);
                mc.player.setPosition(mc.player.posX, mc.player.posY - fallspeed.getValue(), mc.player.posZ);
                mc.player.capabilities.setFlySpeed(speed.getValue());
                mc.player.setSprinting(false);
            }
            else {
                mc.player.setVelocity(0, -.003, 0);
                mc.player.capabilities.setFlySpeed(.915f);
            }
        }

        if (mc.player.onGround) {
            mc.player.capabilities.allowFlying = false;
        }

        if (!mc.player.isElytraFlying()) return;
        switch (mode.getValue()) {
            case BOOST:
                if (mc.player.isInWater()) {
                    mc.getConnection()
                            .sendPacket(new CPacketEntityAction(mc.player,
                                    CPacketEntityAction.Action.START_FALL_FLYING));
                    return;
                }

                if (mc.gameSettings.keyBindJump.isKeyDown())
                    mc.player.motionY += 0.08;
                else if (mc.gameSettings.keyBindSneak.isKeyDown())
                    mc.player.motionY -= 0.04;

                if (mc.gameSettings.keyBindForward.isKeyDown()) {
                    float yaw = (float) Math
                            .toRadians(mc.player.rotationYaw);
                    mc.player.motionX -= MathHelper.sin(yaw) * 0.05F;
                    mc.player.motionZ += MathHelper.cos(yaw) * 0.05F;
                } else if (mc.gameSettings.keyBindBack.isKeyDown()) {
                    float yaw = (float) Math
                            .toRadians(mc.player.rotationYaw);
                    mc.player.motionX += MathHelper.sin(yaw) * 0.05F;
                    mc.player.motionZ -= MathHelper.cos(yaw) * 0.05F;
                }
                break;
            case FLY:
                mc.player.capabilities.setFlySpeed(.915f);
                mc.player.capabilities.isFlying = true;

                if (mc.player.capabilities.isCreativeMode) return;
                mc.player.capabilities.allowFlying = true;
                break;
        }
    }

    @Override
    protected void onDisable() {
        mc.player.capabilities.isFlying = false;
        mc.player.capabilities.setFlySpeed(0.05f);
        if (mc.player.capabilities.isCreativeMode) return;
        mc.player.capabilities.allowFlying = false;
    }

    private enum ElytraFlightMode {
        BOOST, FLY
    }

}
