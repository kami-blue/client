package me.zeroeightsix.kami.module.modules.client

import CapeColor
import CapeUser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import getRequest
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module

@Module.Info(
        name = "Capes",
        category = Module.Category.CLIENT,
        description = "Controls the display of KAMI Blue capes",
        showOnArray = Module.ShowOnArray.OFF,
        enabledByDefault = true
)
object Capes : Module() {
    val capeUsers = HashMap<String, CapeColor>() // This will have to be replaced to be <UUID, Cape> in the future, when Star support is added

    public override fun onEnable() {
        Thread {
            try {
                val response = getRequest(KamiMod.CAPES_JSON)
                val capeUsersCache = Gson().fromJson<ArrayList<CapeUser>?>(response, object : TypeToken<List<CapeUser>>() {}.type)

                capeUsersCache?.forEach { capeUser ->
                    capeUser.capes.forEach { cape ->
                        cape.playerUUID?.let { capeUsers[it] = cape.color }
                    }
                } ?: run {
                    KamiMod.log.warn("$chatName Failed to parse / download capes!")
                }

                KamiMod.log.info("$chatName loaded ${capeUsers.size} capes!")
            } catch (e: Exception) {
                KamiMod.log.error("Failed to load capes!")
                e.printStackTrace()
            }
        }.start()
    }
}