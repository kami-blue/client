/*
 * This file is adapted from 2b2t-Utilities/emoji-api which is licensed under MIT.
 * You can find a copy of the original license here: https://github.com/2b2t-Utilities/emoji-api/blob/35b0683/LICENSE
 */

package me.zeroeightsix.kami.manager.mangers

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.util.JsonUtils
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.io.*
import java.net.URL
import java.util.zip.ZipInputStream
import javax.imageio.ImageIO

object KamiMojiManager : Manager() {
    private const val FOLDER = "emojis"
    private const val VERSION_URL = "https://raw.githubusercontent.com/2b2t-Utilities/emojis/master/version.json"
    private val LOCAL_VERSION = File(FOLDER + File.separator + "version.json")
    private const val ZIP_URL = "https://github.com/2b2t-Utilities/emojis/archive/master.zip"

    private val KAMIMOJIS = HashMap<String, ResourceLocation?>()
    private val files = HashMap<String, File>()
    private val thread: Thread

    @Throws(IOException::class)
    private fun updateEmojis() {
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
    }

    private fun addEmoji(file: File) {
        try {
            val image = ImageIO.read(file)
            val dynamicTexture: DynamicTexture
            dynamicTexture = DynamicTexture(image)
            dynamicTexture.loadTexture(Wrapper.minecraft.getResourceManager())
            KAMIMOJIS[file.name.replace(".png".toRegex(), "")] = Wrapper.minecraft.textureManager.getDynamicTextureLocation(file.name.replace(".png".toRegex(), ""), dynamicTexture)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun getEmoji(emoji: Emoji): ResourceLocation? {
        // Loads emoji on demand
        if (!KAMIMOJIS.contains(emoji.name) && !thread.isAlive) {
            files[emoji.name]?.let { addEmoji(it) }
        }
        return KAMIMOJIS[emoji.name]
    }

    fun isEmoji(name: String?): Boolean {
        // Loads emoji on demand
        if (!KAMIMOJIS.contains(name) && !thread.isAlive) {
            files[name]?.let { addEmoji(it) }
        }
        return KAMIMOJIS.containsKey(name)
    }

    class Emoji(val name: String)

    init {
        thread = Thread({
            KamiMod.log.info("Initialising KamiMoji...")
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

            synchronized(files) {
                // Store the files in hash map, we are not loading them yet until we need them
                files.putAll(File(FOLDER).listFiles { file: File -> file.isFile && file.name.toLowerCase().endsWith(".png") }!!.associateBy { it.name.replace(".png".toRegex(), "") })
            }
        }, "KamiMoji Loading Thread")

        thread.start()
    }
}