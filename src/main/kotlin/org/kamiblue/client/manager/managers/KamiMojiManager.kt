/*
 * This file is adapted from 2b2t-Utilities/emoji-api which is licensed under MIT.
 * You can find a copy of the original license here: https://github.com/2b2t-Utilities/emoji-api/blob/35b0683/LICENSE
 */

package org.kamiblue.client.manager.managers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kamiblue.client.KamiMod
import org.kamiblue.client.manager.Manager
import org.kamiblue.client.util.graphics.texture.MipmapTexture
import org.kamiblue.client.util.threads.defaultScope
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.zip.ZipInputStream
import javax.imageio.ImageIO

object KamiMojiManager : Manager {

    private const val directory = "${KamiMod.DIRECTORY}emojis"
    private const val versionURL = "https://raw.githubusercontent.com/2b2t-Utilities/emojis/master/version.json"
    private const val zipUrl = "https://github.com/2b2t-Utilities/emojis/archive/master.zip"

    private val parser = JsonParser()
    private val emojiMap = HashMap<String, MipmapTexture>()
    private val fileMap = HashMap<String, File>()

    private val job = defaultScope.launch(Dispatchers.IO) {
        val directory = File(directory)

        if (!directory.exists()) {
            directory.mkdir()
        }

        try {
            checkEmojiUpdate()
        } catch (e: Exception) {
            KamiMod.LOG.warn("Failed to check emoji update", e)
        }

        directory.listFiles()?.forEach {
            if (it.isFile && it.extension == "png") {
                fileMap[it.nameWithoutExtension] = it
            }
        }

        KamiMod.LOG.info("KamiMoji Initialized")
    }

    private fun checkEmojiUpdate() {
        val localVersion = File("$directory/version.json")

        if (!localVersion.exists()) {
            updateEmojis()
        } else {
            val globalVer = streamToJson(URL(versionURL).openStream())
            val localVer = streamToJson(FileInputStream(localVersion))

            if (globalVer != null) {
                if (!globalVer.has("version")) {
                    updateEmojis()
                } else if (globalVer["version"].asInt != localVer?.get("version")?.asInt ?: 8) {
                    updateEmojis()
                }
            }
        }
    }

    private fun streamToJson(stream: InputStream): JsonObject? {
        return try {
            parser.parse(stream.reader()).asJsonObject
        } catch (e: Exception) {
            KamiMod.LOG.warn("Failed to parse emoji version Json", e)
            null
        }
    }

    private fun updateEmojis() {
        val zip = ZipInputStream(URL(zipUrl).openStream())
        var entry = zip.nextEntry

        while (entry != null) {
            if (!entry.isDirectory) {
                val path = "$directory/${entry.name.substringAfterLast('/')}"
                File(path).apply {
                    if (!exists()) createNewFile()
                    writeBytes(zip.readBytes())
                }
            }

            zip.closeEntry()
            entry = zip.nextEntry
        }

        zip.close()
    }

    fun getEmoji(name: String?): MipmapTexture? {
        if (name == null) return null

        // Returns null if still loading
        if (job.isActive) {
            return null
        }

        // Loads emoji on demand
        if (!emojiMap.containsKey(name)) {
            loadEmoji(name)
        }

        return emojiMap[name]
    }

    fun isEmoji(name: String?) = getEmoji(name) != null

    private fun loadEmoji(name: String) {
        val file = fileMap[name] ?: return
        if (!file.exists()) return

        try {
            val image = ImageIO.read(file)
            val texture = MipmapTexture(image, GL_RGBA, 3)

            texture.bindTexture()
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, -0.5f)
            texture.unbindTexture()

            emojiMap[name] = texture
        } catch (e: IOException) {
            KamiMod.LOG.warn("Failed to load emoji", e)
        }
    }

}