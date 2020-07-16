package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtils.mobTypeSettings
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer

/**
 * Created by 086 on 12/12/2017.
 * Updated by Xiaro on 11/07/20
 */
@Module.Info(
        name = "Chams",
        category = Module.Category.RENDER,
        description = "See entities through walls"
)
class Chams : Module() {
    companion object {
        private val players = Settings.b("Players", true)
        private val passive = Settings.b("PassiveMobs", false)
        private val neutral = Settings.b("NeutralMobs", true)
        private val hostile = Settings.b("HostileMobs", true)

        @JvmStatic
        fun renderChams(entity: Entity?): Boolean {
            return when (entity) {
                null -> false
                is EntityPlayer -> players.value
                else -> mobTypeSettings(entity, true, passive.value, neutral.value, hostile.value)
            }
        }
    }

    init { /* needed because the settings are static */
        registerAll(players, passive, neutral, hostile)
    }
}