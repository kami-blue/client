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

    @Shadow @Final public NetHandlerPlayClient connection;
    @Shadow protected Minecraft mc;
    @Shadow private double lastReportedPosX;
    @Shadow private double lastReportedPosY;
    @Shadow private double lastReportedPosZ;
    @Shadow private float lastReportedYaw;
    @Shadow private int positionUpdateTicks;
    @Shadow private float lastReportedPitch;

    boolean prevSprinting = false;
    boolean prevSneaking = false;
    boolean prevOnGround = true;
    Vec3d prevPos = new Vec3d(0.0, 0.0, 0.0);
    Vec2f prevRotation = new Vec2f(0f, 0f);


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

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    private void onUpdateWalkingPlayerPre(CallbackInfo ci) {
        prevSprinting = this.isSprinting();
        prevSneaking = this.isSneaking();
        prevOnGround = this.onGround;
        prevPos = new Vec3d(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        prevRotation = new Vec2f(this);

        OnUpdateWalkingPlayerEvent event = new OnUpdateWalkingPlayerEvent(isMoving(), isRotating(), prevSprinting, prevSneaking, prevOnGround, prevPos, prevRotation);
        KamiMod.EVENT_BUS.post(event);

        setMoving(event.getMoving());
        setRotating(event.getRotating());
        this.setSprinting(event.getSprinting());
        this.setSneaking(event.getSneaking());
        this.onGround = prevOnGround;
        this.posX = event.getPos().x;
        this.posY = event.getPos().y;
        this.posZ = event.getPos().z;
        this.rotationYaw = event.getRotation().x;
        this.rotationPitch = event.getRotation().y;
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    private void onUpdateWalkingPlayerPost(CallbackInfo ci) {
        this.setSprinting(prevSprinting);
        this.setSneaking(prevSneaking);
        this.onGround = prevOnGround;
        this.posX = prevPos.x;
        this.posY = prevPos.y;
        this.posZ = prevPos.z;
        this.rotationYaw = prevRotation.x;
        this.rotationPitch = prevRotation.y;
    }

    private boolean isMoving() {
        double xDiff = this.posX - this.lastReportedPosX;
        double yDiff = this.getEntityBoundingBox().minY - this.lastReportedPosY;
        double zDiff = this.posZ - this.lastReportedPosZ;

        return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff > 9.0E-4D || this.positionUpdateTicks >= 20;
    }

    private void setMoving(boolean moving) {
        this.lastReportedPosX = this.posX;
        this.lastReportedPosY = this.posY;
        this.lastReportedPosZ = this.posZ;
        if (moving) { // Force it to update position
            this.lastReportedPosX += 69f;
            this.lastReportedPosY += 69f;
            this.lastReportedPosZ += 69f;
        }
    }

    private boolean isRotating() {
        double yawDiff = this.rotationYaw - this.lastReportedYaw;
        double pitchDiff = this.rotationPitch - this.lastReportedPitch;

        return yawDiff != 0.0D || pitchDiff != 0.0D;
    }

    private void setRotating(boolean rotating) {
        this.lastReportedYaw = this.rotationYaw;
        this.lastReportedPitch = this.rotationPitch;
        if (rotating) { // Force it to update rotation
            this.lastReportedYaw += 69f;
            this.lastReportedPitch += 69f;
        }
    }
}
