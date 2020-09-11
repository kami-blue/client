package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.TimeUtils
import me.zeroeightsix.kami.util.TimeUtils.getFinalTime
import me.zeroeightsix.kami.util.color.ColorTextFormatting
import me.zeroeightsix.kami.util.color.ColorTextFormatting.ColourCode
import me.zeroeightsix.kami.util.math.MathUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendDisableMessage
import net.minecraft.client.Minecraft
import net.minecraft.util.text.TextFormatting
import java.util.*
import kotlin.math.max

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 04/12/19
 * PVP Information by Polymer on 04/03/20
 * Updated by dominikaaaa on 25/03/20
 * Updated by Xiaro on 10/09/20
 */
@Module.Info(
        name = "InfoOverlay",
        category = Module.Category.CLIENT,
        description = "Configures the game information overlay",
        showOnArray = Module.ShowOnArray.OFF
)
@Suppress("UNCHECKED_CAST")
class InfoOverlay : Module() {
    /* This is so horrible but there's no other way */
    private val page = register(Settings.enumBuilder(Page::class.java).withName("Page").withValue(Page.ONE))

    /* Page One */
    private val version = register(Settings.booleanBuilder("Version").withValue(true).withVisibility { page.value == Page.ONE })
    private val username = register(Settings.booleanBuilder("Username").withValue(true).withVisibility { page.value == Page.ONE })
    private val tps = register(Settings.booleanBuilder("TPS").withValue(true).withVisibility { page.value == Page.ONE })
    private val fps = register(Settings.booleanBuilder("FPS").withValue(true).withVisibility { page.value == Page.ONE })
    private val ping = register(Settings.booleanBuilder("Ping").withValue(false).withVisibility { page.value == Page.ONE })
    private val durability = register(Settings.booleanBuilder("ItemDamage").withValue(false).withVisibility { page.value == Page.ONE })
    private val biome = register(Settings.booleanBuilder("Biome").withValue(false).withVisibility { page.value == Page.ONE })
    private val memory = register(Settings.booleanBuilder("RAMUsed").withValue(false).withVisibility { page.value == Page.ONE })
    private val timerSpeed = register(Settings.booleanBuilder("TimerSpeed").withValue(false).withVisibility { page.value == Page.ONE })

    /* Page Two */
    private val totems = register(Settings.booleanBuilder("Totems").withValue(false).withVisibility { page.value == Page.TWO })
    private val endCrystals = register(Settings.booleanBuilder("EndCrystals").withValue(false).withVisibility { page.value == Page.TWO })
    private val expBottles = register(Settings.booleanBuilder("EXPBottles").withValue(false).withVisibility { page.value == Page.TWO })
    private val godApples = register(Settings.booleanBuilder("GodApples").withValue(false).withVisibility { page.value == Page.TWO })

    /* Page Three */
    private val decimalPlaces = register(Settings.integerBuilder("DecimalPlaces").withValue(2).withMinimum(0).withMaximum(10).withVisibility { page.value == Page.THREE })
    private val speed = register(Settings.booleanBuilder("Speed").withValue(true).withVisibility { page.value == Page.THREE })
    private val averageSpeed = register(Settings.floatBuilder("AverageSpeedTime(s)").withValue(1f).withRange(0f, 5f).withVisibility { page.value == Page.THREE && speed.value })
    private val speedUnit = register(Settings.enumBuilder(SpeedUnit::class.java).withName("SpeedUnit").withValue(SpeedUnit.KMH).withVisibility { page.value == Page.THREE && speed.value }) as Setting<SpeedUnit>
    private val time = register(Settings.booleanBuilder("Time").withValue(true).withVisibility { page.value == Page.THREE })
    @JvmField val timeTypeSetting = register(Settings.enumBuilder(TimeUtils.TimeType::class.java).withName("TimeFormat").withValue(TimeUtils.TimeType.HHMMSS).withVisibility { page.value == Page.THREE && time.value }) as Setting<TimeUtils.TimeType>
    @JvmField val timeUnitSetting = register(Settings.enumBuilder(TimeUtils.TimeUnit::class.java).withName("TimeUnit").withValue(TimeUtils.TimeUnit.H12).withVisibility { page.value == Page.THREE && time.value }) as Setting<TimeUtils.TimeUnit>
    @JvmField val doLocale = register(Settings.booleanBuilder("TimeShowAM/PM").withValue(true).withVisibility { page.value == Page.THREE && time.value && timeUnitSetting.value == TimeUtils.TimeUnit.H12 })
    @JvmField val firstColour = register(Settings.enumBuilder(ColourCode::class.java).withName("FirstColour").withValue(ColourCode.WHITE).withVisibility { page.value == Page.THREE }) as Setting<ColourCode>
    @JvmField val secondColour = register(Settings.enumBuilder(ColourCode::class.java).withName("SecondColour").withValue(ColourCode.BLUE).withVisibility { page.value == Page.THREE }) as Setting<ColourCode>

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

