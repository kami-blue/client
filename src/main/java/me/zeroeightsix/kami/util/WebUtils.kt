package me.zeroeightsix.kami.util

import com.google.gson.Gson
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage
import java.awt.Desktop
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Created by Dewy on 09/04/2020
 * Updated by d1gress/Qther on 13 April 2020
 */
object WebUtils {
    @JvmStatic
    fun openWebLink(url: URI) {
        try {
            Desktop.getDesktop().browse(url)
        } catch (e: IOException) {
            KamiMod.log.error("Couldn't open link: $url")
        }
    }

    @JvmStatic
    fun getContributors(chatMessage: Boolean = false): List<GithubUser> {
        return getContributors(emptySet<Int>() as HashSet<Int>, chatMessage)
    }

    @JvmStatic
    fun getContributors(exceptions: HashSet<Int>, chatMessage: Boolean = false): List<GithubUser> {
        // log attempt
        KamiMod.log.info("Attempting to get contributors from github api...")

        //initialize list
        val contributorList = ArrayList<GithubUser>()
        try {
            // connect to https://api.github.com/repos/kami-blue/client/contributors
            val connection = URL("https://api.github.com/repos/kami-blue/client/contributors").openConnection() as HttpsURLConnection
            connection.connect()

            // then parse it
            val contributors = Gson().fromJson(InputStreamReader(connection.inputStream), Array<GithubUser>::class.java)

            // disconnect from api
            connection.disconnect()

            // add contributors to the list
            for (githubUser in contributors) {
                if (exceptions.contains(githubUser.id)) continue
                contributorList.add(githubUser)
            }
        } catch (t: Throwable) {
            // throw error
            KamiMod.log.error("Attempt to get contributors from github api failed.\nError:\n\n$t")
            if (chatMessage) sendErrorMessage("Attempt to get contributors from github api failed.\nError:\n\n$t")
        }
        return contributorList
    }

    class GithubUser {
        @JvmField
        var login: String? = null
        var id = 0
        var contributions: String? = null
    }
}