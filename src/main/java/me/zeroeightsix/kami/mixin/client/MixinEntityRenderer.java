package me.zeroeightsix.kami.mixin.client;

import com.google.common.base.Predicate;
import me.zeroeightsix.kami.module.modules.misc.CameraClip;
import me.zeroeightsix.kami.module.modules.player.Freecam;
import me.zeroeightsix.kami.module.modules.player.NoEntityTrace;
import me.zeroeightsix.kami.module.modules.render.AntiFog;
import me.zeroeightsix.kami.module.modules.render.Brightness;
import me.zeroeightsix.kami.module.modules.render.NoHurtCam;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
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

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * Created by 086 on 11/12/2017.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    private boolean nightVision = false;

    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;"))
    public RayTraceResult rayTraceBlocks(WorldClient world, Vec3d start, Vec3d end) {
        if (MODULE_MANAGER.isModuleEnabled(CameraClip.class))
            return null;
        else
            return world.rayTraceBlocks(start, end);
    }

    @Inject(method = "setupFog", at = @At(value = "HEAD"), cancellable = true)
    public void setupFog(int startCoords, float partialTicks, CallbackInfo callbackInfo) {
        if (AntiFog.enabled() && AntiFog.mode.getValue() == AntiFog.VisionMode.NOFOG)
            callbackInfo.cancel();
    }

    @Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;getBlockStateAtEntityViewpoint(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;F)Lnet/minecraft/block/state/IBlockState;"))
    public IBlockState getBlockStateAtEntityViewpoint(World worldIn, Entity entityIn, float p_186703_2_) {
        if (AntiFog.enabled() && AntiFog.mode.getValue() == AntiFog.VisionMode.AIR) return Blocks.AIR.defaultBlockState;
        return ActiveRenderInfo.getBlockStateAtEntityViewpoint(worldIn, entityIn, p_186703_2_);
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hurtCameraEffect(float ticks, CallbackInfo info) {
        if (NoHurtCam.shouldDisable()) info.cancel();
    }

    @Redirect(method = "updateLightmap", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isPotionActive(Lnet/minecraft/potion/Potion;)Z"))
    public boolean isPotionActive(EntityPlayerSP player, Potion potion) {
        return (nightVision = Brightness.shouldBeActive()) || player.isPotionActive(potion);
    }

    @Redirect(method = "updateLightmap", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;getNightVisionBrightness(Lnet/minecraft/entity/EntityLivingBase;F)F"))
    public float getNightVisionBrightnessMixin(EntityRenderer renderer, EntityLivingBase entity, float partialTicks) {
        if (nightVision) return Brightness.getCurrentBrightness();
        return renderer.getNightVisionBrightness(entity, partialTicks);
    }

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcluding(WorldClient worldClient, Entity entityIn, AxisAlignedBB boundingBox, Predicate predicate) {
        if (NoEntityTrace.shouldBlock())
            return new ArrayList<>();
        else
            return worldClient.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }

    @Redirect(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;isSpectator()Z"))
    public boolean noclipIsSpectator(AbstractClientPlayer acp) {
        // [WebringOfTheDamned]
        // Freecam doesn't actually use spectator mode, but it can go through walls, and only spectator mode is "allowed to" go through walls as far as the renderer is concerned
        if (MODULE_MANAGER.isModuleEnabled(Freecam.class))
            return true;
        return acp.isSpectator();
    }

}
