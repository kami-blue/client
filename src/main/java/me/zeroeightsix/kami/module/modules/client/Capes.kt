package me.zeroeightsix.kami.module.modules.client

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import org.kamiblue.capeapi.Cape
import org.kamiblue.capeapi.CapeUser
import org.kamiblue.commons.utils.ConnectionUtils
import org.kamiblue.commons.utils.ThreadUtils
import java.util.*
import kotlin.collections.HashMap

@Module.Info(
        name = "Capes",
        category = Module.Category.CLIENT,
        description = "Controls the display of KAMI Blue capes",
        showOnArray = Module.ShowOnArray.OFF,
        enabledByDefault = true
)
object Capes : Module() {
    val capeUsers: MutableMap<UUID, Cape> = Collections.synchronizedMap(HashMap<UUID, Cape>())
    var isPremium = false; private set

    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.MINUTES)
    private val gson = Gson()
    private val thread = Thread({ updateCapes() }, "Capes Update Thread")

    override fun onEnable() {
        ThreadUtils.submitTask(thread)
    }

    init {
        listener<SafeTickEvent> {
            if (timer.tick(5L)) ThreadUtils.submitTask(thread)
        }
    }

    private fun updateCapes() {
        val rawJson = ConnectionUtils.requestRawJsonFrom(KamiMod.CAPES_JSON) {
            KamiMod.log.warn("Failed requesting capes", it)
        } ?: return

        try {
            val cacheList = gson.fromJson<ArrayList<CapeUser>>(rawJson, object : TypeToken<List<CapeUser>>() {}.type)
            cacheList.forEach { capeUser ->
                capeUser.capes.forEach { cape ->
                    cape.playerUUID?.let {
                        capeUsers[it] = cape
                        isPremium = isPremium || mc.session.profile.id == it && capeUser.isPremium
                    }
                }
            }
            KamiMod.log.info("Capes loaded")
        } catch (e: Exception) {
            KamiMod.log.warn("Failed parsing capes", e)
        }
    }

}