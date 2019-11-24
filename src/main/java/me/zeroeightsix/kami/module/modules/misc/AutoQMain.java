package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;

/**
 * Created by d1gress/Qther on 24/11/2019.
 */
@Module.Info(name = "AutoQMain", description = "Automatically does \"/queue main\" every 7.1 minutes.", category = Module.Category.MISC)
public class AutoQMain extends Module {

    private static long startTime = 0;

    @Override
    public void onUpdate() {
        if (Minecraft.getMinecraft().getCurrentServerData() == null || (Minecraft.getMinecraft().getCurrentServerData() !=null && !Minecraft.getMinecraft().getCurrentServerData().serverIP.equalsIgnoreCase("2b2t.org"))) {
            Command.sendChatMessage("Not on 2b2t");
            ModuleManager.getModuleByName("AutoQMain").disable();
        } else {
            if (startTime == 0) startTime = System.currentTimeMillis();
            if (startTime + 426000 >= System.currentTimeMillis()) {
                Minecraft.getMinecraft().playerController.connection.sendPacket(new CPacketChatMessage("/queue main"));
                startTime = System.currentTimeMillis();
            }
        }
    }
}
