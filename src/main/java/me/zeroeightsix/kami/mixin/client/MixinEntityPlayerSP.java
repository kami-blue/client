package me.zeroeightsix.kami.mixin.client;

import com.mojang.authlib.GameProfile;
import me.zeroeightsix.kami.event.KamiEvent;
import me.zeroeightsix.kami.event.KamiEventBus;
import me.zeroeightsix.kami.event.events.OnUpdateWalkingPlayerEvent;
import me.zeroeightsix.kami.event.events.PlayerMoveEvent;
import me.zeroeightsix.kami.gui.mc.KamiGuiBeacon;
import me.zeroeightsix.kami.module.modules.chat.PortalChat;
import me.zeroeightsix.kami.module.modules.misc.BeaconSelector;
import me.zeroeightsix.kami.module.modules.movement.Sprint;
import me.zeroeightsix.kami.module.modules.player.Freecam;
import me.zeroeightsix.kami.util.math.Vec2f;
import me.zeroeightsix.kami.util.text.MessageSendHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.MoverType;
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
    @Shadow protected abstract boolean isCurrentViewEntity();

    public MixinEntityPlayerSP(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V"))
    public void closeScreen(EntityPlayerSP entityPlayerSP) {
        if (PortalChat.INSTANCE.isEnabled()) return;
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    public void closeScreen(Minecraft minecraft, GuiScreen screen) {
        if (PortalChat.INSTANCE.isEnabled()) return;
    }

    /**
     * @author TBM
     * Used with full permission from TBM - l1ving
     */
    @Inject(method = "displayGUIChest", at = @At("HEAD"), cancellable = true)
    public void onDisplayGUIChest(IInventory chestInventory, CallbackInfo ci) {
        if (BeaconSelector.INSTANCE.isEnabled()) {
            if (chestInventory instanceof IInteractionObject) {
                if ("minecraft:beacon".equals(((IInteractionObject) chestInventory).getGuiID())) {
                    Minecraft.getMinecraft().displayGuiScreen(new KamiGuiBeacon(this.inventory, chestInventory));
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void move(MoverType type, double x, double y, double z, CallbackInfo info) {
        PlayerMoveEvent event = new PlayerMoveEvent(type, x, y, z);
        KamiEventBus.INSTANCE.post(event);
        if (event.isCancelled()) info.cancel();
    }

    @Redirect(method = "setSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;setSprinting(Z)V"))
    public void setSprinting(AbstractClientPlayer abstractClientPlayer, boolean sprinting) {
        if (Sprint.INSTANCE.isEnabled() && Sprint.INSTANCE.shouldSprint()) {
            sprinting = Sprint.INSTANCE.getSprinting();
        }
        super.setSprinting(sprinting);
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
        ci.cancel();
        final EntityPlayerSP player = mc.player;
        if (player != null) {
            MessageSendHelper.sendServerMessage(message, player, Integer.MAX_VALUE - 1);
        }
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
        event.setEra(KamiEvent.Era.PERI);
        KamiEventBus.INSTANCE.post(event);

        if (event.isCancelled()) {
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
                    this.connection.sendPacket(new CPacketPlayer.PositionRotation(this.motionX, -999.0D, this.motionZ, rotation.x, rotation.y, onGround));
                    moving = false;
                } else if (moving && rotating) {
                    this.connection.sendPacket(new CPacketPlayer.PositionRotation(pos.x, pos.y, pos.z, rotation.x, rotation.y, onGround));
                } else if (moving) {
                    this.connection.sendPacket(new CPacketPlayer.Position(pos.x, pos.y, pos.z, onGround));
                } else if (rotating) {
                    this.connection.sendPacket(new CPacketPlayer.Rotation(rotation.x, rotation.y, onGround));
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
                    this.lastReportedYaw = rotation.x;
                    this.lastReportedPitch = rotation.y;
                }

                this.prevOnGround = onGround;
                this.autoJumpEnabled = this.mc.gameSettings.autoJump;

            }
        }
        event.setEra(KamiEvent.Era.POST);
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
