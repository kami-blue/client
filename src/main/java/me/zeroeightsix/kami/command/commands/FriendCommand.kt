package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.util.text.MessageSendHelper

/**
 * Created by 086 on 14/12/2017.
 * Updated by Xiaro on 14/08/20
 */
class FriendCommand : Command("friend", ChunkBuilder()
        .append("mode", true, EnumParser(arrayOf("is", "add", "del", "list", "toggle", "clear")))
        .append("name")
        .build(), "f") {
    private var confirmTime = 0L

    override fun call(args: Array<String?>) {
        val subCommand = getSubCommand(args)
        if (!FriendManager.enabled && subCommand != SubCommands.NULL && subCommand != SubCommands.TOGGLE) {
            MessageSendHelper.sendWarningMessage("&6Warning: Friends is disabled!")
            MessageSendHelper.sendWarningMessage("These commands will still have effect, but will not visibly do anything.")
        }
        when (subCommand) {
            SubCommands.IS_FRIEND -> {
                MessageSendHelper.sendChatMessage(String.format(
                        if (FriendManager.isFriend(args[1]!!)) "Yes, %s is your friend."
                        else "No, %s isn't a friend of yours.",
                        args[1]))
            }

            SubCommands.ADD -> {
                if (FriendManager.isFriend(args[1]!!)) {
                    MessageSendHelper.sendChatMessage("That player is already your friend.")
                } else {
                    // New thread because of potential internet connection made
                    Thread {
                        if (FriendManager.addFriend(args[1]!!)) {
                            MessageSendHelper.sendChatMessage("&7${args[1]}&r has been friended.")
                        } else {
                            MessageSendHelper.sendChatMessage("Failed to find UUID of ${args[1]}")
                        }
                    }.start()
                }
            }

            SubCommands.DEL -> {
                if (FriendManager.removeFriend(args[1]!!)) MessageSendHelper.sendChatMessage("&7${args[1]}&r has been unfriended.")
                else MessageSendHelper.sendChatMessage("That player isn't your friend.")
            }

            SubCommands.LIST -> {
                if (FriendManager.empty) {
                    MessageSendHelper.sendChatMessage("You currently don't have any friends added. run &7${commandPrefix.value}friend add <name>&r to add one.")
                } else {
                    val f = FriendManager.friends.values.joinToString(prefix = "\n    ", separator = "\n    ") { it.name } // nicely format the chat output
                    MessageSendHelper.sendChatMessage("Your friends: $f")
                }
            }

            SubCommands.TOGGLE -> {
                FriendManager.enabled = !FriendManager.enabled
                if (FriendManager.enabled) {
                    MessageSendHelper.sendChatMessage("Friends have been &aenabled")
                } else {
                    MessageSendHelper.sendChatMessage("Friends have been &cdisabled")
                }
            }

            SubCommands.CLEAR -> {
                if (System.currentTimeMillis() - confirmTime > 15000L) {
                    confirmTime = System.currentTimeMillis()
                    MessageSendHelper.sendChatMessage("This will delete ALL your friends, run &7${commandPrefix.value}friend clear&f again to confirm")
                } else {
                    confirmTime = 0L
                    FriendManager.clearFriend()
                    MessageSendHelper.sendChatMessage("Friends have been &ccleared")
                }
            }

            SubCommands.NULL -> {
                val commands = args.joinToString(separator = " ")
                MessageSendHelper.sendChatMessage("Invalid command &7${commandPrefix.value}${label} $commands&f!")
            }
        }
    }

    private fun getSubCommand(args: Array<String?>): SubCommands {
        return when {
            args[0].isNullOrBlank() || args[0]?.equals("list", ignoreCase = true) == true -> SubCommands.LIST

            args[0].equals("toggle", ignoreCase = true) -> SubCommands.TOGGLE

            args[0].equals("clear", ignoreCase = true) -> SubCommands.CLEAR

            args[1].isNullOrBlank() -> SubCommands.NULL // All the commands below requires a name, so add the check here

            args[0].equals("is", ignoreCase = true) -> SubCommands.IS_FRIEND

            args[0].equals("add", ignoreCase = true)
                    || args[0].equals("new", ignoreCase = true) -> SubCommands.ADD

            args[0].equals("del", ignoreCase = true)
                    || args[0].equals("remove", ignoreCase = true)
                    || args[0].equals("delete", ignoreCase = true) -> SubCommands.DEL

            else -> SubCommands.NULL
        }
    }

    private enum class SubCommands {
        ADD, DEL, LIST, IS_FRIEND, TOGGLE, CLEAR, NULL
    }

    init {
        setDescription("Add someone as your friend!")
    }
}