    public override fun onDisable() {
        sendDisableMessage(this.javaClass)
    }

    fun infoContents(): ArrayList<String> {
        val infoContents = ArrayList<String>()
        if (version.value) {
            infoContents.add(getStringColour(setToText(firstColour.value)) + KamiMod.KAMI_KANJI + getStringColour(setToText(secondColour.value)) + " " + KamiMod.VER_SMALL)
        }
        if (username.value) {
            infoContents.add(getStringColour(setToText(firstColour.value)) + "Welcome" + getStringColour(setToText(secondColour.value)) + " " + mc.getSession().username + "!")
        }
        if (time.value) {
            infoContents.add(getStringColour(setToText(firstColour.value)) + getFinalTime(setToText(secondColour.value), setToText(firstColour.value), timeUnitSetting.value, timeTypeSetting.value, doLocale.value))
        }
        if (tps.value) {
            infoContents.add(getStringColour(setToText(firstColour.value)) + InfoCalculator.tps(decimalPlaces.value) + getStringColour(setToText(secondColour.value)) + " tps")
        }
        if (fps.value) {
            infoContents.add(getStringColour(setToText(firstColour.value)) + Minecraft.debugFPS + getStringColour(setToText(secondColour.value)) + " fps")
        }
        if (speed.value) {
            infoContents.add(getStringColour(setToText(firstColour.value)) + calcSpeed() + getStringColour(setToText(secondColour.value)) + " " + speedUnit.value.displayName)
        }
        if (timerSpeed.value) {
            infoContents.add(getStringColour(setToText(firstColour.value)) + 50f / mc.timer.tickLength + getStringColour(setToText(secondColour.value)) + " x")
        }
        if (ping.value) {
            infoContents.add(getStringColour(setToText(firstColour.value)) + InfoCalculator.ping() + getStringColour(setToText(secondColour.value)) + " ms")
        }
        if (durability.value) {
            infoContents.add(getStringColour(setToText(firstColour.value)) + InfoCalculator.dura() + getStringColour(setToText(secondColour.value)) + " dura")
        }
        if (biome.value) {
            infoContents.add(getStringColour(setToText(firstColour.value)) + mc.world.getBiome(mc.player.position).biomeName + getStringColour(setToText(secondColour.value)) + " biome")
        }
        if (memory.value) {
            infoContents.add(getStringColour(setToText(firstColour.value)) + InfoCalculator.memory() + getStringColour(setToText(secondColour.value)) + " mB free")
        }
        if (totems.value) {
            infoContents.add(getStringColour(setToText(firstColour.value)) + InventoryUtils.countItemAll(449) + getStringColour(setToText(secondColour.value)) + " Totems")
        }
        if (endCrystals.value) {
            infoContents.add(getStringColour(setToText(firstColour.value)) + InventoryUtils.countItemAll(426) + getStringColour(setToText(secondColour.value)) + " Crystals")
        }
        if (expBottles.value) {
            infoContents.add(getStringColour(setToText(firstColour.value)) + InventoryUtils.countItemAll(384) + getStringColour(setToText(secondColour.value)) + " EXP Bottles")
        }
        if (godApples.value) {
            infoContents.add(getStringColour(setToText(firstColour.value)) + InventoryUtils.countItemAll(322) + getStringColour(setToText(secondColour.value)) + " God Apples")
        }
        return infoContents
    }

    private fun setToText(colourCode: ColourCode): TextFormatting {
        return ColorTextFormatting.toTextMap[colourCode]!!
    }

    fun calcSpeed(): String {
        updateSpeedList()
        val averageSpeed = if (speedList.isEmpty()) 0.0 else (speedList.sum() / speedList.size.toDouble())
        return MathUtils.round(averageSpeed, decimalPlaces.value).toString()
    }

    private fun updateSpeedList() {
        val speed = InfoCalculator.speed(speedUnit.value == SpeedUnit.KMH)
        if (speed > 0.0 || mc.player.ticksExisted % 4 == 0) speedList.add(speed) // Only adding it every 4 ticks if speed is 0
        else speedList.poll()
        while (speedList.size > max((averageSpeed.value * 20).toInt(), 1)) speedList.poll()
    }

    companion object {
        @JvmStatic
        fun getStringColour(c: TextFormatting): String {
            return c.toString()
        }
    }
}