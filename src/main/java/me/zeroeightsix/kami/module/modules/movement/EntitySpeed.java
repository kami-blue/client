package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.util.MovementInput;
import net.minecraft.world.chunk.EmptyChunk;

/**
 * Created by 086 on 16/12/2017.
 */
@Module.Info(
        name = "EntitySpeed",
        category = Module.Category.MOVEMENT,
        description = "Abuse client-sided movement to shape sound barrier breaking rideables"
)
public class EntitySpeed extends Module {

    private Setting<Float> speed = register(Settings.f("Speed", 1));
    private Setting<Boolean> antiStuck = register(Settings.b("AntiStuck"));
    private Setting<Boolean> flight = register(Settings.b("Flight", false));
    private Setting<Boolean> wobble = register(Settings.booleanBuilder("Wobble").withValue(true).withVisibility(b -> flight.getValue()).build());
    private static Setting<Float> opacity = Settings.f("Boat opacity", .5f);

    public EntitySpeed() {
        register(opacity);
    }

    @Override
    public void onUpdate() {
        if ((mc.world != null) && (mc.player.getRidingEntity() != null)) {
            Entity riding = mc.player.getRidingEntity();
            if (riding instanceof EntityPig || riding instanceof AbstractHorse) {
                steerEntity(riding);
            } else if (riding instanceof EntityBoat) {
                steerBoat(getBoat());
            }
        }
    }

    private void steerEntity(Entity entity) {
        if (!flight.getValue()) {
            entity.motionY = -0.4D;
        }

        if (flight.getValue()) {
            if (mc.gameSettings.keyBindJump.isKeyDown())
                entity.motionY = speed.getValue();
            else if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown())
                entity.motionY = wobble.getValue() ? Math.sin(mc.player.ticksExisted) : 0;
        }

        moveForward(entity, speed.getValue() * 3.8D);

        if (entity instanceof EntityHorse) {
            entity.rotationYaw = mc.player.rotationYaw;
        }
    }

    private void steerBoat(EntityBoat boat) {
        if (boat == null) return;

        int angle;

        boolean forward = mc.gameSettings.keyBindForward.isKeyDown();
        boolean left = mc.gameSettings.keyBindLeft.isKeyDown();
        boolean right = mc.gameSettings.keyBindRight.isKeyDown();
        boolean back = mc.gameSettings.keyBindBack.isKeyDown();
        if (!(forward && back)) boat.motionY = 0;
        if (mc.gameSettings.keyBindJump.isKeyDown()) boat.motionY += speed.getValue() / 2f;

        if (!forward && !left && !right && !back) return;
        if (left && right) angle = forward ? 0 : back ? 180 : -1;
        else if (forward && back) angle = left ? -90 : (right ? 90 : -1);
        else {
            angle = left ? -90 : (right ? 90 : 0);
            if (forward) angle /= 2;
            else if (back) angle = 180 - (angle / 2);
        }

        if (angle == -1) return;
        float yaw = mc.player.rotationYaw + angle;
        boat.motionX = EntityUtil.getRelativeX(yaw) * speed.getValue();
        boat.motionZ = EntityUtil.getRelativeZ(yaw) * speed.getValue();
    }

    @Override
    public void onRender() {
        EntityBoat boat = getBoat();
        if (boat == null) return;
        boat.rotationYaw = mc.player.rotationYaw;
        boat.updateInputs(false, false, false, false); // Make sure the boat doesn't turn etc (params: isLeftDown, isRightDown, isForwardDown, isBackDown)
    }

    private EntityBoat getBoat() {
        if (mc.player.getRidingEntity() != null && mc.player.getRidingEntity() instanceof EntityBoat)
            return (EntityBoat) mc.player.getRidingEntity();
        return null;
    }

    private void moveForward(Entity entity, double speed) {
        if (entity != null) {
            MovementInput movementInput = mc.player.movementInput;

            double forward = movementInput.moveForward;
            double strafe = movementInput.moveStrafe;
            boolean movingForward = forward != 0;
            boolean movingStrafe = strafe != 0;
            float yaw = mc.player.rotationYaw;

            if (!movingForward && !movingStrafe) {
                setEntitySpeed(entity, 0, 0);
            } else {
                if (forward != 0.0D) {
                    if (strafe > 0.0D) {
                        yaw += (forward > 0.0D ? -45 : 45);
                    } else if (strafe < 0.0D) {
                        yaw += (forward > 0.0D ? 45 : -45);
                    }
                    strafe = 0.0D;
                    if (forward > 0.0D) {
                        forward = 1.0D;
                    } else {
                        forward = -1.0D;
                    }
                }

                double motX = (forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F)));
                double motZ = (forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F)));

                if (isBorderingChunk(entity, motX, motZ))
                    motX = motZ = 0;

                setEntitySpeed(entity, motX, motZ);
            }
        }
    }

    private void setEntitySpeed(Entity entity, double motX, double motZ) {
        entity.motionX = motX;
        entity.motionZ = motZ;
    }

    private boolean isBorderingChunk(Entity entity, double motX, double motZ) {
        return antiStuck.getValue() && mc.world.getChunk((int) (entity.posX + motX) >> 4, (int) (entity.posZ + motZ) >> 4) instanceof EmptyChunk;
    }

    public static float getOpacity() {
        return opacity.getValue();
    }

}
