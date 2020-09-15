package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.manager.mangers.FileInstanceManager
import me.zeroeightsix.kami.module.modules.movement.AutoWalk
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.Waypoint.createWaypoint
import me.zeroeightsix.kami.util.Waypoint.getWaypoint
import me.zeroeightsix.kami.util.Waypoint.removeWaypoint
import me.zeroeightsix.kami.util.Waypoint.writePlayerCoords
import me.zeroeightsix.kami.util.WaypointInfo
import me.zeroeightsix.kami.util.math.CoordinateConverter.asString
import me.zeroeightsix.kami.util.math.CoordinateConverter.bothConverted
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage
import net.minecraft.util.math.BlockPos
import java.util.function.Consumer

/**
 * @author dominikaaaa
 * @since 31/07/20 21:07
 *
 * Thanks to wnuke who wrote the original command
 */
class WaypointCommand : Command("waypoint", ChunkBuilder().append("command", true, EnumParser(arrayOf("add", "remove", "goto", "list", "stashes", "del", "help"))).build(), "wp", "pos") {
    override fun call(args: Array<out String?>?) {
        if (args != null && args[0] != null) {
            when (args[0]!!.toLowerCase()) {
                "add" -> {
                    if (args[1] != null) {
                        if (args[2] != null) {
                            if (!args[2]!!.matches(Regex("[0-9-]+,[0-9-]+,[0-9-]+"))) {
                                sendErrorMessage("You have to enter custom coordinates in the format of '&7x,y,z&f', for example '&7${getCommandPrefix()}waypoint add \"My Waypoint\" 400,60,-100&f', but you can also leave it blank to use the current coordinates")
                                return
                            }
                            val split = args[2]!!.split(",").toTypedArray()
                            val coordinate = BlockPos(split[0].toInt(), split[1].toInt(), split[2].toInt())
                            confirm(args[1]!!, createWaypoint(coordinate, args[1]!!))
                        } else {
                            confirm(args[1]!!, writePlayerCoords(args[1]!!))
                        }
                    } else {
                        confirm("Unnamed", writePlayerCoords("Unnamed"))
                    }
                }
                "remove" -> delete(args)
                "del" -> delete(args)
                "goto" -> {
                    if (args[1] != null) {
                        val current = getWaypoint(args[1]!!, true)
                        if (current != null) {
                            if (AutoWalk.isEnabled) AutoWalk.disable()
                            MessageSendHelper.sendBaritoneCommand("goto", current.x.toString(), current.y.toString(), current.z.toString())
                        } else {
                            sendChatMessage("Couldn't find a waypoint with the ID " + args[1])
                        }
                    } else {
                        sendChatMessage("Please provide the ID of a waypoint to go to. Use '&7${getCommandPrefix()}wp list&f' to list saved waypoints and their IDs")
                    }
                }
                "list" -> {
                    if (args[1] != null) {
                        searchWaypoints(args[1]!!)
                    } else {
                        listWaypoints(false)
                    }
                }
                "stash" -> listWaypoints(true)
                "stashes" -> listWaypoints(true)
                "help" -> {
                    val p = getCommandPrefix()
                    sendChatMessage("Waypoint command help\n\n" +
                            "    &7add&f <name> <coord>\n" +
                            "        &7${p}wp add\n" +
                            "        &7${p}wp add Test 420,120,-1024\n\n" +
                            "    &7remove&f <id>\n" +
                            "        &7${p}wp remove 23\n\n" +
                            "    &7goto&f <id>\n" +
                            "        &7${p}wp goto 22\n\n" +
                            "    &7list&f <filter>\n" +
                            "        &7${p}wp list\n" +
                            "        &7${p}wp list Logout\n\n" +
                            "    &7stashes&f\n" +
                            "        &7${p}wp stashes")
                }
                else -> sendErrorMessage("Please enter a valid argument!")
            }
        }
    }

    private fun listWaypoints(stashes: Boolean) {
        val waypoints = FileInstanceManager.waypoints
        if (waypoints.isEmpty()) {
            if (!stashes) {
                sendChatMessage("No waypoints have been saved.")
            } else {
                sendChatMessage("No stashes have been logged.")
            }
        } else {
            if (!stashes) {
                sendChatMessage("List of waypoints:")
            } else {
                sendChatMessage("List of logged stashes:")
            }
            val stashRegex = Regex("(\\(.* chests, .* shulkers, .* droppers, .* dispensers\\))")
            waypoints.forEach(Consumer { waypoint: WaypointInfo ->
                if (stashes) {
                    if (waypoint.name.matches(stashRegex)) {
                        MessageSendHelper.sendRawChatMessage(format(waypoint, ""))
                    }
                } else {
                    if (!waypoint.name.matches(stashRegex)) {
                        MessageSendHelper.sendRawChatMessage(format(waypoint, ""))
                    }
                }
            })
        }
    }

    private fun searchWaypoints(search: String) {
        var found = false
        var first = true

        for (waypoint in FileInstanceManager.waypoints) {
            if (waypoint.name.contains(search)) {
                if (first) {
                    sendChatMessage("Result of search for &7$search&f: ")
                    first = false
                }
                MessageSendHelper.sendRawChatMessage(format(waypoint, search))
                found = true
            }
        }
        if (!found) {
            sendChatMessage("No results for &7$search&f")
        }
    }

    private fun delete(args: Array<out String?>) {
        if (args[1] != null) {
            if (removeWaypoint(args[1]!!)) {
                sendChatMessage("Removed waypoint with ID " + args[1])
            } else {
                sendChatMessage("No waypoint with ID " + args[1])
            }
        } else {
            sendErrorMessage("You must provide a waypoint ID delete a waypoint. Use '&7${getCommandPrefix()}wp list&f' to list saved waypoints and their IDs")
        }
    }

    private fun format(waypoint: WaypointInfo, search: String): String {
        val message = "${formattedID(waypoint.id)} [${waypoint.server}] ${waypoint.name} (${bothConverted(waypoint.dimension, waypoint.pos)})"
        return message.replace(search.toRegex(), "&7$search&f")
    }

    private fun formattedID(id: Int): String { // massive meme to format the spaces for the width of id lmao
        return " ".repeat((5 - id.toString().length).coerceAtLeast(0)) + "[$id]"
    }

    private fun confirm(name: String, pos: BlockPos) {
        sendChatMessage("Added waypoint at ${pos.asString()} in the ${InfoCalculator.dimension()} with name '&7$name&f'.")
    }

}