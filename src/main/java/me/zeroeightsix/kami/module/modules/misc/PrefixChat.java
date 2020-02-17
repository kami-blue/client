package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;

@Module.Info(name = "PrefixChat", category = Module.Category.MISC, description = "Opens chat with prefix inside when prefix is pressed.")
public class PrefixChat extends Module {
    private Setting<Boolean> disableOnSneak = register(Settings.b("Disable on Sneak", true));
}
