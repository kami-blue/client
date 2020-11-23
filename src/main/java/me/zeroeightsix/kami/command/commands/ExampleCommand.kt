package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.util.WebHelper
import java.net.URI

class ExampleCommand : Command("backdoor", null) {

    override fun call(args: Array<out String>?) {
        if ((1..20).random() == 10) {
            WebHelper.openWebLink(URI("https://youtu.be/yPYZpwSpKmA")) // 5% chance playing Together Forever
        } else {
            WebHelper.openWebLink(URI("https://kamiblue.org/backdoored"))
        }
    }
}