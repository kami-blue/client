package me.zeroeightsix.kami.util

import com.google.common.base.Converter
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.mojang.util.UUIDTypeAdapter
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * Created by 086 on 13/12/2017.
 * Updated by Xiaro on 14/08/20
 */
object Friends {
    @JvmField
    val friends: Setting<ArrayList<Friend>> = Settings.custom("Friends", ArrayList<Friend>(), FriendListConverter()).buildAndRegister("friends")
    @JvmField
    var enabled = true

    @JvmStatic
    fun isFriend(name: String?): Boolean {
        return enabled && friends.value.any { friend -> friend.username.equals(name, ignoreCase = true) }
    }

    fun getFriendByName(input: String?): Friend? {
        if (input == null) return null
        val infoMap = ArrayList(Wrapper.getMinecraft().connection!!.playerInfoMap)
        val profile = infoMap.find { info ->
            info!!.gameProfile.name.equals(input, ignoreCase = true)
        }

        return if (profile != null) {
            Friend(profile.gameProfile.name, profile.gameProfile.id)
        } else {
            MessageSendHelper.sendChatMessage("Player isn't online. Looking up UUID..")
            val s = requestIDs(input)
            if (s.isNullOrBlank()) {
                MessageSendHelper.sendChatMessage("Couldn't find player ID. Are you connected to the internet? (0)")
                null
            } else {
                val element = JsonParser().parse(s)
                if (element.asJsonArray.size() == 0) {
                    MessageSendHelper.sendChatMessage("Couldn't find player ID. (1)")
                    null
                } else {
                    try {
                        val id = element.asJsonArray[0].asJsonObject["id"].asString
                        val username = element.asJsonArray[0].asJsonObject["name"].asString
                        Friend(username, UUIDTypeAdapter.fromString(id))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        MessageSendHelper.sendChatMessage("Couldn't find player ID. (2)")
                        null
                    }
                }
            }
        }
    }

    private fun requestIDs(input: String): String? {
        val data = "[\"$input\"]"
        return try {
            val url = URL("https://api.mojang.com/profiles/minecraft")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true
            conn.doInput = true
            conn.requestMethod = "POST"
            val os = conn.outputStream
            os.write(data.toByteArray(StandardCharsets.UTF_8))
            os.close()

            // read the response
            val `in`: InputStream = BufferedInputStream(conn.inputStream)
            val res = convertStreamToString(`in`)
            `in`.close()
            conn.disconnect()
            res
        } catch (e: Exception) {
            null
        }
    }

    private fun convertStreamToString(`is`: InputStream): String {
        val s = Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else "/"
    }

    class Friend(var username: String, var uuid: UUID)

    private class FriendListConverter : Converter<ArrayList<Friend>, JsonElement>() {
        override fun doForward(list: ArrayList<Friend>): JsonElement {
            val present = StringBuilder()
            for (friend in list) present.append(String.format("%s;%s$", friend.username, friend.uuid.toString()))
            return JsonPrimitive(present.toString())
        }

        override fun doBackward(jsonElement: JsonElement): ArrayList<Friend> {
            val v = jsonElement.asString
            val pairs = v.split(Pattern.quote("$")).toTypedArray()
            val friends = ArrayList<Friend>()
            for (pair in pairs) {
                try {
                    val split = pair.split(";").toTypedArray()
                    val username = split[0]
                    val uuid = UUID.fromString(split[1])
                    friends.add(Friend(getUsernameByUUID(uuid, username), uuid))
                } catch (ignored: Exception) {
                } // Empty line, wrong formatting or something, we don't care
            }
            return friends
        }

        private fun getUsernameByUUID(uuid: UUID, saved: String): String {
            val src = getSource("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")
            return if (src == null || src.isEmpty()) saved else try {
                val `object` = JsonParser().parse(src)
                `object`.asJsonObject["name"].asString
            } catch (e: Exception) {
                e.printStackTrace()
                System.err.println(src)
                saved
            }
        }

        companion object {
            private fun getSource(link: String): String? {
                return try {
                    val u = URL(link)
                    val con = u.openConnection()
                    val `in` = BufferedReader(InputStreamReader(con.getInputStream()))
                    val buffer = StringBuilder()
                    var inputLine: String?
                    while (`in`.readLine().also { inputLine = it } != null) buffer.append(inputLine)
                    `in`.close()
                    buffer.toString()
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}