package org.kamiblue.capeapi

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.kamiblue.commons.utils.ConnectionUtils
import java.io.*
import java.util.*
import kotlin.collections.LinkedHashMap

abstract class AbstractUUIDManager(filePath: String) {
    protected open val maxCacheSize: Int get() = 500

    private val file = File(filePath)
    @Suppress("DEPRECATION")
    private val parser = JsonParser()
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val nameProfileMap = Collections.synchronizedMap(LinkedHashMap<String, PlayerProfile>())
    private val uuidNameMap = Collections.synchronizedMap(LinkedHashMap<UUID, PlayerProfile>())

    fun getByString(stringIn: String?) = stringIn?.let { string ->
        UUIDUtils.fixUUID(string)?.let { getByUUID(it) } ?: getByName(string)
    }

    fun getByUUID(uuid: UUID?) = uuid?.let {
        uuidNameMap.getOrPut(uuid) {
            getOrRequest(uuid.toString())?.also { profile ->
                // If UUID already present in nameUuidMap but not in uuidNameMap (user changed name)
                nameProfileMap[profile.name]?.let { uuidNameMap.remove(it.uuid) }
                nameProfileMap[profile.name] = profile
            }
        }.also {
            trimMaps()
        }
    }

    fun getByName(name: String?) = name?.let {
        nameProfileMap.getOrPut(name.toLowerCase()) {
            getOrRequest(name)?.also { profile ->
                // If UUID already present in uuidNameMap but not in nameUuidMap (user changed name)
                uuidNameMap[profile.uuid]?.let { nameProfileMap.remove(it.name) }
                uuidNameMap[profile.uuid] = profile
            }
        }.also {
            trimMaps()
        }
    }

    private fun trimMaps() {
        while (nameProfileMap.size > maxCacheSize) {
            nameProfileMap.remove(nameProfileMap.keys.first())?.also {
                uuidNameMap.remove(it.uuid)
            }
        }
    }

    /**
     * Overwrites this if you want to get UUID from other source
     * eg. online player in game client
     */
    protected open fun getOrRequest(nameOrUUID: String): PlayerProfile? {
        return requestProfile(nameOrUUID)
    }

    private fun requestProfile(nameOrUUID: String): PlayerProfile? {
        val isUUID = UUIDUtils.isUUID(nameOrUUID)
        val response = if (isUUID) requestProfileFromUUID(nameOrUUID) else requestProfileFromName(nameOrUUID)

        return if (response.isNullOrBlank()) {
            logError("Response is null or blank, internet might be down")
            null
        } else {
            try {
                @Suppress("DEPRECATION") val jsonElement = parser.parse(response)
                if (isUUID) {
                    val name = jsonElement.asJsonArray.last().asJsonObject["name"].asString
                    PlayerProfile(UUID.fromString(nameOrUUID), name)
                } else {
                    val id = jsonElement.asJsonObject["id"].asString
                    val name = jsonElement.asJsonObject["name"].asString
                    PlayerProfile(UUIDUtils.fixUUID(id)!!, name) // let it throw a NPE if failed to parse the string to UUID
                }
            } catch (e: Exception) {
                logError("Failed parsing profile")
                e.printStackTrace()
                null
            }
        }
    }

    private fun requestProfileFromUUID(uuid: String): String? {
        return request("https://api.mojang.com/user/profiles/${UUIDUtils.removeDashes(uuid)}/names")
    }

    private fun requestProfileFromName(name: String): String? {
        return request("https://api.mojang.com/users/profiles/minecraft/$name")
    }

    private fun request(url: String): String? {
        return ConnectionUtils.requestRawJsonFrom(url) {
            logError("Failed requesting from Mojang API")
            it.printStackTrace()
        }
    }

    fun load(): Boolean {
        fixEmptyJson(file)
        val reader = BufferedReader(FileReader(file))
        return try {
            val cacheList = gson.fromJson<List<PlayerProfile>>(reader, object : TypeToken<List<PlayerProfile>>() {}.type)
            uuidNameMap.clear()
            nameProfileMap.clear()
            uuidNameMap.putAll(cacheList.associateBy { it.uuid })
            nameProfileMap.putAll(cacheList.associateBy { it.name.toLowerCase() })
            logError("UUID cache loaded")
            true
        } catch (e: Exception) {
            logError("Failed loading UUID cache")
            e.printStackTrace()
            false
        } finally {
            reader.close()
        }
    }

    fun save(): Boolean {
        val writer = BufferedWriter(FileWriter(file, false))
        return try {
            val cacheList = uuidNameMap.values.sortedBy { it.name }
            gson.toJson(cacheList, writer)
            println("UUID cache saved")
            true
        } catch (e: Exception) {
            logError("Failed saving UUID cache")
            e.printStackTrace()
            false
        } finally {
            writer.flush()
            writer.close()
        }
    }

    private fun fixEmptyJson(file: File) {
        if (!file.exists()) file.createNewFile()
        var notEmpty = false
        file.forEachLine { notEmpty = notEmpty || it.trim().isNotBlank() || it == "[]" || it == "{}" }

        if (!notEmpty) {
            val fileWriter = FileWriter(file)
            try {
                fileWriter.write("[]")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                fileWriter.close()
            }
        }
    }

    protected abstract fun logError(message: String)

}
