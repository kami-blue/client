package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.world.GameType

/**
 * Yes, this is 100% original code. Go away
 */
@Module.Info(
        name = "FakeGamemode",
        description = "Fakes your current gamemode client side",
        category = Module.Category.MISC
)
object FakeGamemode : Module() {
    private val gamemode = register(Settings.e<GamemodeChanged>("Mode", GamemodeChanged.CREATIVE))

    private var gameType: GameType? = null

    override fun onUpdate(event: SafeTickEvent) {
        when (gamemode.value) {
            GamemodeChanged.CREATIVE -> {
                mc.playerController.setGameType(gameType)
                mc.playerController.setGameType(GameType.CREATIVE)
            }
            GamemodeChanged.SURVIVAL -> {
                mc.playerController.setGameType(gameType)
                mc.playerController.setGameType(GameType.SURVIVAL)
            }
            GamemodeChanged.ADVENTURE -> {
                mc.playerController.setGameType(gameType)
                mc.playerController.setGameType(GameType.ADVENTURE)
            }
            GamemodeChanged.SPECTATOR -> {
                mc.playerController.setGameType(gameType)
                mc.playerController.setGameType(GameType.SPECTATOR)
            }
        }
    }

    public override fun onEnable() {
        if (mc.player == null) disable() else gameType = mc.playerController.getCurrentGameType()
    }

    public override fun onDisable() {
        if (mc.player == null) return
        mc.playerController.setGameType(gameType)
    }

    private enum class GamemodeChanged {
        SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR
    }
}