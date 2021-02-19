package org.kamiblue.client.mixin.client.render;

import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.kamiblue.client.event.KamiEventBus;
import org.kamiblue.client.event.events.RenderOverlayEvent;
import org.kamiblue.client.module.modules.movement.ElytraFlight;
import org.kamiblue.client.module.modules.player.Freecam;
import org.kamiblue.client.module.modules.player.NoEntityTrace;
import org.kamiblue.client.module.modules.player.ViewLock;
import org.kamiblue.client.module.modules.render.AntiFog;
import org.kamiblue.client.module.modules.render.AntiOverlay;
import org.kamiblue.client.module.modules.render.CameraClip;
import org.kamiblue.client.module.modules.render.NoHurtCam;
import org.kamiblue.client.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = EntityRenderer.class, priority = Integer.MAX_VALUE)
public class MixinEntityRenderer {

    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V", shift = At.Shift.AFTER))
    public void updateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo ci) {
        KamiEventBus.INSTANCE.post(new RenderOverlayEvent());
    }

    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;"))
    public RayTraceResult rayTraceBlocks(WorldClient world, Vec3d start, Vec3d end) {
        if (CameraClip.INSTANCE.isEnabled()) return null;
        else return world.rayTraceBlocks(start, end);
    }

    @Inject(method = "displayItemActivation", at = @At(value = "HEAD"), cancellable = true)
    public void displayItemActivation(ItemStack stack, CallbackInfo callbackInfo) {
        if (AntiOverlay.INSTANCE.isEnabled() && AntiOverlay.INSTANCE.getTotems().getValue()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "setupFog", at = @At(value = "HEAD"), cancellable = true)
    public void setupFog(int startCoords, float partialTicks, CallbackInfo callbackInfo) {
        if (AntiFog.INSTANCE.getShouldNoFog()) {
            callbackInfo.cancel();
        }
    }

    @Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;getBlockStateAtEntityViewpoint(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;F)Lnet/minecraft/block/state/IBlockState;"))
    public IBlockState getBlockStateAtEntityViewpoint(World worldIn, Entity entityIn, float p_186703_2_) {
        if (AntiFog.INSTANCE.getShouldAir()) {
            return Blocks.AIR.getDefaultState();
        } else {
            return ActiveRenderInfo.getBlockStateAtEntityViewpoint(worldIn, entityIn, p_186703_2_);
        }
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hurtCameraEffect(float ticks, CallbackInfo info) {
        if (NoHurtCam.INSTANCE.isEnabled()) info.cancel();
    }

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcluding(WorldClient worldClient, Entity entityIn, AxisAlignedBB boundingBox, Predicate<? super Entity> predicate) {
        if (NoEntityTrace.INSTANCE.shouldIgnoreEntity())
            return new ArrayList<>();
        else
            return worldClient.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }

    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getEyeHeight()F"))
    public float getEyeHeight(Entity entity) {
        if (ElytraFlight.INSTANCE.shouldSwing()) {
            return 0.4F;
        } else {
            return entity.getEyeHeight();
        }
    }

    @Redirect(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;turn(FF)V"))
    public void turn(EntityPlayerSP player, float yaw, float pitch) {
        if (ViewLock.INSTANCE.isEnabled() && Freecam.INSTANCE.isDisabled()) {
            Vec2f rotation = ViewLock.INSTANCE.handleTurn(yaw, pitch);
            player.turn(rotation.getX(), rotation.getY());
        } else {
            player.turn(yaw, pitch);
        }
    }
}
