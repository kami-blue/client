package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.DiscordPresence;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.GuiScreenEvent;
import me.zeroeightsix.kami.module.MacroManager;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by 086 on 17/11/2017.
 */
@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow
    public WorldClient world;
    @Shadow
    public EntityPlayerSP player;
    @Shadow
    public GuiScreen currentScreen;
    @Shadow
    public GameSettings gameSettings;
    @Shadow
    public GuiIngame ingameGUI;
    @Shadow
    public boolean skipRenderWorld;
    @Shadow
    public SoundHandler soundHandler;

    @Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
    public void displayGuiScreen(GuiScreen guiScreenIn, CallbackInfo info) {
        GuiScreenEvent.Closed screenEvent = new GuiScreenEvent.Closed(Wrapper.getMinecraft().currentScreen);
        KamiMod.EVENT_BUS.post(screenEvent);
        GuiScreenEvent.Displayed screenEvent1 = new GuiScreenEvent.Displayed(guiScreenIn);
        KamiMod.EVENT_BUS.post(screenEvent1);
        guiScreenIn = screenEvent1.getScreen();

        if (guiScreenIn == null && this.world == null) {
            guiScreenIn = new GuiMainMenu();
        } else if (guiScreenIn == null && this.player.getHealth() <= 0.0F) {
            guiScreenIn = new GuiGameOver(null);
        }

        GuiScreen old = this.currentScreen;
        net.minecraftforge.client.event.GuiOpenEvent event = new net.minecraftforge.client.event.GuiOpenEvent(guiScreenIn);

        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) return;

        guiScreenIn = event.getGui();
        if (old != null && guiScreenIn != old) {
            old.onGuiClosed();
        }

        if (guiScreenIn instanceof GuiMainMenu || guiScreenIn instanceof GuiMultiplayer) {
            this.gameSettings.showDebugInfo = false;
            this.ingameGUI.getChatGUI().clearChatMessages(true);
        }

        this.currentScreen = guiScreenIn;

        if (guiScreenIn != null) {
            Minecraft.getMinecraft().setIngameNotInFocus();
            KeyBinding.unPressAllKeys();

            while (Mouse.next()) {
            }

            while (Keyboard.next()) {
            }

            ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            guiScreenIn.setWorldAndResolution(Minecraft.getMinecraft(), i, j);
            this.skipRenderWorld = false;
        } else {
            this.soundHandler.resumeSounds();
            Minecraft.getMinecraft().setIngameFocus();
        }

        info.cancel();
    }

    @Inject(method = "run", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;displayCrashReport(Lnet/minecraft/crash/CrashReport;)V", shift = At.Shift.BEFORE))
    public void displayCrashReport(CallbackInfo _info) {
       save();
       DiscordPresence.end();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    public void shutdown(CallbackInfo info) {
        save();
        DiscordPresence.end();
    }

    private void save() {
        System.out.println("Shutting down: saving KAMI configuration");
        KamiMod.saveConfiguration();
        MacroManager.INSTANCE.saveMacros();
        System.out.println("Configuration saved.");
    }

}

