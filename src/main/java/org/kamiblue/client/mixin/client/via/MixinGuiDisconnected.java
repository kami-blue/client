package org.kamiblue.client.mixin.client.via;


import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import org.kamiblue.client.via.viafabric.ViaFabric;
import org.kamiblue.client.via.viafabric.util.ProtocolUtils;
import org.kamiblue.client.via.viaforge.gui.GuiProtocolSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiDisconnected.class)
public abstract class MixinGuiDisconnected extends GuiScreen {

    @Inject(method = "initGui", at = @At("RETURN"))
    public void injectInitGui(CallbackInfo ci) {
        buttonList.add(new GuiButton(1337, 5, 6, 98, 20,
                ProtocolUtils.getProtocolName(ViaFabric.clientSideVersion)));
        buttonList.add(new GuiButton(1338, 5, 28, 98, 20, "Reconnect"));
    }

    @Inject(method = "actionPerformed", at = @At("RETURN"))
    public void injectActionPerformed(GuiButton p_actionPerformed_1_, CallbackInfo ci) {
        if (p_actionPerformed_1_.id == 1337)
            mc.displayGuiScreen(new GuiProtocolSelector(this));
        else if (p_actionPerformed_1_.id == 1338)
            mc.displayGuiScreen(new GuiConnecting(new GuiMultiplayer(new GuiMainMenu()), mc,
                    new ServerData(ViaFabric.lastServer, ViaFabric.lastServer, false)));
    }
}
