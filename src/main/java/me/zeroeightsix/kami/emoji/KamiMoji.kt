/*
 * This file is adapted from 2b2t-Utilities/emoji-api which is licensed under MIT.
 * You can find a copy of the original license here: https://github.com/2b2t-Utilities/emoji-api/blob/35b0683/LICENSE
 */

package me.zeroeightsix.kami.emoji

import me.zeroeightsix.kami.util.JsonUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.io.*
import java.net.URL
import java.util.*
import java.util.zip.ZipInputStream
import javax.imageio.ImageIO

class KamiMoji {
    private val KAMIMOJIS: MutableMap<String, ResourceLocation?> = HashMap()

    fun start() {
        val dir = File(FOLDER)
        val kamiMojiDir = File("kamimoji")

        if (!dir.exists()) {
            dir.mkdir()
        }

        if (kamiMojiDir.exists()) {
            kamiMojiDir.deleteRecursively()
        }

        try {
            if (!LOCAL_VERSION.exists()) {
                updateEmojis()
            } else {
                val globalVer = JsonUtils.streamToJson(URL(VERSION_URL).openStream())
                val localVer = JsonUtils.streamToJson(FileInputStream(LOCAL_VERSION))

                if (globalVer != null) {
                    if (!globalVer.has("version")) {
                        updateEmojis()
                    } else {
                        if (globalVer["version"].asInt != localVer?.get("version")?.asInt ?: 8) {
                            updateEmojis()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val emojis = File(FOLDER).listFiles { file: File -> file.isFile && file.name.toLowerCase().endsWith(".png") }!!

        for (emoji in emojis) {
            try {
                addEmoji(emoji)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    fun updateEmojis() {
        Thread(Runnable {
            val zip = ZipInputStream(URL(ZIP_URL).openStream())
            var entry = zip.nextEntry

            while (entry != null) {
                val filePath = FOLDER + File.separator + entry.name.substring(entry.name.indexOf("/"))

                if (!entry.isDirectory) {
                    val bos = BufferedOutputStream(FileOutputStream(filePath))
                    val bytesIn = ByteArray(4096)
                    var read: Int

                    while (zip.read(bytesIn).also { read = it } != -1) {
                        bos.write(bytesIn, 0, read)
                    }

                    bos.close()
                }

                zip.closeEntry()

                entry = zip.nextEntry
            }

            zip.close()
        }).start()
    }

    fun addEmoji(file: File) {
        val dynamicTexture: DynamicTexture

        try {
            val image = ImageIO.read(file)

            dynamicTexture = DynamicTexture(image)
            dynamicTexture.loadTexture(Minecraft.getMinecraft().getResourceManager())
        } catch (e: IOException) {
            e.printStackTrace()

            return
        }

        KAMIMOJIS[file.name.replace(".png".toRegex(), "")] = Minecraft.getMinecraft().textureManager.getDynamicTextureLocation(file.name.replace(".png".toRegex(), ""), dynamicTexture)
    }

    fun getEmoji(emoji: Emoji): ResourceLocation? {
        return KAMIMOJIS[emoji.name]
    }

    fun isEmoji(name: String?): Boolean {
        return KAMIMOJIS.containsKey(name)
    }

    companion object {
        private const val FOLDER = "emojis"
        private const val VERSION_URL = "https://raw.githubusercontent.com/2b2t-Utilities/emojis/master/version.json"
        private val LOCAL_VERSION = File(FOLDER + File.separator + "version.json")
        private const val ZIP_URL = "https://github.com/2b2t-Utilities/emojis/archive/master.zip"
    }
}