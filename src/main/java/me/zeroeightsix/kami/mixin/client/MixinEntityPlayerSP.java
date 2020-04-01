package me.zeroeightsix.kami.mixin.client;

import com.mojang.authlib.GameProfile;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.PlayerMoveEvent;
import me.zeroeightsix.kami.module.modules.chat.PortalChat;
import me.zeroeightsix.kami.module.modules.misc.BeaconSelector;
import me.zeroeightsix.kami.util.BeaconGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * Created by 086 on 12/12/2017.
 */
@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends EntityPlayer {

    public MixinEntityPlayerSP(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V"))
    public void closeScreen(EntityPlayerSP entityPlayerSP) {
        if (MODULE_MANAGER.isModuleEnabled(PortalChat.class)) return;
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    public void closeScreen(Minecraft minecraft, GuiScreen screen) {
        if (MODULE_MANAGER.isModuleEnabled(PortalChat.class)) return;
    }

    /**
     * @author TBM
     */
    @Inject(method = "displayGUIChest", at = @At("HEAD"), cancellable = true)
    public void onDisplayGUIChest(IInventory chestInventory, CallbackInfo ci) {
        if (MODULE_MANAGER.isModuleEnabled(BeaconSelector.class)) {
            if (chestInventory instanceof IInteractionObject) {
                if ("minecraft:beacon".equals(((IInteractionObject)chestInventory).getGuiID())) {
                    Minecraft.getMinecraft().displayGuiScreen(new BeaconGui(this.inventory, chestInventory));
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

}
