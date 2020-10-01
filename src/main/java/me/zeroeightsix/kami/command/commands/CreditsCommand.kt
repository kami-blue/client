package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.util.WebUtils.getContributors
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage

/**
 * Created by l1ving on 01/12/2019.
 * Revamped by d1gress/Qther on 13 April 2020
 * Updated by Xiaro on 21/08/20
 */
class CreditsCommand : Command("credits", null) {

    override fun call(args: Array<String>) {
        Thread { // We have to start a new thread here since the connection might block the main thread
            var message = localList
            val githubContributors = getContributors(exceptions, true)
            if (githubContributors.isNotEmpty()) {
                message += githubContributors.joinToString(prefix = "\n    ", separator = "\n    ") { githubUser ->
                    githubUser.login!!
                }
            }
            sendChatMessage(message)
        }.start()
    }

    companion object {
        const val localList: String =
                "\nName (Github if not same as name)" +
                        "\n&l&9Author:" +
                        "\n    086 (zeroeightysix)" +
                        "\n&l&9Contributors:" +
                        "\n    Dominika (l1ving)" +
                        "\n    hub (blockparole)" +
                        "\n    Dewy (iBuyMountainDew)" +
                        "\n    Sasha (EmotionalLove)" +
                        "\n    Qther (d1gress / Vonr)" +
                        "\n    cats (Cuhnt)" +
                        "\n    Jack (jacksonellsworth03)" +
                        "\n    TheBritishMidget (TBM)" +
                        "\n    Hamburger (Hamburger2k)" +
                        "\n    0x2E (PretendingToCode)" +
                        "\n    Battery Settings (Bluskript)" +
                        "\n    An-En (AnotherEntity)" +
                        "\n    Arisa (Arisa-Snowbell)" +
                        "\n    Jamie (jamie27)" +
                        "\n    Waizy (WaizyNet)" +
                        "\n    It is the end (Itistheend)" +
                        "\n    Robeart (Ropro2002/Potentia-Public)" +
                        "\n    Xiaro"

        val exceptions = hashSetOf(
                17222512,  // l1ving / dominika
                27009727,  // zeroeightysix
                48992448,  // blockparole
                37771542,  // iBuyMountainDew
                19880089,  // EmotionalLove
                55198830,  // d1gress
                24369412,  // Vonr
                51212427,  // Cuhnt
                11698651,  // jacksonellworth03
                44139104,  // TheBritishMidget
                59456376,  // Hamburger2k
                41800112,  // PretendingToCode
                52386117,  // Bluskript
                26636167,  // AnotherEntity
                22961592,  // ArisaSnowbell
                58238984,  // Itistheend
                32800833,  // Ropro2002
                62033805,  // Xiaro
                // Bots
                27856297 // dependabot
        )
    }

    init {
        setDescription("Prints KAMI Blue's authors and contributors")
    }
}