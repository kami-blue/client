package me.zeroeightsix.kami.manager.managers

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.mojang.util.UUIDTypeAdapter
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.text.MessageSendHelper
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet

object FriendManager : Manager {
    private const val configName = "KAMIBlueFriends.json"
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File(configName)

    private var friendFile = FriendFile()
    val friends: MutableMap<String, Friend> = Collections.synchronizedMap(HashMap<String, Friend>())

    val empty get() = friends.isEmpty()
    var enabled = friendFile.enabled
        set(value) {
            field = value
            friendFile.enabled = value
        }

    fun isFriend(name: String) = friendFile.enabled && friends.contains(name.toLowerCase())

    fun addFriend(name: String) = getFriendByName(name)?.let {
        friendFile.friends.add(it)
        friends[it.username.toLowerCase()] = it
        true
    } ?: false

    fun removeFriend(name: String) = friendFile.friends.remove(friends.remove(name.toLowerCase()))

    fun clearFriend() {
        friends.clear()
        friendFile.friends.clear()
    }

    private fun getFriendByName(input: String): Friend? {
        val infoMap = Wrapper.minecraft.connection?.playerInfoMap?.let { ArrayList(it) }
        val profile = infoMap?.firstOrNull { it.gameProfile.name.equals(input, ignoreCase = true) }

        return if (profile != null) {
            Friend(profile.gameProfile.name, profile.gameProfile.id)
        } else {
            MessageSendHelper.sendChatMessage("Player isn't online. Looking up UUID..")
            val string = requestIDs(input)
            if (string.isNullOrBlank()) {
                MessageSendHelper.sendChatMessage("Couldn't find player ID. Are you connected to the internet? (0)")
                null
            } else {
                val element = JsonParser().parse(string)
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
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.doOutput = true
            connection.doInput = true
            connection.requestMethod = "POST"
            val outputStream = connection.outputStream
            outputStream.write(data.toByteArray(StandardCharsets.UTF_8))
            outputStream.close()

            // read the response
            val inputStream: InputStream = BufferedInputStream(connection.inputStream)
            val res = convertStreamToString(inputStream)
            inputStream.close()
            connection.disconnect()
            res
        } catch (e: Exception) {
            null
        }
    }

    private fun convertStreamToString(inputStream: InputStream): String {
        val scanner = Scanner(inputStream).useDelimiter("\\A")
        return if (scanner.hasNext()) scanner.next() else "/"
    }

    /**
     * Reads friends from KAMIBlueFriends.json into the friends ArrayList
     */
    @JvmStatic
    fun loadFriends(): Boolean {
        ConfigUtils.fixEmptyJson(file)
        return try {
            friendFile = gson.fromJson(FileReader(file), object : TypeToken<FriendFile>() {}.type)
            friends.clear()
            friends.putAll(friendFile.friends.associateBy { it.username.toLowerCase() })
            KamiMod.log.info("Friend loaded")
            true
        } catch (e: Exception) {
            KamiMod.log.error("Failed loading friends", e)
            false
        }
    }

    /**
     * Saves friends from the friends ArrayList into KAMIBlueFriends.json
     */
    @JvmStatic
    fun saveFriends(): Boolean {
        return try {
            friendFile.friends.removeIf { friend -> friend.username.isBlank() } // mfw empty friend
            val fileWriter = FileWriter(file, false)
            gson.toJson(friendFile, fileWriter)
            fileWriter.flush()
            fileWriter.close()
            KamiMod.log.info("Friend saved")
            true
        } catch (e: Exception) {
            KamiMod.log.error("Failed saving friend", e)
            e.printStackTrace()
            false
        }
    }

    data class FriendFile(
            @SerializedName("Enabled")
            var enabled: Boolean = true,

            @SerializedName("Friends")
            val friends: MutableSet<Friend> = Collections.synchronizedSet(LinkedHashSet<Friend>())
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is FriendFile) return false

            if (enabled != other.enabled) return false
            if (friends != other.friends) return false

            return true
        }

        override fun hashCode(): Int {
            var result = enabled.hashCode()
            result = 31 * result + friends.hashCode()
            return result
        }
    }

    data class Friend(
            @SerializedName("Name")
            var username: String,

            @SerializedName("UUID")
            var uuid: UUID
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Friend) return false

            if (username != other.username) return false
            if (uuid != other.uuid) return false

            return true
        }

        override fun hashCode(): Int {
            var result = username.hashCode()
            result = 31 * result + uuid.hashCode()
            return result
        }
    }
}