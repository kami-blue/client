package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.mixin.extension.tickLength
import me.zeroeightsix.kami.mixin.extension.timer
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.TimeUtils
import me.zeroeightsix.kami.util.color.EnumTextColor
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.utils.MathUtils.round
import java.util.*
import kotlin.collections.ArrayList
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
    private val firstColor = register(Settings.enumBuilder(EnumTextColor::class.java, "FirstColor").withValue(EnumTextColor.WHITE).withVisibility { page.value == Page.THREE })
    private val secondColor = register(Settings.enumBuilder(EnumTextColor::class.java, "SecondColor").withValue(EnumTextColor.BLUE).withVisibility { page.value == Page.THREE })

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

    fun infoContents(): String {
        val contents = ArrayList<String>()

        contents.apply {
            if (version.value) addContent(KamiMod.KAMI_KATAKANA, KamiMod.VERSION_SIMPLE)
            if (username.value) addContent("Welcome", "${mc.session.username}!")
            if (time.value) add(TimeUtils.getFinalTime(secondColor.value.textFormatting, firstColor.value.textFormatting, timeUnitSetting.value, timeTypeSetting.value, doLocale.value))
            if (tps.value) addContent("${InfoCalculator.tps(decimalPlaces.value)}", "tps")
            if (fps.value) addContent("${Minecraft.getDebugFPS()}", "fps")
            if (speed.value) addContent("${calcSpeed(decimalPlaces.value)}", speedUnit.value.displayName)
            if (timerSpeed.value) addContent("${round(50f / mc.timer.tickLength, decimalPlaces.value)}", "x")
            if (ping.value) addContent("${InfoCalculator.ping()}", "ms")
            if (server.value) addContent(mc.player.serverBrand)
            if (durability.value) addContent("${InfoCalculator.heldItemDurability()}", "dura")
            if (biome.value) addContent(mc.world.getBiome(mc.player.position).biomeName, "biome")
            if (memory.value) addContent("${InfoCalculator.memory()}", "MB")
            if (totems.value) addContent("${InventoryUtils.countItemAll(449)}", "totems")
            if (endCrystals.value) addContent("${InventoryUtils.countItemAll(426)}", "crystals")
            if (expBottles.value) addContent("${InventoryUtils.countItemAll(384)}", "exp")
            if (godApples.value) addContent("${InventoryUtils.countItemAll(322)}", "gaps")
        }

        return contents.joinToString(separator = System.lineSeparator())
    }

    private fun ArrayList<String>.addContent(first: String, second: String? = null) {
        var string = "${firstColor.value.textFormatting}$first"
        if (second != null) string += "${secondColor.value.textFormatting}$second"
        add(string)
    }

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