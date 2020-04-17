package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.util.Coord;

import static me.zeroeightsix.kami.util.LogUtil.coordsLogToArray;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendRawChatMessage;

public class ListCoordsCommand extends Command {
    public ListCoordsCommand() {
        super("listcoords", new ChunkBuilder()
                .build());
        setDescription("Log the current coordinates.");
    }
    public void call(String[] args) {
        listCoords();
    }
    private void confirm(String name, int[] xyz) {
        sendChatMessage("Added coordinate " + xyz[0] + " " + xyz[1] + " " + xyz[2] + " with name " + name + ".");
    }
    private void listCoords() {
        sendChatMessage("List of logged coordinates:");
        Coord[] coords = coordsLogToArray();
        for (Coord coord : coords) {
            String message = "   " + coord.name + " (" + coord.x + " " + coord.y + " " + coord.z + ") made at " + coord.time + " on " + coord.date;
            sendRawChatMessage(message);
        }
    }
}