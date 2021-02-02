package org.kamiblue.client.mixin.client.gui;

import org.kamiblue.client.module.modules.render.ExtraTab;
import org.kamiblue.client.module.modules.render.TabFriends;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Created by 086 on 8/04/2018.
 * Optimized by l1ving on 28/05/20
 */
@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay {

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;", remap = false))
    public <E> List<E> subList(List<E> list, int fromIndex, int toIndex) {
        return ExtraTab.INSTANCE.subList(list, fromIndex, toIndex);
    }

    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    public void getPlayerName(NetworkPlayerInfo networkPlayerInfoIn, CallbackInfoReturnable<String> cir) {
        if (TabFriends.INSTANCE.isEnabled()) {
            cir.setReturnValue(TabFriends.getPlayerName(networkPlayerInfoIn));
        }
    }

}
