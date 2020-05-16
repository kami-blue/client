package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser;
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
        super("coord", new ChunkBuilder()
                .append("command", true, new EnumParser(new String[]{"add", "del", "list", "stashes", "help"}))
                .append("name", false)
                .build(), "pos");
        setDescription("Log the current coordinates.");
    }

    public void call(String[] args) {
        if (args[0] != null) {
            switch (args[0].toLowerCase()) {
                case "add":
                    if (args[1] != null) {
                        confirm(args[1], writePlayerCoords(args[1]));
                    } else {
                        confirm("Unnamed", writePlayerCoords("Unnamed"));
                    }
                    break;
                case "list":
                    if (args[1] != null) {
                        searchCoords(args[1]);
                    } else {
                        listCoords(false);
                    }
                    break;
                case "stashes":
                    listCoords(true);
                    break;
                case "del":
                    if (args[1] != null) {
                        if (removeCoord(args[1], coordsLogFilename)) {
                            sendChatMessage("Removed coordinate with name " + args[1]);
                        } else {
                            sendChatMessage("No coordinate with name " + args[1]);
                        }
                    } else {
                        sendChatMessage("Please provide the name of a coordinate to remove.");
                    }
                    break;
                case "help":
                    sendChatMessage("Coordinate logger help:");
                    sendRawChatMessage("  list &7[searchterm]&f - lists logged coordinates, optionally searches and filters the results");
                    sendRawChatMessage("  stashes - lists logged stashes");
                    sendRawChatMessage("  add &7[name]&f - logs a coordinate with an optional name");
                    sendRawChatMessage("  del <name> - removes a coordinate with the specified name");
                    sendRawChatMessage("  help - displays this list");
                    break;
                default:
                    sendChatMessage("Please use a valid command (add, del, list, stashes or help");
                    break;
            }
        } else {
            sendChatMessage("Please choose a command (list or save)");
        }
    }

    private void listCoords(boolean stashes) {
        ArrayList<CoordinateInfo> coords = readCoords(coordsLogFilename);
        if (coords.isEmpty()) {
            if (!stashes) {
                sendChatMessage("No coordinates have been logged.");
            } else {
                sendChatMessage("No stashes have been logged.");
            }
        } else {
            if (!stashes) {
                sendChatMessage("List of logged coordinates:");
            } else {
                sendChatMessage("List of logged stashes:");
            }
            String stashRegex = "(\\(.* chests, .* shulkers, .* droppers, .* dispensers\\))";
            Objects.requireNonNull(coords).forEach(coord -> {
                if (stashes) {
                    if (coord.name.matches(stashRegex)) {
                        sendRawChatMessage(format(coord, ""));
                    }
                } else {
                    if (!coord.name.matches(stashRegex)) {
                        sendRawChatMessage(format(coord, ""));
                    }
                }
            });
        }
    }
    private void searchCoords(String searchterm) {
        boolean hasfound = false;
        boolean firstfind = true;
        ArrayList<CoordinateInfo> coords = readCoords(coordsLogFilename);
        for (CoordinateInfo coord : Objects.requireNonNull(coords)) {
            if (coord.name.contains(searchterm)) {
                if (firstfind) {
                    sendChatMessage("Result of search for &7" + searchterm + "&f: ");
                    firstfind = false;
                }
                sendRawChatMessage(format(coord, searchterm));
                hasfound = true;
            }
        }
        if (!hasfound) {
            sendChatMessage("No results for " + searchterm);
        }
    }
    private String format(CoordinateInfo coord, String searchterm) {
        String message = "   [" + coord.id + "] " + coord.name + " (" + coord.xyz.x + " " + coord.xyz.y + " " + coord.xyz.z + ")";
        return message.replaceAll(searchterm, "&7" + searchterm + "&f");
    }
    private void confirm(String name, Coordinate xyz) {
        sendChatMessage("Added coordinate " + xyz.x + " " + xyz.y + " " + xyz.z + " with name " + name + ".");
    }
}