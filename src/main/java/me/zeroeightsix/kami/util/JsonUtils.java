package me.zeroeightsix.kami.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.zeroeightsix.kami.KamiMod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.compress.utils.Charsets;

import java.io.IOException;
import java.io.InputStream;

public class JsonUtils {

    /**
     * Pop in a JSON input stream and get a JsonObject as output.
     *
     * @param in A JSON input stream ting
     * @return Corresponding JsonObject
     */
    public static JsonObject streamToJson(InputStream in) {
        Gson gson = new Gson();
        JsonObject jsonObject = null;

        try {
            String json = IOUtils.toString(in, Charsets.UTF_8);

            jsonObject = gson.fromJson(json, JsonObject.class);
        } catch (IOException e) {
            KamiMod.log.error(e);
        }

        return jsonObject;
    }
}
