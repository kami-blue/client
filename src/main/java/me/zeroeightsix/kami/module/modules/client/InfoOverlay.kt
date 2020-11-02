@file:Suppress("DEPRECATION")

package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.TimeUtils
import me.zeroeightsix.kami.util.color.ColorTextFormatting
import me.zeroeightsix.kami.util.color.ColorTextFormatting.ColorCode
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.MathUtils.round
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*
import kotlin.math.max

@Module.Info(
        name = "InfoOverlay",
        category = Module.Category.CLIENT,
        description = "Configures the game information overlay",
        showOnArray = false,
        alwaysEnabled = true
)
object InfoOverlay : Module() {
    /* This is so horrible // TODO: FIX */
    private val page = setting("Page", Page.ONE)

    /* Page One */
    private val version = setting("Version", true, { page.value == Page.ONE })
    private val username = setting("Username", true, { page.value == Page.ONE })
    private val tps = setting("TPS", true, { page.value == Page.ONE })
    private val fps = setting("FPS", true, { page.value == Page.ONE })
    private val ping = setting("Ping", false, { page.value == Page.ONE })
    private val server = setting("ServerType", true, { page.value == Page.ONE })
    private val chunkSize = setting("ChunkSize", true, { page.value == Page.ONE })
    private val durability = setting("ItemDamage", false, { page.value == Page.ONE })
    private val biome = setting("Biome", false, { page.value == Page.ONE })

    /* Page Two */
    private val totems = setting("Totems", false, { page.value == Page.TWO })
    private val endCrystals = setting("EndCrystals", false, { page.value == Page.TWO })
    private val expBottles = setting("EXPBottles", false, { page.value == Page.TWO })
    private val godApples = setting("GodApples", false, { page.value == Page.TWO })

    /* Page Three */
    private val decimalPlaces = setting("DecimalPlaces", 2, 0..10, 1, { page.value == Page.THREE })
    private val speed = setting("Speed", true, { page.value == Page.THREE })
    private val averageSpeedTime = setting("AverageSpeedTime(s)", 1f, 0f..5f, 0.25f, { page.value == Page.THREE && speed.value })
    private val speedUnit = setting("SpeedUnit", SpeedUnit.KMH, { page.value == Page.THREE && speed.value })
    private val time = setting("Time", true, { page.value == Page.THREE })
    val timeTypeSetting = setting("TimeFormat", TimeUtils.TimeType.HHMMSS, { page.value == Page.THREE && time.value })
    val timeUnitSetting = setting("TimeUnit", TimeUtils.TimeUnit.H12, { page.value == Page.THREE && time.value })
    val doLocale = setting("TimeShowAM/PM", true, { page.value == Page.THREE && time.value && timeUnitSetting.value == TimeUtils.TimeUnit.H12 })
    private val memory = setting("RAMUsed", false, { page.value == Page.THREE })
    private val timerSpeed = setting("TimerSpeed", false, { page.value == Page.THREE })
    private val firstColor = setting("FirstColour", ColorCode.WHITE, { page.value == Page.THREE })
    private val secondColor = setting("SecondColour", ColorCode.BLUE, { page.value == Page.THREE })

    private enum class Page {
        ONE, TWO, THREE
    }

    @Suppress("UNUSED")
    private enum class SpeedUnit(val displayName: String) {
        MPS("m/s"),
        KMH("km/h")
        // No retarded imperial unit here
    }

    private val speedList = ArrayDeque<Double>()
    private var currentChunkSize = 0

    init {
        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@listener
            updateSpeedList()
            if (chunkSize.value && mc.player.ticksExisted % 4 == 0) {
                currentChunkSize = InfoCalculator.chunkSize()
            }
        }
    }

    fun infoContents(): ArrayList<String> {
        val infoContents = ArrayList<String>()
        for (setting in fullSettingList) {
            if (setting.value != true) continue // make sure it is a Boolean setting and enabled

            setting.infoMap()?.let {
                infoContents.add(first().toString() + it)
            }
        }
        return infoContents
    }

    private fun Setting<*>.infoMap() = when (this) {
        version -> "${KamiMod.KAMI_KANJI} ${second()}${KamiMod.VER_SMALL}"
        username -> "Welcome ${second()}${mc.session.username}!"
        time -> TimeUtils.getFinalTime(setToText(secondColor.value), setToText(firstColor.value), timeUnitSetting.value, timeTypeSetting.value, doLocale.value)
        tps -> "${InfoCalculator.tps(decimalPlaces.value)} ${second()}tps"
        fps -> "${Minecraft.debugFPS} ${second()}fps"
        speed -> "${calcSpeed(decimalPlaces.value)} ${second()}${speedUnit.value.displayName}"
        timerSpeed -> "${round(50f / mc.timer.tickLength, decimalPlaces.value)} ${second()}x"
        ping -> "${InfoCalculator.ping()} ${second()}ms"
        server -> mc.player.serverBrand
        durability -> "${InfoCalculator.heldItemDurability()} ${second()}dura"
        biome -> "${mc.world.getBiome(mc.player.position).biomeName} ${second()}biome"
        memory -> "${InfoCalculator.memory()} ${second()}MB"
        totems -> "${InventoryUtils.countItemAll(449)} ${second()}totems"
        endCrystals -> "${InventoryUtils.countItemAll(426)} ${second()}crystals"
        expBottles -> "${InventoryUtils.countItemAll(384)} ${second()}exp"
        godApples -> "${InventoryUtils.countItemAll(322)} ${second()}gaps"
        chunkSize -> "${round(currentChunkSize / 1000.0, decimalPlaces.value)} KB ${second()}(chunk)"
        else -> null
    }

    fun first() = setToText(firstColor.value)

    fun second() = setToText(secondColor.value)

    private fun setToText(colourCode: ColorCode) = ColorTextFormatting.toTextMap[colourCode]!!

    fun calcSpeedWithUnit(place: Int) = "${calcSpeed(place)} ${speedUnit.value.displayName}"

    private fun calcSpeed(place: Int): Double {
        val averageSpeed = if (speedList.isEmpty()) 0.0 else (speedList.sum() / speedList.size.toDouble())
        return round(averageSpeed, place)
    }

    private fun updateSpeedList() {
        val speed = InfoCalculator.speed(speedUnit.value == SpeedUnit.KMH)
        if (speed > 0.0 || mc.player.ticksExisted % 4 == 0) speedList.add(speed) // Only adding it every 4 ticks if speed is 0
        else speedList.poll()
        while (speedList.size > max((averageSpeedTime.value * 20).toInt(), 1)) speedList.poll()
    }
}