package org.kamiblue.client.mixin.client.player;

import com.mojang.authlib.GameProfile;
import org.kamiblue.client.event.KamiEventBus;
import org.kamiblue.client.event.events.OnUpdateWalkingPlayerEvent;
import org.kamiblue.client.gui.mc.KamiGuiBeacon;
import org.kamiblue.client.manager.managers.MessageManager;
import org.kamiblue.client.module.modules.chat.PortalChat;
import org.kamiblue.client.module.modules.misc.BeaconSelector;
import org.kamiblue.client.module.modules.movement.Sprint;
import org.kamiblue.client.module.modules.player.Freecam;
import org.kamiblue.client.util.Wrapper;
import org.kamiblue.client.util.math.Vec2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityPlayerSP.class, priority = Integer.MAX_VALUE)
public abstract class MixinEntityPlayerSP extends EntityPlayer {

    @Shadow @Final public NetHandlerPlayClient connection;
    @Shadow protected Minecraft mc;
    @Shadow private double lastReportedPosX;
    @Shadow private double lastReportedPosY;
    @Shadow private double lastReportedPosZ;
    @Shadow private float lastReportedYaw;
    @Shadow private int positionUpdateTicks;
    @Shadow private float lastReportedPitch;
    @Shadow private boolean serverSprintState;
    @Shadow private boolean serverSneakState;
    @Shadow private boolean prevOnGround;
    @Shadow private boolean autoJumpEnabled;

    @Shadow
    protected abstract boolean isCurrentViewEntity();

    public MixinEntityPlayerSP(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V"))
    public void closeScreen(EntityPlayerSP player) {
        if (PortalChat.INSTANCE.isDisabled()) player.closeScreen();
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    public void closeScreen(Minecraft minecraft, GuiScreen screen) {
        if (PortalChat.INSTANCE.isDisabled()) Wrapper.getMinecraft().displayGuiScreen(screen);
    }

    /**
     * @author TBM
     * Used with full permission from TBM - l1ving
     */
    @Inject(method = "displayGUIChest", at = @At("HEAD"), cancellable = true)
    public void onDisplayGUIChest(IInventory chestInventory, CallbackInfo ci) {
        if (BeaconSelector.INSTANCE.isEnabled()) {
            if (chestInventory instanceof IInteractionObject && "minecraft:beacon".equals(((IInteractionObject) chestInventory).getGuiID())) {
                Minecraft.getMinecraft().displayGuiScreen(new KamiGuiBeacon(this.inventory, chestInventory));
                ci.cancel();
            }
        }
    }

    @ModifyArg(method = "setSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;setSprinting(Z)V"), index = 0)
    public boolean modifySprinting(boolean sprinting) {
        if (Sprint.INSTANCE.isEnabled() && Sprint.INSTANCE.shouldSprint()) {
            return Sprint.INSTANCE.getSprinting();
        } else {
            return sprinting;
        }
    }

    // We have to return true here so it would still update movement inputs from Baritone and send packets
    @Inject(method = "isCurrentViewEntity", at = @At("RETURN"), cancellable = true)
    protected void mixinIsCurrentViewEntity(CallbackInfoReturnable<Boolean> cir) {
        if (Freecam.INSTANCE.isEnabled() && Freecam.INSTANCE.getCameraGuy() != null) {
            cir.setReturnValue(mc.getRenderViewEntity() == Freecam.INSTANCE.getCameraGuy());
        }
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(String message, CallbackInfo ci) {
        MessageManager.INSTANCE.setLastPlayerMessage(message);
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    private void onUpdateWalkingPlayerPre(CallbackInfo ci) {
        // Setup flags
        boolean moving = isMoving();
        boolean rotating = isRotating();
        boolean sprinting = this.isSprinting();
        boolean sneaking = this.isSneaking();
        boolean onGround = this.onGround;
        Vec3d pos = new Vec3d(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        Vec2f rotation = new Vec2f(this);

        OnUpdateWalkingPlayerEvent event = new OnUpdateWalkingPlayerEvent(moving, rotating, sprinting, sneaking, onGround, pos, rotation);
        KamiEventBus.INSTANCE.post(event);

        event = event.nextPhase();
        KamiEventBus.INSTANCE.post(event);

        if (event.getCancelled()) {
            ci.cancel();

            ++this.positionUpdateTicks;

            // Copy flags from event
            moving = event.getMoving();
            rotating = event.getRotating();
            sprinting = event.getSprinting();
            sneaking = event.getSneaking();
            onGround = event.getOnGround();
            pos = event.getPos();
            rotation = event.getRotation();

            // Sprinting Packet
            if (sprinting != this.serverSprintState) {
                if (sprinting) {
                    this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SPRINTING));
                } else {
                    this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SPRINTING));
                }
                this.serverSprintState = sprinting;
            }

            // Sneaking Packet
            if (sneaking != this.serverSneakState) {
                if (sneaking) {
                    this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SNEAKING));
                } else {
                    this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SNEAKING));
                }
                this.serverSneakState = sneaking;
            }

            // Position & Rotation Packet
            if (this.isCurrentViewEntity()) {

                if (this.isRiding()) {
                    this.connection.sendPacket(new CPacketPlayer.PositionRotation(this.motionX, -999.0D, this.motionZ, rotation.getX(), rotation.getY(), onGround));
                    moving = false;
                } else if (moving && rotating) {
                    this.connection.sendPacket(new CPacketPlayer.PositionRotation(pos.x, pos.y, pos.z, rotation.getX(), rotation.getY(), onGround));
                } else if (moving) {
                    this.connection.sendPacket(new CPacketPlayer.Position(pos.x, pos.y, pos.z, onGround));
                } else if (rotating) {
                    this.connection.sendPacket(new CPacketPlayer.Rotation(rotation.getX(), rotation.getY(), onGround));
                } else if (this.prevOnGround != onGround) {
                    this.connection.sendPacket(new CPacketPlayer(onGround));
                }

                if (moving) {
                    this.lastReportedPosX = pos.x;
                    this.lastReportedPosY = pos.y;
                    this.lastReportedPosZ = pos.z;
                    this.positionUpdateTicks = 0;
                }

                if (rotating) {
                    this.lastReportedYaw = rotation.getX();
                    this.lastReportedPitch = rotation.getY();
                }

                this.prevOnGround = onGround;
                this.autoJumpEnabled = this.mc.gameSettings.autoJump;

            }
        }

        event = event.nextPhase();
        KamiEventBus.INSTANCE.post(event);
    }

    private boolean isMoving() {
        double xDiff = this.posX - this.lastReportedPosX;
        double yDiff = this.getEntityBoundingBox().minY - this.lastReportedPosY;
        double zDiff = this.posZ - this.lastReportedPosZ;

        return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff > 9.0E-4D || this.positionUpdateTicks >= 20;
    }

    private boolean isRotating() {
        double yawDiff = this.rotationYaw - this.lastReportedYaw;
        double pitchDiff = this.rotationPitch - this.lastReportedPitch;

        return yawDiff != 0.0D || pitchDiff != 0.0D;
    }
}
