/*
 * This file is adapted from 2b2t-Utilities/emoji-api which is licensed under MIT.
 * You can find a copy of the original license here: https://github.com/2b2t-Utilities/emoji-api/blob/35b0683/LICENSE
 */

package me.zeroeightsix.kami.manager.managers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.graphics.TextureUtils
import me.zeroeightsix.kami.util.threads.mainScope
import me.zeroeightsix.kami.util.threads.onMainThreadW
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.GL_RGBA
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
    private val emojiMap = HashMap<String, ResourceLocation?>()

    private val job = mainScope.launch(Dispatchers.IO) {
        val directory = File(directory)

        if (!directory.exists()) {
            directory.mkdir()
        }

        try {
            checkEmojiUpdate()
        } catch (e: Exception) {
            KamiMod.LOG.warn("Failed to check emoji update", e)
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

    fun getEmoji(name: String?): ResourceLocation? {
        if (name == null) return null

        // Returns null if still loading
        if (job.isActive) {
            return null
        }

        // Loads emoji on demand
        if (!emojiMap.containsKey(name)) {
            mainScope.launch(Dispatchers.IO) {
                loadEmoji(name)
            }
        }

        return emojiMap[name]
    }

    fun isEmoji(name: String?) = getEmoji(name) != null

    private fun loadEmoji(name: String) {
        val file = File("$directory/${name}.png")
        if (!file.exists()) return

        try {
            val image = ImageIO.read(file)

            onMainThreadW(5000L) {
                val dynamicTexture = TextureUtils.genTextureWithMipmaps(image, 3, GL_RGBA)
                val resourceLocation = Wrapper.minecraft.textureManager.getDynamicTextureLocation(name, dynamicTexture)
                emojiMap[name] = resourceLocation
            }
        } catch (e: IOException) {
            KamiMod.LOG.warn("Failed to load emoji", e)
        }
    }

}