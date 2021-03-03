package org.kamiblue.client.manager.managers

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import org.kamiblue.capeapi.PlayerProfile
import org.kamiblue.client.KamiMod
import org.kamiblue.client.manager.Manager
import org.kamiblue.client.util.ConfigUtils
import org.kamiblue.commons.extension.synchronized
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet

object FriendManager : Manager {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File(KamiMod.DIRECTORY + "friends.json")

    private var friendFile = FriendFile()
    val friends= HashMap<String, PlayerProfile>().synchronized()

    val empty get() = friends.isEmpty()
    var enabled = friendFile.enabled
        set(value) {
            field = value
            friendFile.enabled = value
        }

    fun isFriend(name: String) = friendFile.enabled && friends.contains(name.toLowerCase())

    fun addFriend(name: String) = UUIDManager.getByName(name)?.let {
        friendFile.friends.add(it)
        friends[it.name.toLowerCase()] = it
        true
    } ?: false

    fun removeFriend(name: String) = friendFile.friends.remove(friends.remove(name.toLowerCase()))

    fun clearFriend() {
        friends.clear()
        friendFile.friends.clear()
    }

    fun loadFriends(): Boolean {
        ConfigUtils.fixEmptyJson(file)

        return try {
            friendFile = gson.fromJson(FileReader(file), object : TypeToken<FriendFile>() {}.type)
            friends.clear()
            friends.putAll(friendFile.friends.associateBy { it.name.toLowerCase() })
            KamiMod.LOG.info("Friend loaded")
            true
        } catch (e: Exception) {
            KamiMod.LOG.warn("Failed loading friends", e)
            false
        }
    }

    fun saveFriends(): Boolean {
        return try {
            FileWriter(file, false).buffered().use {
                gson.toJson(friendFile, it)
            }
            KamiMod.LOG.info("Friends saved")
            true
        } catch (e: Exception) {
            KamiMod.LOG.warn("Failed saving friends", e)
            false
        }
    }

    data class FriendFile(
        @SerializedName("Enabled")
        var enabled: Boolean = true,

        @SerializedName("Friends")
        val friends: MutableSet<PlayerProfile> = LinkedHashSet<PlayerProfile>().synchronized()
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
}