package me.zeroeightsix.kami.mixin.client.gui;

import me.zeroeightsix.kami.module.modules.render.ExtraTab;
import me.zeroeightsix.kami.module.modules.render.TabFriends;
import me.zeroeightsix.kami.util.TimerUtils;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by 086 on 8/04/2018.
 * Optimized by l1ving on 28/05/20
 */
@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay {

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;", remap = false))
    public <E> List<E> subList(List<E> list, int fromIndex, int toIndex) {
        return list.subList(fromIndex, ExtraTab.INSTANCE.isEnabled() ? Math.min(ExtraTab.INSTANCE.getTabSize().getValue(), list.size()) : toIndex);
    }

    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    public void getPlayerName(NetworkPlayerInfo networkPlayerInfoIn, CallbackInfoReturnable<String> cir) {
        if (TabFriends.INSTANCE.isEnabled()) {
            cir.setReturnValue(TabFriends.getPlayerName(networkPlayerInfoIn));
        }
    }

}
