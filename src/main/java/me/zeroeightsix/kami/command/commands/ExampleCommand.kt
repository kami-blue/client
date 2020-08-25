package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.util.WebUtils
import java.net.URI

class ExampleCommand : Command("backdoor", null) {

    override fun call(args: Array<out String>?) {
        if ((1..20).random() == 10) {
            WebUtils.openWebLink(URI("https://youtu.be/yPYZpwSpKmA")) // 5% chance playing Together Forever
        } else {
            WebUtils.openWebLink(URI("https://kamiblue.org/backdoored"))
        }
    }
}