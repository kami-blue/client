package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.mixin.client.entity.MixinEntity
import me.zeroeightsix.kami.module.Module

/**
 * @see MixinEntity.isSneaking
 */
@Module.Info(
        name = "Scaffold",
        category = Module.Category.PLAYER,
        description = "Places blocks under you"
)
object Scaffold : Module() {

}