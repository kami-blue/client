package me.zeroeightsix.kami.module.modules.misc

import club.minnced.discord.rpc.DiscordEventHandlers
import club.minnced.discord.rpc.DiscordRichPresence
import com.google.gson.Gson
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.client.InfoOverlay
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.CoordinateConverter.asString
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.Minecraft
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

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

    private lateinit var customUsers: Array<CustomUser>
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

        KamiMod.log.info("Starting Discord RPC")
        connected = true
        rpc.Discord_Initialize(KamiMod.APP_ID, DiscordEventHandlers(), true, "")
        presence.startTimestamp = System.currentTimeMillis() / 1000L

        /* update rpc while thread isn't interrupted  */
        Thread({ setRpcWithDelay() }, "Discord-RPC-Callback-Handler").start()

        KamiMod.log.info("Discord RPC initialised successfully")
    }

    fun end() {
        if (!connected) return

        KamiMod.log.info("Shutting down Discord RPC...")
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
                KamiMod.VER_SMALL
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
                "${Minecraft.debugFPS} FPS"
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

    private fun setCustomIcons() {
        if (customUsers.isNullOrEmpty()) return
        for (user in customUsers) {
            if (user.uuid.isNullOrBlank() || user.type.isNullOrBlank()) continue
            if (!user.uuid.equals(Wrapper.minecraft.session.profile.id.toString(), ignoreCase = true)) continue
            when (user.type.toInt()) {
                0 -> {
                    presence.smallImageKey = "booster"
                    presence.smallImageText = "booster uwu"
                }
                1 -> {
                    presence.smallImageKey = "inviter"
                    presence.smallImageText = "inviter owo"
                }
                2 -> {
                    presence.smallImageKey = "giveaway"
                    presence.smallImageText = "giveaway winner"
                }
                3 -> {
                    presence.smallImageKey = "contest"
                    presence.smallImageText = "contest winner"
                }
                4 -> {
                    presence.smallImageKey = "nine"
                    presence.smallImageText = "900th member"
                }
                5 -> {
                    presence.smallImageKey = "github1"
                    presence.smallImageText = "contributor!! uwu"
                }
                else -> {
                    presence.smallImageKey = "donator2"
                    presence.smallImageText = "donator <3"
                }
            }
        }
    }

    private class CustomUser(val uuid: String?, val type: String?)

    init {
        try {
            val connection = URL(KamiMod.DONATORS_JSON).openConnection() as HttpsURLConnection
            connection.connect()
            customUsers = Gson().fromJson(InputStreamReader(connection.inputStream), Array<CustomUser>::class.java)
            connection.disconnect()
            setCustomIcons()
            KamiMod.log.info("Rich Presence Users init!")
        } catch (exception: Exception) {
            KamiMod.log.error("Failed to load donators")
            exception.printStackTrace()
        }
        presence.largeImageKey = "kami"
        presence.largeImageText = "kamiblue.org"
    }
}
