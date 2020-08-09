package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.EntityUtils.getTargetList
import me.zeroeightsix.kami.util.MathsUtils.convertRange
import me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

/**
 * Created by 086 on 11/12/2017.
 * Kurisu Makise is best girl
 * Updated by Afel on 08/06/20
 * Updated by Xiaro on 28/07/20
 */
@Module.Info(
        name = "Tracers",
        description = "Draws lines to other living entities",
        category = Module.Category.RENDER
)
class Tracers : Module() {
    private val page = register(Settings.e<Page>("Page", Page.ENTITY_TYPE))

    /* Entity type settings */
    private val players = register(Settings.booleanBuilder("Players").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE }.build())
    private val friends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && players.value }.build())
    private val sleeping = register(Settings.booleanBuilder("Sleeping").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && players.value }.build())
    private val mobs = register(Settings.booleanBuilder("Mobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE }.build())
    private val passive = register(Settings.booleanBuilder("PassiveMobs").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && mobs.value }.build())
    private val neutral = register(Settings.booleanBuilder("NeutralMobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && mobs.value }.build())
    private val hostile = register(Settings.booleanBuilder("HostileMobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && mobs.value }.build())
    private val invisible = register(Settings.booleanBuilder("Invisible").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE }.build())
    private val range = register(Settings.integerBuilder("Range").withValue(64).withRange(1, 256).withVisibility { page.value == Page.ENTITY_TYPE }.build())

    /* Rendering settings */
    private val rangedColor = register(Settings.booleanBuilder("RangedColor").withValue(true).withVisibility { page.value == Page.RENDERING }.build())
    private val playerOnly = register(Settings.booleanBuilder("PlayerOnly").withValue(true).withVisibility { page.value == Page.RENDERING && rangedColor.value }.build())
    private val rFar = register(Settings.integerBuilder("RedFar").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING && rangedColor.value }.build())
    private val gFar = register(Settings.integerBuilder("GreenFar").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING && rangedColor.value }.build())
    private val bFar = register(Settings.integerBuilder("BlueFar").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING && rangedColor.value }.build())
    private val aFar = register(Settings.integerBuilder("AlphaFar").withValue(127).withRange(0, 255).withVisibility { page.value == Page.RENDERING && rangedColor.value }.build())
    private val r = register(Settings.integerBuilder("RedPlayer").withValue(155).withRange(0, 255).withVisibility { page.value == Page.RENDERING }.build())
    private val g = register(Settings.integerBuilder("GreenPlayer").withValue(144).withRange(0, 255).withVisibility { page.value == Page.RENDERING }.build())
    private val b = register(Settings.integerBuilder("BluePlayer").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING }.build())
    private val a = register(Settings.integerBuilder("Alpha").withValue(200).withRange(0, 255).withVisibility { page.value == Page.RENDERING }.build())
    private val yOffset = register(Settings.integerBuilder("yOffsetPercentage").withValue(0).withRange(0, 100).withVisibility { page.value == Page.RENDERING }.build())
    private val thickness = register(Settings.floatBuilder("LineThickness").withValue(2.0f).withRange(0.0f, 8.0f).withVisibility { page.value == Page.RENDERING }.build())

    private enum class Page {
        ENTITY_TYPE, RENDERING
    }

    private var renderList = ConcurrentHashMap<Entity, Pair<ColourHolder, Float>>() /* <Entity, <ColourRGBA, AlphaMultiplier>> */
    private var cycler = HueCycler(200)

    override fun onWorldRender(event: RenderEvent) {
        val renderer = ESPRenderer(event.partialTicks)
        renderer.aTracer = a.value
        renderer.thickness = thickness.value
        renderer.tracerOffset = yOffset.value
        for ((entity, pair) in renderList) {
            val rgba = pair.first.clone()
            rgba.a = (rgba.a * pair.second).toInt()
            renderer.add(entity, rgba)
        }
        renderer.render()
    }

    override fun onUpdate() {
        alwaysListening = renderList.isNotEmpty()
        cycler.next()

        val player = arrayOf(players.value, friends.value, sleeping.value)
        val mob = arrayOf(mobs.value, passive.value, neutral.value, hostile.value)
        val entityList = if (isEnabled) {
            getTargetList(player, mob, true, invisible.value, range.value.toFloat())
        } else {
            emptyArray()
        }

        val cacheMap = HashMap<Entity, Pair<ColourHolder, Float>>()
        for (entity in entityList) {
            cacheMap[entity] = Pair(getColour(entity), 0f)
        }

        for ((entity, pair) in renderList) {
            cacheMap.computeIfPresent(entity) { _, cachePair -> Pair(cachePair.first, min(pair.second + 0.07f, 1f)) }
            cacheMap.computeIfAbsent(entity) { Pair(getColour(entity), pair.second - 0.05f) }
            if (pair.second < 0f) cacheMap.remove(entity)
        }
        renderList.clear()
        renderList.putAll(cacheMap)
    }

    private fun getColour(entity: Entity): ColourHolder {
        val rgb = when {
            Friends.isFriend(entity.name) -> {
                val colorInt = cycler.current()
                ColourHolder((colorInt shr 16), (colorInt shr 8 and 255), (colorInt and 255))
            }

            entity is EntityPlayer -> ColourHolder(r.value, g.value, b.value)

            EntityUtils.isPassiveMob(entity) -> ColourHolder(0, 255, 0)

            EntityUtils.isCurrentlyNeutral(entity) -> ColourHolder(255, 255, 0)

            else -> ColourHolder(255, 0, 0)
        }
        return getRangedColour(entity, rgb)
    }

    private fun getRangedColour(entity: Entity, rgba: ColourHolder): ColourHolder {
        if (!rangedColor.value || playerOnly.value && entity !is EntityPlayer) return rgba
        val distance = mc.player.getDistance(entity)
        val r = convertRange(distance, 0f, range.value.toFloat(), rgba.r.toFloat(), rFar.value.toFloat()).toInt()
        val g = convertRange(distance, 0f, range.value.toFloat(), rgba.g.toFloat(), gFar.value.toFloat()).toInt()
        val b = convertRange(distance, 0f, range.value.toFloat(), rgba.b.toFloat(), bFar.value.toFloat()).toInt()
        val a = convertRange(distance, 0f, range.value.toFloat(), a.value.toFloat(), aFar.value.toFloat()).toInt()
        return if (!Friends.isFriend(entity.name)) ColourHolder(r, g, b, a) else ColourHolder(rgba.r, rgba.g, rgba.b, a)
    }
}