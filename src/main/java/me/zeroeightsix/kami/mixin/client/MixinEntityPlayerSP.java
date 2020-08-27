package me.zeroeightsix.kami.mixin.client;

import com.mojang.authlib.GameProfile;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.OnUpdateWalkingPlayerEvent;
import me.zeroeightsix.kami.event.events.PlayerMoveEvent;
import me.zeroeightsix.kami.gui.mc.KamiGuiBeacon;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.chat.PortalChat;
import me.zeroeightsix.kami.module.modules.misc.BeaconSelector;
import me.zeroeightsix.kami.module.modules.movement.Sprint;
import me.zeroeightsix.kami.util.math.Vec2f;
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

/**
 * Created by 086 on 12/12/2017.
 */
@Mixin(value = EntityPlayerSP.class, priority = Integer.MAX_VALUE)
public abstract class MixinEntityPlayerSP extends EntityPlayer {

    @Shadow private boolean serverSprintState;
    @Shadow @Final public NetHandlerPlayClient connection;
    @Shadow private boolean serverSneakState;
    @Shadow protected abstract boolean isCurrentViewEntity();
    @Shadow private double lastReportedPosX;
    @Shadow private double lastReportedPosY;
    @Shadow private double lastReportedPosZ;
    @Shadow private float lastReportedYaw;
    @Shadow private float lastReportedPitch;
    @Shadow private int positionUpdateTicks;
    @Shadow private boolean prevOnGround;
    @Shadow private boolean autoJumpEnabled;
    @Shadow protected Minecraft mc;

    @Shadow public abstract boolean isSneaking();

    public MixinEntityPlayerSP(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V"))
    public void closeScreen(EntityPlayerSP entityPlayerSP) {
        if (ModuleManager.isModuleEnabled(PortalChat.class)) return;
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    public void closeScreen(Minecraft minecraft, GuiScreen screen) {
        if (ModuleManager.isModuleEnabled(PortalChat.class)) return;
    }

    /**
     * @author TBM
     * Used with full permission from TBM - dominikaaaa
     */
    @Inject(method = "displayGUIChest", at = @At("HEAD"), cancellable = true)
    public void onDisplayGUIChest(IInventory chestInventory, CallbackInfo ci) {
        if (ModuleManager.isModuleEnabled(BeaconSelector.class)) {
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
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) info.cancel();
    }

    @Redirect(method = "setSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;setSprinting(Z)V"))
    public void setSprinting(AbstractClientPlayer abstractClientPlayer, boolean sprinting) {
        Sprint sprint = ModuleManager.getModuleT(Sprint.class);
        if (sprint != null && sprint.isEnabled() && sprint.shouldSprint()) {
            sprinting = sprint.getSprinting();
        }
        super.setSprinting(sprinting);
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V"))
    private void onUpdateWalkingPlayer(EntityPlayerSP player) {

        // Setup flags
        ++this.positionUpdateTicks;
        boolean moving = isMoving();
        boolean rotating = isRotating();
        boolean sprinting = this.isSprinting();
        boolean sneaking = this.isSneaking();
        boolean onGround = this.onGround;
        Vec3d pos = new Vec3d(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        Vec2f rotation = new Vec2f(this);

        OnUpdateWalkingPlayerEvent event = new OnUpdateWalkingPlayerEvent(moving, rotating, sprinting, sneaking, onGround, pos, rotation);
        KamiMod.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            return;
        }

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
