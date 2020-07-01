package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.DiscordPresence
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.MessageSendHelper

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 13/01/20
 * Updated (slightly) by Dewy on 3rd April 2020
 */
@Module.Info(
        name = "DiscordRPC",
        category = Module.Category.MISC,
        description = "Discord Rich Presence"
)
class DiscordRPC : Module() {
    private val coordsConfirm = register(Settings.b("CoordsConfirm", false))
    @JvmField
    var line1Setting: Setting<LineInfo> = register(Settings.e("Line1Left", LineInfo.VERSION)) // details left
    @JvmField
    var line3Setting: Setting<LineInfo> = register(Settings.e("Line1Right", LineInfo.USERNAME)) // details right
    @JvmField
    var line2Setting: Setting<LineInfo> = register(Settings.e("Line2Left", LineInfo.SERVER_IP)) // state left
    @JvmField
    var line4Setting: Setting<LineInfo> = register(Settings.e("Line2Right", LineInfo.HEALTH)) // state right

    enum class LineInfo {
        VERSION, WORLD, DIMENSION, USERNAME, HEALTH, SERVER_IP, COORDS, NONE
    }

    fun getLine(line: LineInfo?): String {
        return when (line) {
            LineInfo.VERSION -> KamiMod.MODVERSMALL
            LineInfo.WORLD -> if (mc.isIntegratedServerRunning) "Singleplayer" else if (mc.getCurrentServerData() != null) "Multiplayer" else "Main Menu"
            LineInfo.DIMENSION -> InfoCalculator.playerDimension(mc)
            LineInfo.USERNAME -> if (mc.player != null) mc.player.name else mc.getSession().username
            LineInfo.HEALTH -> if (mc.player != null) mc.player.health.toInt().toString() + " hp" else "No hp"
            LineInfo.SERVER_IP -> if (mc.getCurrentServerData() != null) mc.getCurrentServerData()!!.serverIP else if (mc.isIntegratedServerRunning) "Offline" else "Main Menu"
            LineInfo.COORDS -> if (mc.player != null && coordsConfirm.value) "(" + mc.player.posX.toInt() + " " + mc.player.posY.toInt() + " " + mc.player.posZ.toInt() + ")" else "No coords"
            else -> ""
        }
    }

    public override fun onEnable() {
        DiscordPresence.start()
    }

    override fun onUpdate() {
        if (startTime == 0L) startTime = System.currentTimeMillis()
        if (startTime + 10000 <= System.currentTimeMillis()) { // 10 seconds in milliseconds
            if (line1Setting.value == LineInfo.COORDS || line2Setting.value == LineInfo.COORDS || line3Setting.value == LineInfo.COORDS || line4Setting.value == LineInfo.COORDS) {
                if (!coordsConfirm.value && mc.player != null) {
                    MessageSendHelper.sendWarningMessage("$chatName Warning: In order to use the coords option please enable the coords confirmation option. This will display your coords on the discord rpc. Do NOT use this if you do not want your coords displayed")
                }
            }
            startTime = System.currentTimeMillis()
        }
    }

    override fun onDisable() {
        DiscordPresence.end()
    }

    companion object {
        private var startTime: Long = 0
    }
}