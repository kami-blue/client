package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.util.text.MessageSendHelper

/**
 * @author dominikaaaa
 */
class BaritoneCommand : Command("baritone", ChunkBuilder().append("command", true, EnumParser(arrayOf("goto", "mine", "tunnel", "farm", "explore", "click", "build", "cancel", "pause", "resume", "help"))).build(), "b") {
    override fun call(args: Array<out String>?) {
        val newArgs = arrayOfNulls<String>(args!!.size - 1) // returns Array<String?>

        if (args.size - 1 >= 0) {
            System.arraycopy(args, 0, newArgs, 0, args.size - 1)
        }

        MessageSendHelper.sendBaritoneCommand(*newArgs)
    }
}