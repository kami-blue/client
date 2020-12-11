package me.zeroeightsix.kami.mixin.client.gui;

import me.zeroeightsix.kami.NecronClient;
import me.zeroeightsix.kami.gui.mc.KamiGuiUpdateNotification;
import me.zeroeightsix.kami.gui.mc.NecronGuiKamiImportNotification;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Dewy on 09/04/2020
 */
@Mixin(GuiMainMenu.class)
public abstract class MixinGuiMainMenu {

    private static boolean hasAskedToUpdate = false;

    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    public void onActionPerformed(GuiButton button, CallbackInfo ci) throws IOException {
        Path importKami = Paths.get(NecronClient.DIRECTORY + "hasAskedToImportKamiConfig.txt");
        if (!Files.exists(importKami)) {
            if (Files.exists(Paths.get("KAMIBlueConfig.json"))) {
                Wrapper.getMinecraft().displayGuiScreen(new NecronGuiKamiImportNotification());
                ci.cancel();
            }
            Files.createFile(importKami);
        }
        else if (!hasAskedToUpdate && KamiGuiUpdateNotification.Companion.getLatest() != null && !KamiGuiUpdateNotification.Companion.isLatest()) {
            if (button.id == 1 || button.id == 2) {
                Wrapper.getMinecraft().displayGuiScreen(new KamiGuiUpdateNotification(button.id));
                hasAskedToUpdate = true;
                ci.cancel();
            }
        }
    }
}
