package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.util.text.MessageSendHelper

object FriendCommand : ClientCommand(
    name = "friend",
    alias = arrayOf("f"),
    description = "Add someone as your friend!"
) {

    private var confirmTime = 0L

    init {
        literal("add", "new", "+") {
            player("player") { playerArg ->
                execute {
                    val name = playerArg.value.name
                    if (FriendManager.isFriend(name)) {
                        MessageSendHelper.sendChatMessage("That player is already your friend.")
                    } else {
                        if (FriendManager.addFriend(name)) {
                            MessageSendHelper.sendChatMessage("&7${name}&r has been friended.")
                        } else {
                            MessageSendHelper.sendChatMessage("Failed to find UUID of $name")
                        }
                    }
                }
            }
        }

        literal("del", "remove", "-") {
            player("player") { playerArg ->
                execute {
                    val name = playerArg.value.name
                    if (FriendManager.removeFriend(name)) MessageSendHelper.sendChatMessage("&7${name}&r has been unfriended.")
                    else MessageSendHelper.sendChatMessage("That player isn't your friend.")
                }
            }
        }

        literal("toggle") {
            execute {
                FriendManager.enabled = !FriendManager.enabled
                if (FriendManager.enabled) {
                    MessageSendHelper.sendChatMessage("Friends have been &aenabled")
                } else {
                    MessageSendHelper.sendChatMessage("Friends have been &cdisabled")
                }
            }
        }

        literal("clear") {
            execute {
                if (System.currentTimeMillis() - confirmTime > 15000L) {
                    confirmTime = System.currentTimeMillis()
                    MessageSendHelper.sendChatMessage("This will delete ALL your friends, run &7${prefix}friend clear&f again to confirm")
                } else {
                    confirmTime = 0L
                    FriendManager.clearFriend()
                    MessageSendHelper.sendChatMessage("Friends have been &ccleared")
                }
            }
        }

        literal("is") {
            player("player") { playerArg ->
                execute {
                    isFriend(playerArg.value.name)
                }
            }
        }

        player("player") { playerArg ->
            execute {
                isFriend(playerArg.value.name)
            }
        }

        literal("list") {
            execute {
                listFriends()
            }
        }

        execute {
            listFriends()
        }
    }

    private fun isFriend(name: String) {
        val string = if (FriendManager.isFriend(name)) "Yes, $name is your friend."
        else "No, $name isn't a friend of yours."
        MessageSendHelper.sendChatMessage(string)
    }

    private fun listFriends() {
        if (FriendManager.empty) {
            MessageSendHelper.sendChatMessage("You currently don't have any friends added. run &7${prefix}friend add <name>&r to add one.")
        } else {
            val f = FriendManager.friends.values.joinToString(prefix = "\n    ", separator = "\n    ") { it.name } // nicely format the chat output
            MessageSendHelper.sendChatMessage("Your friends: $f")
        }
    }
}