package me.zeroeightsix.kami.setting.config;

import com.google.gson.*;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.SettingsRegister;
import me.zeroeightsix.kami.setting.converter.Convertable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Created by 086 on 13/10/2018.
 */
public class Configuration {

    public static JsonObject produceConfig() {
        return produceConfig(SettingsRegister.ROOT);
    }

    private static JsonObject produceConfig(SettingsRegister register) {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, SettingsRegister> entry : register.registerHashMap.entrySet()) {
            object.add(entry.getKey(), produceConfig(entry.getValue()));
        }
        for (Map.Entry<String, Setting> entry : register.settingHashMap.entrySet()) {
            Setting setting = entry.getValue();
            if (!(setting instanceof Convertable)) continue;
            object.add(entry.getKey(), (JsonElement) ((Convertable) setting).converter().convert(setting.getValue()));
        }
        return object;
    }

    public static void saveConfiguration(Path path) throws IOException {
        saveConfiguration(Files.newOutputStream(path));
    }

    public static void saveConfiguration(OutputStream stream) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(produceConfig());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
        writer.write(json);
        writer.close();
    }

    public static void loadConfiguration(Path path) throws IOException {
        InputStream stream = Files.newInputStream(path);
        loadConfiguration(stream);
        stream.close();
    }

    public static void loadConfiguration(InputStream stream) {
        try {
            loadConfiguration(new JsonParser().parse(new InputStreamReader(stream)).getAsJsonObject());
        } catch (IllegalStateException e) { // The JSON file is probably malformed.
            KamiMod.LOG.error("KAMI Config malformed: resetting.");
            e.printStackTrace();
            loadConfiguration(new JsonObject()); // Just reset it!
        }
    }

    public static void loadConfiguration(JsonObject input) {
        loadConfiguration(SettingsRegister.ROOT, input);
    }

    private static void loadConfiguration(SettingsRegister register, JsonObject input) {
        for (Map.Entry<String, JsonElement> entry : input.entrySet()) {
            String key = entry.getKey();
            JsonElement element = entry.getValue();
            if (register.registerHashMap.containsKey(key)) {
                loadConfiguration(register.subregister(key), element.getAsJsonObject());
            } else {
                Setting setting = register.getSetting(key);
                if (setting == null) continue;
                setting.setValue(setting.converter().reverse().convert(element));
            }
        }
    }

}
