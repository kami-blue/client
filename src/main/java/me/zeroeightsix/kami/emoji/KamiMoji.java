package me.zeroeightsix.kami.emoji;

import com.google.gson.JsonObject;
import me.zeroeightsix.kami.util.JsonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class KamiMoji {
    private static final String FOLDER = "kamimoji";

    private static final String VERSION_URL = "https://raw.githubusercontent.com/kami-blue/kamimoji/master/version.json";
    private static final File LOCAL_VERSION = new File(FOLDER + File.separator + "version.json");

    private static final String ZIP_URL = "https://github.com/kami-blue/kamimoji/archive/master.zip";

    private final Map<String, ResourceLocation> EMOJI_MAP = new HashMap<>();

    public void start() {
        File dir = new File(FOLDER);

        if (!dir.exists()) {
            dir.mkdir();
        }

        try {
            if (!LOCAL_VERSION.exists()) {
                updateEmojis();
            } else {
                JsonObject globalVer = JsonUtils.streamToJson(new URL(VERSION_URL).openStream());
                JsonObject localVer = JsonUtils.streamToJson(new FileInputStream(LOCAL_VERSION));

                if (!globalVer.has("version")) {
                    updateEmojis();
                } else {
                    if (globalVer.get("version").getAsInt() != localVer.get("version").getAsInt()) {
                        updateEmojis();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File[] emojis = new File(FOLDER).listFiles(file -> file.isFile() && file.getName().toLowerCase().endsWith(".png"));
        assert emojis != null;

        for (File emoji : emojis) {
            try {
                addEmoji(emoji);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateEmojis() throws IOException {
        ZipInputStream zip = new ZipInputStream(new URL(ZIP_URL).openStream());
        ZipEntry entry = zip.getNextEntry();

        while (entry != null) {
            String filePath = FOLDER + File.separator + entry.getName().substring(entry.getName().indexOf("/"));

            if (!entry.isDirectory()) {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));

                byte[] bytesIn = new byte[4096];
                int read;

                while ((read = zip.read(bytesIn)) != -1) {
                    bos.write(bytesIn, 0, read);
                }

                bos.close();
            }

            zip.closeEntry();

            entry = zip.getNextEntry();
        }

        zip.close();
    }

    public void addEmoji(File file) {
        DynamicTexture dynamicTexture;

        try {
            BufferedImage image = ImageIO.read(file);

            dynamicTexture = new DynamicTexture(image);
            dynamicTexture.loadTexture(Minecraft.getMinecraft().getResourceManager());
        } catch (IOException e) {
            e.printStackTrace();

            return;
        }

        EMOJI_MAP.put(file.getName().replaceAll(".png", ""), Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(file.getName().replaceAll(".png", ""), dynamicTexture));
    }

    public ResourceLocation getEmoji(Emoji emoji) {
        return EMOJI_MAP.get(emoji.getName());
    }

    public boolean isEmoji(String name) {
        return EMOJI_MAP.containsKey(name);
    }
}
