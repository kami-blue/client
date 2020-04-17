package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.util.Coord;
import me.zeroeightsix.kami.util.LogUtil;

import java.util.Objects;

import static me.zeroeightsix.kami.util.LogUtil.coordsLogToArray;
import static me.zeroeightsix.kami.util.LogUtil.writePlayerCoords;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendRawChatMessage;

public class CoordsCommand extends Command {
    public CoordsCommand() {
        super("pos", new ChunkBuilder()
                .append("save/list", true)
                .append("searchterm", false)
                .append("showDateTime", false)
                .build());
        setDescription("Log the current coordinates.");
    }
    public void call(String[] args) {
        if (args[0] != null) {
            if (args[0].equals("save")) {
                if (args[1] != null) {
                    confirm(args[1], writePlayerCoords(args[1], Boolean.parseBoolean(args[2])));
                } else {
                    confirm("Unnamed", writePlayerCoords("Unnamed", false));
                }
            } else if (args[0].equals("list")) {
                if (args[1] != null) {
                    searchCoords(args[1], Boolean.parseBoolean(args[2]));
                } else {
                    listCoords(false);
                }
            } else {
                sendChatMessage("Please use a valid command (list or save)");
            }
        } else {
            sendChatMessage("Please choose a command (list or save)");
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
    private void confirm(String name, int[] xyz) {
        sendChatMessage("Added coordinate " + xyz[0] + " " + xyz[1] + " " + xyz[2] + " with name " + name + ".");
    }
}