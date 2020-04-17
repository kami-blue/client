package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.util.Coord;

import java.util.Objects;

import static me.zeroeightsix.kami.util.LogUtil.coordsLogToArray;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendRawChatMessage;

public class ListCoordsCommand extends Command {
    public ListCoordsCommand() {
        super("listpos", new ChunkBuilder()
                .append("searchterm", false)
                .append("showDateTime", false)
                .build());
        setDescription("Log the current coordinates.");
    }
    public void call(String[] args) {
        if (args[0] != null) {
            if (args[1] == null) {
                searchCoords(args[0], false);
            } else {
                searchCoords(args[0], Boolean.parseBoolean(args[1]));
            }
        } else {
            listCoords(false);
        }
    }
    private void listCoords(boolean showDateTime) {
        sendChatMessage("List of logged coordinates:");
        Coord[] coords = coordsLogToArray();
        for (Coord coord : Objects.requireNonNull(coords)) {
            sendRawChatMessage(format(coord, showDateTime));
        }
    }
    private void searchCoords(String searchterm, boolean showDateTime) {
        boolean hasfound = false;
        boolean firstfind = true;
        Coord[] coords = coordsLogToArray();
        for (Coord coord : Objects.requireNonNull(coords)) {
            if (coord.name.contains(searchterm)) {
                if (firstfind) {
                    sendChatMessage("Result of search for " + searchterm + ": ");
                    firstfind = false;
                }
                sendRawChatMessage(format(coord, showDateTime));
                hasfound = true;
            }
        }
        if (!hasfound) {
            sendChatMessage("No results for " + searchterm);
        }
    }
    private String format(Coord coord, boolean showDateTime) {
        String message = "   " + coord.name + " (" + coord.x + " " + coord.y + " " + coord.z + ")";
        if (showDateTime) {
            message += " made at " + coord.time + " on " + coord.date;
        }
        return message;
    }
}