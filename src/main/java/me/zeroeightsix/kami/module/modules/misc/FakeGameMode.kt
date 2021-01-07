package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.threads.runSafe
import me.zeroeightsix.kami.util.threads.runSafeR
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.world.GameType
import net.minecraftforge.fml.common.gameevent.TickEvent

@Module.Info(
    name = "FakeGameMode",
    description = "Fakes your current gamemode client side",
    category = Module.Category.MISC
)
object FakeGameMode : Module() {
    private val gamemode by setting("Mode", GameMode.CREATIVE)

    @Suppress("UNUSED")
    private enum class GameMode(val gameType: GameType) {
        SURVIVAL(GameType.SURVIVAL),
        CREATIVE(GameType.CREATIVE),
        ADVENTURE(GameType.ADVENTURE),
        SPECTATOR(GameType.SPECTATOR)
    }

    private var prevGameMode: GameType? = null

    init {
        safeListener<TickEvent.ClientTickEvent> {
            playerController.setGameType(gamemode.gameType)
        }

        onEnable {
            runSafeR {
                prevGameMode = playerController.currentGameType
            } ?: disable()
        }

        onDisable {
            runSafe {
                prevGameMode?.let { playerController.setGameType(it) }
            }
        }
    }
}