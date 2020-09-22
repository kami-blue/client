package me.zeroeightsix.kami.mixin.client;

import com.google.common.base.Predicate;
import me.zeroeightsix.kami.module.modules.movement.ElytraFlight;
import me.zeroeightsix.kami.module.modules.player.Freecam;
import me.zeroeightsix.kami.module.modules.player.NoEntityTrace;
import me.zeroeightsix.kami.module.modules.render.AntiFog;
import me.zeroeightsix.kami.module.modules.render.AntiOverlay;
import me.zeroeightsix.kami.module.modules.render.CameraClip;
import me.zeroeightsix.kami.module.modules.render.NoHurtCam;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = EntityRenderer.class, priority = Integer.MAX_VALUE)
public class MixinEntityRenderer {

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
        if (AntiFog.INSTANCE.isEnabled() && AntiFog.INSTANCE.getMode().getValue() == AntiFog.VisionMode.NO_FOG)
            callbackInfo.cancel();
    }

    @Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;getBlockStateAtEntityViewpoint(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;F)Lnet/minecraft/block/state/IBlockState;"))
    public IBlockState getBlockStateAtEntityViewpoint(World worldIn, Entity entityIn, float p_186703_2_) {
        if (AntiFog.INSTANCE.isEnabled() && AntiFog.INSTANCE.getMode().getValue() == AntiFog.VisionMode.AIR)
            return Blocks.AIR.defaultBlockState;
        return ActiveRenderInfo.getBlockStateAtEntityViewpoint(worldIn, entityIn, p_186703_2_);
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

    @Redirect(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSpectator()Z"))
    public boolean noclipIsSpectator(EntityPlayerSP entityPlayerSP) {
        // [WebringOfTheDamned]
        // Freecam doesn't actually use spectator mode, but it can go through walls, and only spectator mode is "allowed to" go through walls as far as the renderer is concerned
        if (Freecam.INSTANCE.isEnabled())
            return true;
        return entityPlayerSP.isSpectator();
    }

    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getEyeHeight()F"))
    public float getEyeHeight(Entity entity) {
        if (ElytraFlight.INSTANCE.shouldSwing()) {
            return 0.4F;
        } else {
            return entity.getEyeHeight();
        }
    }
}
