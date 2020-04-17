package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.util.LogUtil;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

public class LogCoordsCommand extends Command {
    public LogCoordsCommand() {
        super("logpos", new ChunkBuilder()
                .append("name", false)
                .append("useChunkCoords", false)
                .build());
        setDescription("Log the current coordinates.");
    }
    public void call(String[] args) {
        String name = "Unnamed";
        if (args[0] != null) {
            name = args[0];
            confirm(name, LogUtil.writePlayerCoords(name, Boolean.parseBoolean(args[1])));
            return;
        } else {
            confirm(name, LogUtil.writePlayerCoords(name, false));
            return;
        }
    }
    private void confirm(String name, int[] xyz) {
        sendChatMessage("Added coordinate " + xyz[0] + " " + xyz[1] + " " + xyz[2] + " with name " + name + ".");
    }
}
