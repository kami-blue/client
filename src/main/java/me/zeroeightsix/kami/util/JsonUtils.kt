package me.zeroeightsix.kami.util

import com.google.gson.Gson
import com.google.gson.JsonObject
import me.zeroeightsix.kami.KamiMod
import org.apache.commons.compress.utils.Charsets
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.io.InputStream

object JsonUtils {
    /**
     * Pop in a JSON input stream and get a JsonObject as output.
     *
     * @param in A JSON input stream ting
     * @return Corresponding JsonObject
     */
    fun streamToJson(`in`: InputStream?): JsonObject? {
        val gson = Gson()
        var jsonObject: JsonObject? = null

        try {
            val json = IOUtils.toString(`in`, Charsets.UTF_8)

            jsonObject = gson.fromJson(json, JsonObject::class.java)
        } catch (e: IOException) {
            KamiMod.log.error(e)
        }

        return jsonObject
    }
}