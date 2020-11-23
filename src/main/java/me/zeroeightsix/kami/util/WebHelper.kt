package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.KamiMod
import java.awt.Desktop
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.net.URL
import java.nio.channels.Channels

/**
 * @author balusc (StackOverflow ID 157882)
 *
 * https://stackoverflow.com/questions/3584210/preferred-java-way-to-ping-an-http-url-for-availability#3584332
 */
object WebHelper : Runnable {
    var isInternetDown = false

    fun isDown(host: String?, port: Int, timeout: Int): Boolean {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeout)
                return false
            }
        } catch (e: IOException) {
            return true // Either timeout or unreachable or failed DNS lookup.
        }
    }

    override fun run() {
        isInternetDown = isDown("1.1.1.1", 80, 100)
    }

    fun openWebLink(url: String) {
        try {
            Desktop.getDesktop().browse(URI(url))
        } catch (e: IOException) {
            KamiMod.log.error("Couldn't open link: $url")
        }
    }

    fun getUrlContents(_url: String): String {
        val content = StringBuilder()
        try {
            val url = URL(_url)
            val urlConnection = url.openConnection()
            val bufferedReader = BufferedReader(InputStreamReader(urlConnection.getInputStream()))
            var line: String?

            while (bufferedReader.readLine().also { line = it } != null) {
                content.append(line).append("\n")
            }
            bufferedReader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return content.toString()
    }

    @Throws(IOException::class)
    fun downloadUsingNIO(urlStr: String, file: String) {
        val url = URL(urlStr)
        val rbc = Channels.newChannel(url.openStream())
        val fos = FileOutputStream(file)
        fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
        fos.close()
        rbc.close()
    }
}