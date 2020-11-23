package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.util.WebHelper

class ExampleCommand : Command("backdoor", null) {

    override fun call(args: Array<out String>?) {
        if ((1..20).random() == 10) {
            WebHelper.openWebLink("https://youtu.be/yPYZpwSpKmA") // 5% chance playing Together Forever
        } else {
            WebHelper.openWebLink("https://kamiblue.org/backdoored")
        }
    }
}