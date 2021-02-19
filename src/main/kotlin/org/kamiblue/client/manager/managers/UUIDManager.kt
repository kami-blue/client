package org.kamiblue.client.manager.managers

import org.kamiblue.capeapi.AbstractUUIDManager
import org.kamiblue.capeapi.PlayerProfile
import org.kamiblue.capeapi.UUIDUtils
import org.kamiblue.client.KamiMod
import org.kamiblue.client.manager.Manager
import org.kamiblue.client.util.Wrapper

object UUIDManager : AbstractUUIDManager(KamiMod.DIRECTORY + "uuid_cache.json", KamiMod.LOG, maxCacheSize = 1000), Manager {

    override fun getOrRequest(nameOrUUID: String): PlayerProfile? {
        return Wrapper.minecraft.connection?.playerInfoMap?.let { playerInfoMap ->
            val infoMap = ArrayList(playerInfoMap)
            val isUUID = UUIDUtils.isUUID(nameOrUUID)
            val withOutDashes = UUIDUtils.removeDashes(nameOrUUID)

            infoMap.find {
                isUUID && UUIDUtils.removeDashes(it.gameProfile.id.toString()).equals(withOutDashes, ignoreCase = true)
                    || !isUUID && it.gameProfile.name.equals(nameOrUUID, ignoreCase = true)
            }?.gameProfile?.let {
                PlayerProfile(it.id, it.name)
            }
        } ?: super.getOrRequest(nameOrUUID)
    }
}
