package org.kamiblue.client.util

import org.kamiblue.client.KamiMod
import org.kamiblue.commons.utils.ConnectionUtils
import java.awt.Desktop
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import java.nio.channels.Channels

object WebUtils {
    fun openWebLink(url: String) {
        try {
            Desktop.getDesktop().browse(URI(url))
        } catch (e: IOException) {
            KamiMod.LOG.error("Couldn't open link: $url")
        }
    }

    fun getUrlContents(url: String): String {
        val content = StringBuilder()

        ConnectionUtils.runConnection(url, block = { connection ->
            val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
            bufferedReader.forEachLine { content.append("$it\n") }
        }, catch = {
            it.printStackTrace()
        })

        return content.toString()
    }

    @Throws(IOException::class)
    fun downloadUsingNIO(url: String, file: String) {
        Channels.newChannel(URL(url).openStream()).use { channel ->
            FileOutputStream(file).use {
                it.channel.transferFrom(channel, 0, Long.MAX_VALUE)
            }
        }
    }
}