package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.event.KamiEventBus;
import me.zeroeightsix.kami.event.events.ClientPlayerAttackEvent;
import me.zeroeightsix.kami.module.modules.player.TpsSync;
import me.zeroeightsix.kami.util.TpsCalculator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Redirect(method = "onPlayerDamageBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;getPlayerRelativeBlockHardness(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)F"))
    float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos) {
        return state.getPlayerRelativeBlockHardness(player, worldIn, pos) * (TpsSync.INSTANCE.isEnabled() ? (TpsCalculator.INSTANCE.getTickRate() / 20f) : 1);
    }

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    public void attackEntity(EntityPlayer playerIn, Entity targetEntity, CallbackInfo ci) {
        if (targetEntity == null) return;
        ClientPlayerAttackEvent event = new ClientPlayerAttackEvent(targetEntity);
        KamiEventBus.INSTANCE.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
