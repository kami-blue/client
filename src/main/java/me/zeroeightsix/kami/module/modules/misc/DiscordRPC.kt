package me.zeroeightsix.kami.module.modules.misc

import club.minnced.discord.rpc.DiscordEventHandlers
import club.minnced.discord.rpc.DiscordRichPresence
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.client.InfoOverlay
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.math.CoordinateConverter.asString
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.Minecraft
import org.kamiblue.capeapi.CapeType
import org.kamiblue.event.listener.listener

@Module.Info(
        name = "DiscordRPC",
        category = Module.Category.MISC,
        description = "Discord Rich Presence",
        enabledByDefault = true
)
object DiscordRPC : Module() {
    private val line1Left: Setting<LineInfo> = register(Settings.e("Line1Left", LineInfo.VERSION)) // details left
    private val line1Right: Setting<LineInfo> = register(Settings.e("Line1Right", LineInfo.USERNAME)) // details right
    private val line2Left: Setting<LineInfo> = register(Settings.e("Line2Left", LineInfo.SERVER_IP)) // state left
    private val line2Right: Setting<LineInfo> = register(Settings.e("Line2Right", LineInfo.HEALTH)) // state right
    private val coordsConfirm = register(Settings.booleanBuilder("CoordsConfirm").withValue(false).withVisibility { showCoordsConfirm() })
    private val updateDelay = register(Settings.floatBuilder("UpdateDelay").withValue(4f).withRange(1f, 10f))

    private enum class LineInfo {
        VERSION, WORLD, DIMENSION, USERNAME, HEALTH, HUNGER, SERVER_IP, COORDS, SPEED, HELD_ITEM, FPS, TPS, NONE
    }

    private val presence = DiscordRichPresence()
    private val rpc = club.minnced.discord.rpc.DiscordRPC.INSTANCE
    private var connected = false
    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)

    override fun onEnable() {
        start()
    }

    override fun onDisable() {
        end()
    }

    init {
        listener<SafeTickEvent> {
            if (showCoordsConfirm() && !coordsConfirm.value && timer.tick(10L)) {
                MessageSendHelper.sendWarningMessage("$chatName Warning: In order to use the coords option please enable the coords confirmation option. " +
                        "This will display your coords on the discord rpc. " +
                        "Do NOT use this if you do not want your coords displayed")
            }
        }
    }

    private fun start() {
        if (connected) return

        KamiMod.LOG.info("Starting Discord RPC")
        connected = true
        rpc.Discord_Initialize(KamiMod.APP_ID, DiscordEventHandlers(), true, "")
        presence.startTimestamp = System.currentTimeMillis() / 1000L

        /* update rpc while thread isn't interrupted  */
        Thread({ setRpcWithDelay() }, "Discord-RPC-Callback-Handler").start()

        KamiMod.LOG.info("Discord RPC initialised successfully")
    }

    fun end() {
        if (!connected) return

        KamiMod.LOG.info("Shutting down Discord RPC...")
        connected = false
        rpc.Discord_Shutdown()
    }

    private fun showCoordsConfirm(): Boolean {
        return line1Left.value == LineInfo.COORDS
                || line2Left.value == LineInfo.COORDS
                || line1Right.value == LineInfo.COORDS
                || line2Right.value == LineInfo.COORDS
    }

    private fun setRpcWithDelay() {
        while (!Thread.currentThread().isInterrupted && connected) {
            try {
                presence.details = getLine(line1Left.value) + getSeparator(0) + getLine(line1Right.value)
                presence.state = getLine(line2Left.value) + getSeparator(1) + getLine(line2Right.value)
                rpc.Discord_UpdatePresence(presence)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            try {
                Thread.sleep((updateDelay.value * 1000f).toLong())
            } catch (interruptedException: InterruptedException) {
                interruptedException.printStackTrace()
            }
        }
    }

    private fun getLine(line: LineInfo): String {
        return when (line) {
            LineInfo.VERSION -> {
                KamiMod.VERSION_SIMPLE
            }
            LineInfo.WORLD -> {
                when {
                    mc.isIntegratedServerRunning -> "Singleplayer"
                    mc.currentServerData != null -> "Multiplayer"
                    else -> "Main Menu"
                }
            }
            LineInfo.DIMENSION -> {
                InfoCalculator.dimension()
            }
            LineInfo.USERNAME -> {
                mc.session.username
            }
            LineInfo.HEALTH -> {
                if (mc.player != null) "${mc.player.health.toInt()} HP"
                else "No HP"
            }
            LineInfo.HUNGER -> {
                if (mc.player != null) "${mc.player.foodStats.foodLevel} hunger"
                else "No Hunger"
            }
            LineInfo.SERVER_IP -> {
                InfoCalculator.getServerType()
            }
            LineInfo.COORDS -> {
                if (mc.player != null && coordsConfirm.value) "(${mc.player.positionVector.toBlockPos().asString()})"
                else "No Coords"
            }
            LineInfo.SPEED -> {
                if (mc.player != null) InfoOverlay.calcSpeedWithUnit(1)
                else "No Speed"
            }
            LineInfo.HELD_ITEM -> {
                "Holding ${mc.player?.heldItemMainhand?.displayName ?: "Air"}" // Holding air meme
            }
            LineInfo.FPS -> {
                "${Minecraft.getDebugFPS()} FPS"
            }
            LineInfo.TPS -> {
                if (mc.player != null) "${InfoCalculator.tps(1)} tps"
                else "No Tps"
            }
            else -> {
                " "
            }
        }
    }

    private fun getSeparator(line: Int): String {
        return if (line == 0) {
            if (line1Left.value == LineInfo.NONE || line1Right.value == LineInfo.NONE) " " else " | "
        } else {
            if (line2Left.value == LineInfo.NONE || line2Right.value == LineInfo.NONE) " " else " | "
        }
    }

    fun setCustomIcons(capeType: CapeType?) {
        presence.smallImageKey = capeType?.imageKey ?: ""
        presence.smallImageText = when (capeType) {
            CapeType.BOOSTER -> "booster"
            CapeType.CONTEST -> "contest winner!"
            CapeType.CONTRIBUTOR -> "code contributor!"
            CapeType.DONOR -> "donator <3"
            CapeType.INVITER -> "inviter"
            CapeType.SPECIAL -> "special cape!"
            else -> ""
        }
    }

    init {
        presence.largeImageKey = "kami"
        presence.largeImageText = "kamiblue.org"
    }
}
