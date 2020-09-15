package me.zeroeightsix.kami.module.modules.capes

import com.google.gson.Gson
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.IImageBuffer
import net.minecraft.client.renderer.ThreadDownloadImageData
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

/**
 * @author Crystallinqq
 * Updated by dominikaaaa on 20/12/19
 * Updated by 20kdc on 17/02/20 - changed implementation method, made a module again, made async
 * Updated by 20kdc on 21/02/20 - unbroke things, sorry!
 * Updated by Xiaro on 11/09/20
 */
@Module.Info(
        name = "Capes",
        category = Module.Category.CLIENT,
        description = "Controls the display of KAMI Blue capes",
        showOnArray = Module.ShowOnArray.OFF,
        enabledByDefault = true
)
class Capes : Module() {
    // This allows controlling if other capes (Mojang, OptiFine) should override the KAMI Blue cape.
    @JvmField
    val overrideOtherCapes = register(Settings.b("Override Other Capes", true))

    // This starts out null, and then is replaced from another thread if the Capes module is enabled.
    // It maps the UUIDs to CachedCape instances.
    // When it arrives here it must no longer be modified.
    private var allCapes = Collections.unmodifiableMap(HashMap<String, CachedCape>())
    private var hasBegunDownload = false

    public override fun onEnable() {
        // Begin the download if we haven't begun the download before and we're enabling the module.
        // This should reduce server requests, if nothing else...
        if (!hasBegunDownload) {
            hasBegunDownload = true
            Thread {
                try {
                    val connection = URL(KamiMod.CAPES_JSON).openConnection() as HttpsURLConnection
                    connection.connect()
                    val capeUser = Gson().fromJson(InputStreamReader(connection.inputStream), Array<CapeUser>::class.java)
                    connection.disconnect()
                    // If we got this far, begin working out the cape details
                    // This first collection contains CachedCape instances by their URL to reduce redundant loading.
                    val capesByURL = HashMap<String?, CachedCape>()
                    // This second collection maps UUIDs to their CachedCape instances.
                    val capesByUUID = HashMap<String?, CachedCape>()
                    for (cape in capeUser) {
                        var o = capesByURL[cape.url]
                        if (o == null) {
                            o = CachedCape(cape)
                            capesByURL[cape.url] = o
                        }
                        capesByUUID[cape.uuid] = o
                    }
                    allCapes = Collections.unmodifiableMap(capesByUUID)
                } catch (e: Exception) {
                    KamiMod.log.error("Failed to load capes")
                    // e.printStackTrace();
                }
            }.start()
        }
    }

    // This is the raw Gson structure as seen in the assets
    private class CapeUser(val uuid: String? = null, var url: String? = null)

    // This is the shared cape instance.
    private class CachedCape(cape: CapeUser) {
        val location: ResourceLocation = ResourceLocation("capes/kami/" + formatUUID(cape.uuid))
        private val url: String = cape.url.toString()
        private var hasRequested = false

        fun request() {
            if (hasRequested) return
            hasRequested = true
            // This is bindTexture moved to runtime (but still on the main thread)
            val iib: IImageBuffer = object : IImageBuffer {
                override fun parseUserSkin(image: BufferedImage): BufferedImage {
                    return parseCape(image)
                }

                override fun skinAvailable() {}
            }
            val textureManager = mc.textureManager
            textureManager.getTexture(location)
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") // Big IDE meme
            val textureCape = ThreadDownloadImageData(null, url, null, iib)
            textureManager.loadTexture(location, textureCape)
        }

        private fun parseCape(img: BufferedImage): BufferedImage {
            var imageWidth = 64
            var imageHeight = 32
            val srcWidth = img.width
            val srcHeight = img.height
            while (imageWidth < srcWidth || imageHeight < srcHeight) {
                imageWidth *= 2
                imageHeight *= 2
            }
            val imgNew = BufferedImage(imageWidth, imageHeight, 2)
            val g = imgNew.graphics
            g.drawImage(img, 0, 0, null)
            g.dispose()
            return imgNew
        }
    }

    companion object {
        @JvmField var INSTANCE: Capes? = null

        @JvmStatic
        fun getCapeResource(player: AbstractClientPlayer): ResourceLocation? {
            val result = INSTANCE?.allCapes?.get(player.uniqueID.toString()) ?: return null
            result.request()
            return result.location
        }

        private fun formatUUID(uuid: String?): String {
            return uuid!!.replace("-".toRegex(), "")
        }
    }

    init {
        INSTANCE = this
    }
}