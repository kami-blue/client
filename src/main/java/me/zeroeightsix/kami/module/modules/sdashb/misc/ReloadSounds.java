package me.zeroeightsix.kami.module.modules.sdashb.misc;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.gui.GuiScreen;
import me.zeroeightsix.kami.module.Module;

/**
 * Created By AceOfSpades
 */

@Module.Info(name = "ReloadSounds", category = Module.Category.MISC, description = "Reload broken sounds")
public class ReloadSounds extends Module
{
    private GuiScreen ReloadSoundSystem;
    private Setting<Boolean> debug = register(Settings.b("Error messages", false));


    public void onEnable() {
        try {
            final SoundManager sndManager = (SoundManager)ObfuscationReflectionHelper.getPrivateValue((Class)SoundHandler.class, (Object)this.ReloadSoundSystem.mc.getSoundHandler(), new String[] { "sndManager", "sndManager" });
            sndManager.reloadSoundSystem();
            Command.sendChatMessage("[ReloadSounds] Reloaded!");
            this.disable();
        }
        catch (Exception e) {
            System.out.println("Could not restart sounds: " + e.toString());
            e.printStackTrace();
            if (debug.getValue()) {
                Command.sendChatMessage("[ReloadSounds] Error: " + e.toString());
                Command.sendChatMessage("[ReloadSounds] If you get a null pointer exception that's fine, that means sounds are not broken " + e.toString());
            }
            this.disable();
        }
    }
}