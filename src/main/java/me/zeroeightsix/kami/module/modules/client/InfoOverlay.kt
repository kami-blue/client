@file:Suppress("DEPRECATION")

package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.TimeUtils
import me.zeroeightsix.kami.util.color.ColorTextFormatting
import me.zeroeightsix.kami.util.color.ColorTextFormatting.ColourCode
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.utils.MathUtils.round
import java.util.*
import kotlin.math.max

@Module.Info(
        name = "InfoOverlay",
        category = Module.Category.CLIENT,
        description = "Configures the game information overlay",
        showOnArray = Module.ShowOnArray.OFF,
        alwaysEnabled = true
)
object InfoOverlay : Module() {
    /* This is so horrible // TODO: FIX */
    private val page = register(Settings.enumBuilder(Page::class.java, "Page").withValue(Page.ONE))

    /* Page One */
    private val version = register(Settings.booleanBuilder("Version").withValue(true).withVisibility { page.value == Page.ONE })
    private val username = register(Settings.booleanBuilder("Username").withValue(true).withVisibility { page.value == Page.ONE })
    private val tps = register(Settings.booleanBuilder("TPS").withValue(true).withVisibility { page.value == Page.ONE })
    private val fps = register(Settings.booleanBuilder("FPS").withValue(true).withVisibility { page.value == Page.ONE })
    private val ping = register(Settings.booleanBuilder("Ping").withValue(false).withVisibility { page.value == Page.ONE })
    private val server = register(Settings.booleanBuilder("ServerType").withValue(true).withVisibility { page.value == Page.ONE })
    private val durability = register(Settings.booleanBuilder("ItemDamage").withValue(false).withVisibility { page.value == Page.ONE })
    private val biome = register(Settings.booleanBuilder("Biome").withValue(false).withVisibility { page.value == Page.ONE })

    /* Page Two */
    private val totems = register(Settings.booleanBuilder("Totems").withValue(false).withVisibility { page.value == Page.TWO })
    private val endCrystals = register(Settings.booleanBuilder("EndCrystals").withValue(false).withVisibility { page.value == Page.TWO })
    private val expBottles = register(Settings.booleanBuilder("EXPBottles").withValue(false).withVisibility { page.value == Page.TWO })
    private val godApples = register(Settings.booleanBuilder("GodApples").withValue(false).withVisibility { page.value == Page.TWO })

    /* Page Three */
    private val decimalPlaces = register(Settings.integerBuilder("DecimalPlaces").withValue(2).withRange(0, 10).withVisibility { page.value == Page.THREE })
    private val speed = register(Settings.booleanBuilder("Speed").withValue(true).withVisibility { page.value == Page.THREE })
    private val averageSpeedTime = register(Settings.floatBuilder("AverageSpeedTime(s)").withValue(1f).withRange(0f, 5f).withVisibility { page.value == Page.THREE && speed.value })
    private val speedUnit = register(Settings.enumBuilder(SpeedUnit::class.java, "SpeedUnit").withValue(SpeedUnit.KMH).withVisibility { page.value == Page.THREE && speed.value })
    private val time = register(Settings.booleanBuilder("Time").withValue(true).withVisibility { page.value == Page.THREE })
    val timeTypeSetting = register(Settings.enumBuilder(TimeUtils.TimeType::class.java, "TimeFormat").withValue(TimeUtils.TimeType.HHMMSS).withVisibility { page.value == Page.THREE && time.value })
    val timeUnitSetting = register(Settings.enumBuilder(TimeUtils.TimeUnit::class.java, "TimeUnit").withValue(TimeUtils.TimeUnit.H12).withVisibility { page.value == Page.THREE && time.value })
    val doLocale = register(Settings.booleanBuilder("TimeShowAM/PM").withValue(true).withVisibility { page.value == Page.THREE && time.value && timeUnitSetting.value == TimeUtils.TimeUnit.H12 })
    private val memory = register(Settings.booleanBuilder("RAMUsed").withValue(false).withVisibility { page.value == Page.THREE })
    private val timerSpeed = register(Settings.booleanBuilder("TimerSpeed").withValue(false).withVisibility { page.value == Page.THREE })
    private val firstColor = register(Settings.enumBuilder(ColourCode::class.java, "FirstColour").withValue(ColourCode.WHITE).withVisibility { page.value == Page.THREE })
    private val secondColor = register(Settings.enumBuilder(ColourCode::class.java, "SecondColour").withValue(ColourCode.BLUE).withVisibility { page.value == Page.THREE })

    private enum class Page {
        ONE, TWO, THREE
    }

    @Suppress("UNUSED")
    private enum class SpeedUnit(val displayName: String) {
        MPS("m/s"),
        KMH("km/h")
        // No retarded imperial unit here
    }

    private val speedList = LinkedList<Double>()

    init {
        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@listener
            updateSpeedList()
        }
    }

    fun infoContents(): ArrayList<String> {
        val infoContents = ArrayList<String>()
        for (setting in settingList) {
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
        else -> null
    }

    fun first() = setToText(firstColor.value)

    fun second() = setToText(secondColor.value)

    private fun setToText(colourCode: ColourCode) = ColorTextFormatting.toTextMap[colourCode]!!

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