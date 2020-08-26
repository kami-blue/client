package me.zeroeightsix.kami.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.mojang.util.UUIDTypeAdapter
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.manager.mangers.FileInstanceManager
import me.zeroeightsix.kami.manager.mangers.FriendManager
import me.zeroeightsix.kami.util.text.MessageSendHelper
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by 086 on 13/12/2017.
 * Updated by Xiaro on 14/08/20
 */
object Friends {

    @JvmStatic
    fun isFriend(name: String): Boolean {
        return FriendManager.friendFile.enabled && FriendManager.friendFile.friends.any { friend -> friend.username.equals(name, ignoreCase = true) }
    }

    fun addFriend(name: String): Boolean {
        val friend = getFriendByName(name)
        return if (friend == null) {
            false
        } else {
            FriendManager.friendFile.friends.add(friend)
            true
        }
    }

    fun removeFriend(name: String): Boolean {
        return FriendManager.friendFile.friends.removeIf { friend -> friend.username.equals(name, ignoreCase = true) }
    }

    fun getFriendByName(input: String): Friend? {
        val infoMap = ArrayList(Wrapper.minecraft.connection!!.playerInfoMap)
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

    class Friend(
            @SerializedName("Name")
            var username: String,

            @SerializedName("UUID")
            var uuid: UUID
    )
}