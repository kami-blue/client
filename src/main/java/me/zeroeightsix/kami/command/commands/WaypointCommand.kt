package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.manager.managers.WaypointManager.Waypoint
import me.zeroeightsix.kami.module.modules.movement.AutoWalk
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.math.CoordinateConverter.asString
import me.zeroeightsix.kami.util.math.CoordinateConverter.bothConverted
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.util.math.BlockPos

class WaypointCommand : Command("waypoint", ChunkBuilder().append("command", true, EnumParser(arrayOf("add", "remove", "goto", "list", "clear", "stashes", "del", "help"))).build(), "wp", "pos") {
    private var confirmTime = 0L

    override fun call(args: Array<out String?>?) {
        if (args != null && args[0] != null) {
            when (args[0]!!.toLowerCase()) {
                "add" -> {
                    if (args[1] != null) {
                        if (args[2] != null) {
                            if (!args[2]!!.matches(Regex("[0-9-]+,[0-9-]+,[0-9-]+"))) {
                                MessageSendHelper.sendErrorMessage("You have to enter custom coordinates in the format of '&7x,y,z&f', for example '&7${getCommandPrefix()}waypoint add \"My Waypoint\" 400,60,-100&f', but you can also leave it blank to use the current coordinates")
                                return
                            }
                            val split = args[2]!!.split(",").toTypedArray()
                            val coordinate = BlockPos(split[0].toInt(), split[1].toInt(), split[2].toInt())
                            confirm(args[1]!!, WaypointManager.add(coordinate, args[1]!!).pos)
                        } else {
                            confirm(args[1]!!, WaypointManager.add(args[1]!!).pos)
                        }
                    } else {
                        confirm("Unnamed", WaypointManager.add("Unnamed").pos)
                    }
                }
                "remove" -> delete(args)
                "del" -> delete(args)
                "goto" -> {
                    if (args[1] != null) {
                        val waypoint = WaypointManager.get(args[1]!!)
                        if (waypoint != null) {
                            if (AutoWalk.isEnabled) AutoWalk.disable()
                            val pos = waypoint.currentPos()
                            MessageSendHelper.sendBaritoneCommand("goto", pos.x.toString(), pos.y.toString(), pos.z.toString())
                        } else {
                            MessageSendHelper.sendChatMessage("Couldn't find a waypoint with the ID " + args[1])
                        }
                    } else {
                        MessageSendHelper.sendChatMessage("Please provide the ID of a waypoint to go to. Use '&7${getCommandPrefix()}wp list&f' to list saved waypoints and their IDs")
                    }
                }
                "list" -> {
                    if (args[1] != null) {
                        searchWaypoints(args[1]!!)
                    } else {
                        listWaypoints(false)
                    }
                }
                "clear" -> {
                    if (System.currentTimeMillis() - confirmTime > 15000L) {
                        confirmTime = System.currentTimeMillis()
                        MessageSendHelper.sendChatMessage("This will delete ALL your waypoints, run '&7${commandPrefix.value}wp clear&f' again to confirm")
                    } else {
                        confirmTime = 0L
                        WaypointManager.clear()
                        MessageSendHelper.sendChatMessage("Waypoints have been &ccleared")
                    }
                }
                "stash" -> listWaypoints(true)
                "stashes" -> listWaypoints(true)
                "help" -> {
                    val p = getCommandPrefix()
                    MessageSendHelper.sendChatMessage("Waypoint command help\n\n" +
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
                else -> MessageSendHelper.sendErrorMessage("Please enter a valid argument!")
            }
        }
    }

    private fun listWaypoints(stashes: Boolean) {
        val waypoints = WaypointManager.waypoints
        if (waypoints.isEmpty()) {
            if (!stashes) {
                MessageSendHelper.sendChatMessage("No waypoints have been saved.")
            } else {
                MessageSendHelper.sendChatMessage("No stashes have been logged.")
            }
        } else {
            if (!stashes) {
                MessageSendHelper.sendChatMessage("List of waypoints:")
            } else {
                MessageSendHelper.sendChatMessage("List of logged stashes:")
            }
            val stashRegex = Regex("(\\(.* chests, .* shulkers, .* droppers, .* dispensers\\))")
            for (waypoint in WaypointManager.waypoints) {
                if (stashes) {
                    if (waypoint.name.matches(stashRegex)) {
                        MessageSendHelper.sendRawChatMessage(format(waypoint, ""))
                    }
                } else {
                    if (!waypoint.name.matches(stashRegex)) {
                        MessageSendHelper.sendRawChatMessage(format(waypoint, ""))
                    }
                }
            }
        }
    }

    private fun searchWaypoints(search: String) {
        var found = false
        var first = true

        for (waypoint in WaypointManager.waypoints) {
            if (waypoint.name.contains(search)) {
                if (first) {
                    MessageSendHelper.sendChatMessage("Result of search for &7$search&f: ")
                    first = false
                }
                MessageSendHelper.sendRawChatMessage(format(waypoint, search))
                found = true
            }
        }
        if (!found) {
            MessageSendHelper.sendChatMessage("No results for &7$search&f")
        }
    }

    private fun delete(args: Array<out String?>) {
        if (args[1] != null) {
            if (WaypointManager.remove(args[1]!!)) {
                MessageSendHelper.sendChatMessage("Removed waypoint with ID " + args[1])
            } else {
                MessageSendHelper.sendChatMessage("No waypoint with ID " + args[1])
            }
        } else {
            MessageSendHelper.sendErrorMessage("You must provide a waypoint ID delete a waypoint. Use '&7${commandPrefix.value}wp list&f' to list saved waypoints and their IDs")
        }
    }

    private fun format(waypoint: Waypoint, search: String): String {
        val message = "${formattedID(waypoint.id)} [${waypoint.server}] ${waypoint.name} (${bothConverted(waypoint.dimension, waypoint.pos)})"
        return message.replace(search.toRegex(), "&7$search&f")
    }

    private fun formattedID(id: Int): String { // massive meme to format the spaces for the width of id lmao
        return " ".repeat((5 - id.toString().length).coerceAtLeast(0)) + "[$id]"
    }

    private fun confirm(name: String, pos: BlockPos) {
        MessageSendHelper.sendChatMessage("Added waypoint at ${pos.asString()} in the ${InfoCalculator.dimension()} with name '&7$name&f'.")
    }

}