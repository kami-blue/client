package me.zeroeightsix.kami.command.commands

import com.google.gson.Gson
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.util.text.MessageSendHelper
import org.kamiblue.commons.utils.ConnectionUtils
import org.kamiblue.commons.utils.ThreadUtils

/**
 * Created by l1ving on 01/12/2019.
 * Revamped by d1gress/Qther on 13 April 2020
 * Updated by Xiaro on 21/08/20
 */
class CreditsCommand : Command("credits", null) {

    private val gson = Gson()
    private val thread = Thread { sendMessage() }

    override fun call(args: Array<String>) {
        // We have to start a new thread here since the connection might block the main thread
        ThreadUtils.submitTask(thread)
    }

    private fun sendMessage() {
        val list = getContributors()
        StringBuilder(list?.size ?: 0 + 1).run {
            append(localList)
            list?.let {
                for (user in it) {
                    if (user.login == null || user.id == null) continue
                    if (exceptions.contains(user.id)) continue
                    append("\n    ${user.login}")
                }
            }
            MessageSendHelper.sendChatMessage(toString())
        }
    }

    private fun getContributors(): Array<GithubUser>? {
        return try {
            val rawJson = ConnectionUtils.requestRawJsonFrom(url) {
                MessageSendHelper.sendErrorMessage("Attempt to get contributors from github api failed.\nError:\n\n$it")
            }
            gson.getAdapter(Array<GithubUser>::class.java).fromJson(rawJson)
        } catch (e: Exception) {
            MessageSendHelper.sendErrorMessage("Failed parsing contributor list")
            null
        }
    }

    private data class GithubUser(
        val login: String? = null,
        val id: Int? = 0
    )

    private companion object {
        const val url = "https://api.github.com/repos/kami-blue/client/contributors"

        const val localList: String =
            "Name (Github if not same as name)" +
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