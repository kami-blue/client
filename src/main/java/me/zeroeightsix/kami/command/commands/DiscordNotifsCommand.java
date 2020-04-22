package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.module.modules.chat.DiscordNotifs;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendErrorMessage;

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 26/03/20
 */
public class DiscordNotifsCommand extends Command {
    public DiscordNotifsCommand() {
        super("discordnotifs", new ChunkBuilder().append("webhook url").append("discord id").append("avatar url").build(), "webhook");
    }

    @Override
    public void call(String[] args) {
        DiscordNotifs df = MODULE_MANAGER.getModuleT(DiscordNotifs.class);
        if (args[0] != null && !args[0].equals("")) {
            df.url.setValue(args[0]);
            sendChatMessage(df.getChatName() + "Set URL to \"" + args[0] + "\"!");
        } else if (args[0] == null) {
            sendErrorMessage(df.getChatName() + "Error: you must specify a URL or \"\" for the first parameter when running the command");
        }

        if (args[1] == null) return;
        if (!args[1].equals("")) {
            df.pingID.setValue(args[1]);
            sendChatMessage(df.getChatName() + "Set Discord ID to \"" + df.pingID.getValue() + "\"!");
        }

        if (args[2] == null) return;
        if (!args[2].equals("")) {
            df.avatar.setValue(args[2]);
            sendChatMessage(df.getChatName() + "Set Avatar to \"" + args[2] + "\"!");
        } else {
            df.avatar.setValue(KamiMod.GITHUB_LINK + "raw/assets/assets/icons/kami.png");
            sendChatMessage(df.getChatName() + "Reset Avatar!");
        }
    }
}
