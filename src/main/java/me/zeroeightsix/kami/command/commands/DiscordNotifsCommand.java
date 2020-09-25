package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.module.modules.chat.DiscordNotifs;

import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage;

/**
 * @author l1ving
 * Created by l1ving on 26/03/20
 */
public class DiscordNotifsCommand extends Command {
    public DiscordNotifsCommand() {
        super("discordnotifs", new ChunkBuilder().append("webhook url").append("discord id").append("avatar url").build(), "webhook");
    }

    @Override
    public void call(String[] args) {
        if (args[0] != null && !args[0].equals("")) {
            DiscordNotifs.INSTANCE.getUrl().setValue(args[0]);
            sendChatMessage(DiscordNotifs.INSTANCE.getChatName() + " Set URL to \"" + args[0] + "\"!");
        } else if (args[0] == null) {
            sendErrorMessage(DiscordNotifs.INSTANCE.getChatName() + " Error: you must specify a URL or \"\" for the first parameter when running the command");
        }

        if (args[1] == null) return;
        if (!args[1].equals("")) {
            DiscordNotifs.INSTANCE.getPingID().setValue(args[1]);
            sendChatMessage(DiscordNotifs.INSTANCE.getChatName() + " Set Discord ID to \"" + DiscordNotifs.INSTANCE.getPingID().getValue() + "\"!");
        }

        if (args[2] == null) return;
        if (!args[2].equals("")) {
            DiscordNotifs.INSTANCE.getAvatar().setValue(args[2]);
            sendChatMessage(DiscordNotifs.INSTANCE.getChatName() + " Set Avatar to \"" + args[2] + "\"!");
        } else {
            DiscordNotifs.INSTANCE.getAvatar().setValue(KamiMod.GITHUB_LINK + "raw/assets/assets/icons/kami.png");
            sendChatMessage(DiscordNotifs.INSTANCE.getChatName() + " Reset Avatar!");
        }
    }
}
