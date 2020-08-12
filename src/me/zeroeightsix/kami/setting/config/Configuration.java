// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.setting.config;

import me.zeroeightsix.kami.KamiMod;
import java.io.Reader;
import java.io.InputStreamReader;
import com.google.gson.JsonParser;
import java.io.InputStream;
import com.google.gson.Gson;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import com.google.gson.GsonBuilder;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Iterator;
import me.zeroeightsix.kami.setting.converter.Convertable;
import me.zeroeightsix.kami.setting.Setting;
import com.google.gson.JsonElement;
import java.util.Map;
import me.zeroeightsix.kami.setting.SettingsRegister;
import com.google.gson.JsonObject;

public class Configuration
{
    public static JsonObject produceConfig() {
        return produceConfig(SettingsRegister.ROOT);
    }
    
    private static JsonObject produceConfig(final SettingsRegister register) {
        final JsonObject object = new JsonObject();
        for (final Map.Entry<String, SettingsRegister> entry : register.registerHashMap.entrySet()) {
            object.add((String)entry.getKey(), (JsonElement)produceConfig(entry.getValue()));
        }
        for (final Map.Entry<String, Setting> entry2 : register.settingHashMap.entrySet()) {
            final Setting setting = entry2.getValue();
            if (!(setting instanceof Convertable)) {
                continue;
            }
            object.add((String)entry2.getKey(), (JsonElement)setting.converter().convert(setting.getValue()));
        }
        return object;
    }
    
    public static void saveConfiguration(final Path path) throws IOException {
        saveConfiguration(Files.newOutputStream(path, new OpenOption[0]));
    }
    
    public static void saveConfiguration(final OutputStream stream) throws IOException {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String json = gson.toJson((JsonElement)produceConfig());
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
        writer.write(json);
        writer.close();
    }
    
    public static void loadConfiguration(final Path path) throws IOException {
        final InputStream stream = Files.newInputStream(path, new OpenOption[0]);
        loadConfiguration(stream);
        stream.close();
    }
    
    public static void loadConfiguration(final InputStream stream) {
        try {
            loadConfiguration(new JsonParser().parse((Reader)new InputStreamReader(stream)).getAsJsonObject());
        }
        catch (IllegalStateException e) {
            KamiMod.log.error("KAMI Config malformed: resetting.");
            loadConfiguration(new JsonObject());
        }
    }
    
    public static void loadConfiguration(final JsonObject input) {
        loadConfiguration(SettingsRegister.ROOT, input);
    }
    
    private static void loadConfiguration(final SettingsRegister register, final JsonObject input) {
        for (final Map.Entry<String, JsonElement> entry : input.entrySet()) {
            final String key = entry.getKey();
            final JsonElement element = entry.getValue();
            if (register.registerHashMap.containsKey(key)) {
                loadConfiguration(register.subregister(key), element.getAsJsonObject());
            }
            else {
                final Setting setting = register.getSetting(key);
                if (setting == null) {
                    continue;
                }
                setting.setValue(setting.converter().reverse().convert((Object)element));
            }
        }
    }
}
