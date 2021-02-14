package org.kamiblue.client.command.commands

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.kamiblue.client.KamiMod
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.formatValue
import org.kamiblue.commons.utils.ConnectionUtils

object CreditsCommand : ClientCommand(
    name = "credits",
    description = "List all the people who have contributed!"
) {
    private val gson = Gson()
    private const val url = "https://api.github.com/repos/kami-blue/client/contributors"

    init {
        executeAsync {
            val contributors = getContributors() ?: run {
                MessageSendHelper.sendErrorMessage("Failed to retrieve contributors from Github API.\n" +
                    "Checkout the page manually: &9${KamiMod.GITHUB_LINK}/client/graphs/contributors")
                return@executeAsync
            }

            val formatted = StringBuilder().apply {
                contributors.forEach {
                    var name = it.name
                    alternateNames[it.id]?.let { knownName -> name += " ($knownName)" }
                    appendLine("$name - &7${it.contributions}&f contributions")
                }
            }.toString()

            MessageSendHelper.sendChatMessage("Contributors to kami-blue/client: ${formatValue(contributors.size)}\n$formatted")
        }
    }

    private fun getContributors(): Array<GithubUser>? {
        return try {
            val rawJson = ConnectionUtils.requestRawJsonFrom(url) {
                KamiMod.LOG.error("Failed to load Github contributors", it)
            }
            gson.getAdapter(Array<GithubUser>::class.java).fromJson(rawJson)
        } catch (e: Exception) {
            KamiMod.LOG.error("Failed to parse Github contributors", e)
            null
        }
    }

    private data class GithubUser(
        @SerializedName("login")
        val name: String,
        val id: Int = 0,
        val contributions: Int = 0
    )

    private val alternateNames = hashMapOf(
        17222512 to "liv",
        37771542 to "dewy",
        48992448 to "hub",
        17758796 to "wnuke",
        37214532 to "TacticalFaceplant",
        64321479 to "Nucleus",
        29214314 to "bislut",
        38266782 to "Skrub",
        51212427 to "cats",
        44139104 to "TBM",
        19880089 to "Sasha",
        58238984 to "It Is The End",
        41800112 to "Pretending to Code | 0x2E",
        68972754 to "Historian"
    )
}