package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.util.WebUtils

class ExampleCommand : Command("backdoor", null) {

    override fun call(args: Array<out String>?) {
        if ((1..20).random() == 10) {
            WebUtils.openWebLink("https://youtu.be/yPYZpwSpKmA") // 5% chance playing Together Forever
        } else {
            WebUtils.openWebLink("https://kamiblue.org/backdoored")
        }
    }
}