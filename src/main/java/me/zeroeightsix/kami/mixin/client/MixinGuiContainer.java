package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.gui.mc.KamiGuiStealButton;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.player.ChestStealer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(GuiContainer.class)
public class MixinGuiContainer extends GuiScreen {

    @Shadow protected int guiLeft;
    @Shadow protected int guiTop;
    @Shadow protected int xSize;

    private final ChestStealer chestStealer = ModuleManager.getModuleT(ChestStealer.class);
    private final GuiButton stealButton = new KamiGuiStealButton(this.guiLeft + this.xSize + 2, this.guiTop + 2);

    @Inject(method = "initGui", at = @At("HEAD"))
    public void initGui(CallbackInfo ci) {
        if (chestStealer.isValidGui()) {
            this.buttonList.add(stealButton);
            updateButton();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 696969) {
            chestStealer.setStealing(!chestStealer.getStealing());
        } else {
            super.actionPerformed(button);
        }
    }

    @Inject(method = "updateScreen", at = @At("HEAD"))
    public void updateScreen(CallbackInfo ci) {
        updateButton();
    }

    private void updateButton() {
        if (chestStealer.isEnabled() && chestStealer.isContainerOpen()) {
            String str;
            if (chestStealer.getStealing()) {
                str = "Stop";
            } else {
                str = "Steal";
            }
            stealButton.x = this.guiLeft + this.xSize + 2;
            stealButton.y = this.guiTop + 2;
            stealButton.enabled = chestStealer.canSteal();
            stealButton.visible = true;
            stealButton.displayString = str;
        } else {
            stealButton.visible = false;
        }
    }
}
