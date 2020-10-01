package me.zeroeightsix.kami.manager.mangers

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.util.Friends
import java.io.*

object FriendManager : Manager() {
    var friendFile = FriendFile()

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private const val configName = "KAMIBlueFriends.json"
    val file = File(configName)

    /**
     * Reads friends from KAMIBlueFriends.json into the friends ArrayList
     */
    @JvmStatic
    fun loadFriends(): Boolean {
        var success = false
        try {
            try {
                friendFile = gson.fromJson(FileReader(file), object : TypeToken<FriendFile>() {}.type)
                KamiMod.log.info("Friend loaded")
                success = true
            } catch (e: FileNotFoundException) {
                KamiMod.log.warn("Could not find file ${configName}, clearing the friend list")
                friendFile.friends.clear()
            }
        } catch (e: IllegalStateException) {
            KamiMod.log.warn("$configName is empty!")
            friendFile.friends.clear()
        }
        return success
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
        } catch (e: IOException) {
            KamiMod.log.info("Failed saving friend")
            e.printStackTrace()
            false
        }
    }

    class FriendFile {
        @JvmField
        @SerializedName("Enabled")
        var enabled = true

        @JvmField
        @SerializedName("Friends")
        val friends = ArrayList<Friends.Friend>()
    }
}