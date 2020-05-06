package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.util.Coordinate;
import me.zeroeightsix.kami.util.CoordinateInfo;

import java.util.ArrayList;
import java.util.Objects;

import static me.zeroeightsix.kami.util.CoordUtil.*;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendRawChatMessage;

/**
 * @author wnuke
 * Created by wnuke on 17/04/20
 */

public class CoordsCommand extends Command {
    public CoordsCommand() {
        super("pos", new ChunkBuilder()
                .append("save|list|stashes", true)
                .append("name|searchterm", false)
                .append("showDateTime", false)
                .build());
        setDescription("Log the current coordinates.");
    }

    public void call(String[] args) {
        if (args[0] != null) {
            switch (args[0]) {
                case "save":
                    if (args[1] != null) {
                        confirm(args[1], writePlayerCoords(args[1]));
                    } else {
                        confirm("Unnamed", writePlayerCoords("Unnamed"));
                    }
                    break;
                case "list":
                    if (args[1] != null) {
                        searchCoords(args[1], Boolean.parseBoolean(args[2]));
                    } else {
                        listCoords(false, false);
                    }
                    break;
                case "stashes":
                    listCoords(false, true);
                    break;
                default:
                    sendChatMessage("Please use a valid command (list or save)");
                    break;
            }
        } else {
            sendChatMessage("Please choose a command (list or save)");
        }
    }

    private void listCoords(boolean showDateTime, boolean stashes) {
        sendChatMessage("List of logged coordinates:");
        String stashRegex = "(\\(.*chests, .* shulkers\\))";
        ArrayList<CoordinateInfo> coords = readCoords(coordsLogFilename);
        Objects.requireNonNull(coords).forEach(coord -> {
            if (stashes) {
                if (coord.name.matches(stashRegex)) {
                    sendRawChatMessage(format(coord, showDateTime));
                }
            } else {
                if (!coord.name.matches(stashRegex)) {
                    sendRawChatMessage(format(coord, showDateTime));
                }
            }
        });
    }
    private void searchCoords(String searchterm, boolean showDateTime) {
        boolean hasfound = false;
        boolean firstfind = true;
        ArrayList<CoordinateInfo> coords = readCoords(coordsLogFilename);
        for (CoordinateInfo coord : Objects.requireNonNull(coords)) {
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
    private String format(CoordinateInfo coord, boolean showDateTime) {
        String message = "   " + coord.name + " (" + coord.xyz.x + " " + coord.xyz.y + " " + coord.xyz.z + ")";
        if (showDateTime) {
            message += " made at " + coord.time + " on " + coord.date;
        }
        return message;
    }
    private void confirm(String name, Coordinate xyz) {
        sendChatMessage("Added coordinate " + xyz.x + " " + xyz.y + " " + xyz.z + " with name " + name + ".");
    }
}