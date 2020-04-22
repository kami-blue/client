package me.zeroeightsix.kami.module.modules.capes;

import com.google.gson.Gson;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Crystallinqq
 * Updated by dominikaaaa on 20/12/19
 * Updated by 20kdc on 17/02/20 - changed implementation method, made a module again, made async
 * Updated by 20kdc on 21/02/20 - unbroke things, sorry!
 */
@Module.Info(
        name = "Capes",
        category = Module.Category.CLIENT,
        description = "Controls the display of KAMI Blue capes",
        showOnArray = Module.ShowOnArray.OFF
)
public class Capes extends Module {

    // This allows controlling if other capes (Mojang, OptiFine) should override the KAMI Blue cape.
    public Setting<Boolean> overrideOtherCapes = register(Settings.b("Override Other Capes", true));

    public static Capes INSTANCE;

    // This starts out null, and then is replaced from another thread if the Capes module is enabled.
    // It maps the UUIDs to CachedCape instances.
    // When it arrives here it must no longer be modified.
    private Map<String, CachedCape> allCapes = Collections.unmodifiableMap(new HashMap<>());

    private boolean hasBegunDownload = false;

    public Capes() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        // Begin the download if we haven't begun the download before and we're enabling the module.
        // This should reduce server requests, if nothing else...
        if (!hasBegunDownload) {
            hasBegunDownload = true;
            new Thread() {
                @Override
                public void run() {
                    try {
                        HttpsURLConnection connection = (HttpsURLConnection) new URL(KamiMod.CAPES_JSON).openConnection();
                        connection.connect();
                        CapeUser[] capeUser = new Gson().fromJson(new InputStreamReader(connection.getInputStream()), CapeUser[].class);
                        connection.disconnect();
                        // If we got this far, begin working out the cape details
                        // This first collection contains CachedCape instances by their URL to reduce redundant loading.
                        HashMap<String, CachedCape> capesByURL = new HashMap<>();
                        // This second collection maps UUIDs to their CachedCape instances.
                        HashMap<String, CachedCape> capesByUUID = new HashMap<>();
                        for (CapeUser cape : capeUser) {
                            CachedCape o = capesByURL.get(cape.url);
                            if (o == null) {
                                o = new CachedCape(cape);
                                capesByURL.put(cape.url, o);
                            }
                            capesByUUID.put(cape.uuid, o);
                        }
                        allCapes = Collections.unmodifiableMap(capesByUUID);
                    } catch (Exception e) {
                        KamiMod.log.error("Failed to load capes");
                        // e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    public static ResourceLocation getCapeResource(AbstractClientPlayer player) {
        CachedCape result = INSTANCE.allCapes.get(player.getUniqueID().toString());
        if (result == null)
            return null;
        result.request();
        return result.location;
    }

    private static BufferedImage parseCape(BufferedImage img)  {
        int imageWidth = 64;
        int imageHeight = 32;

        int srcWidth = img.getWidth();
        int srcHeight = img.getHeight();
        while (imageWidth < srcWidth || imageHeight < srcHeight) {
            imageWidth *= 2;
            imageHeight *= 2;
        }
        BufferedImage imgNew = new BufferedImage(imageWidth, imageHeight, 2);
        Graphics g = imgNew.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        return imgNew;
    }

    private static String formatUUID(String uuid) {
        return uuid.replaceAll("-", "");
    }

    // This is the raw Gson structure as seen in the assets
    public class CapeUser {
        public String uuid;
        public String url;
    }

    // This is the shared cape instance.
    private static class CachedCape {
        public final ResourceLocation location;
        public final String url;
        private boolean hasRequested = false;

        public CachedCape(CapeUser cape) {
            location = new ResourceLocation("capes/kami/" + formatUUID(cape.uuid));
            url = cape.url;
        }

        public void request() {
            if (hasRequested)
                return;
            hasRequested = true;
            // This is bindTexture moved to runtime (but still on the main thread)
            IImageBuffer iib = new IImageBuffer() {
                @Override
                public BufferedImage parseUserSkin(BufferedImage image) {
                    return parseCape(image);
                }

                @Override
                public void skinAvailable() {}
            };

            TextureManager textureManager = Wrapper.getMinecraft().getTextureManager();
            textureManager.getTexture(location);
            ThreadDownloadImageData textureCape = new ThreadDownloadImageData(null, url, null, iib);
            textureManager.loadTexture(location, textureCape);
        }
    }
}
