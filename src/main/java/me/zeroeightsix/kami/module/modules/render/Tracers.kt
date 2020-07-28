package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.EntityUtils.getTargetList
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.HueCycler
import me.zeroeightsix.kami.util.KamiTessellator
import me.zeroeightsix.kami.util.MathsUtils.convertRange
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
    private val range = register(Settings.integerBuilder("Range").withValue(64).withRange(1, 256).withVisibility { page.value == Page.ENTITY_TYPE }.build())

    /* Rendering settings */
    private val rangedColor = register(Settings.booleanBuilder("RangedColor").withValue(true).withVisibility { page.value == Page.RENDERING }.build())
    private val playerOnly = register(Settings.booleanBuilder("PlayerOnly").withValue(true).withVisibility { page.value == Page.RENDERING && rangedColor.value }.build())
    private val rFar = register(Settings.integerBuilder("RedFar").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING && rangedColor.value }.build())
    private val gFar = register(Settings.integerBuilder("GreenFar").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING && rangedColor.value }.build())
    private val bFar = register(Settings.integerBuilder("BlueFar").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING && rangedColor.value }.build())
    private val aFar = register(Settings.integerBuilder("AlphaFar").withValue(127).withRange(0, 255).withVisibility { page.value == Page.RENDERING && rangedColor.value }.build())
    private val r = register(Settings.integerBuilder("Red").withValue(155).withRange(0, 255).withVisibility { page.value == Page.RENDERING }.build())
    private val g = register(Settings.integerBuilder("Green").withValue(144).withRange(0, 255).withVisibility { page.value == Page.RENDERING }.build())
    private val b = register(Settings.integerBuilder("Blue").withValue(255).withRange(0, 255).withVisibility { page.value == Page.RENDERING }.build())
    private val a = register(Settings.integerBuilder("Alpha").withValue(200).withRange(0, 255).withVisibility { page.value == Page.RENDERING }.build())
    private val thickness = register(Settings.floatBuilder("LineThickness").withValue(2.0f).withRange(0.0f, 8.0f).withVisibility { page.value == Page.RENDERING }.build())

    private enum class Page {
        ENTITY_TYPE, RENDERING
    }

    private var entityList = ConcurrentHashMap<Entity, Float>()
    private var cycler = HueCycler(3600)

    override fun onWorldRender(event: RenderEvent) {
        val pTicks = event.partialTicks
        for ((entity, alpha) in entityList) {
            val rgba = getColour(entity)
            if (rgba[0] != -1) {
                KamiTessellator.drawLineToEntity(entity, rgba[0], rgba[1], rgba[2], (rgba[3] * alpha).toInt(), pTicks, thickness.value)
            } else {
                KamiTessellator.drawLineToEntity(entity, cycler.current(), (rgba[3] * alpha).toInt(), pTicks, thickness.value)
            }
        }
    }

    override fun onUpdate() {
        alwaysListening = entityList.isNotEmpty()
        cycler.next()
        val player = arrayOf(players.value, friends.value, sleeping.value)
        val mob = arrayOf(mobs.value, passive.value, neutral.value, hostile.value)
        val cacheList = if (isEnabled) {
            getTargetList(player, mob, true, false, range.value.toFloat())
        } else {
            emptyArray()
        }
        val cacheMap = HashMap<Entity, Float>()
        for (entity in cacheList) {
            cacheMap[entity] = 0f
        }
        for ((entity, alpha) in entityList) {
            cacheMap.computeIfPresent(entity) { _, _ -> min(alpha + 0.07f, 1f) }
            cacheMap.computeIfAbsent(entity) { alpha - 0.05f }
            if (alpha < 0f) cacheMap.remove(entity)
        }
        entityList.clear()
        entityList.putAll(cacheMap)
    }

    private fun getColour(entity: Entity): Array<Int> {
        val rgb: Triple<Int, Int, Int> = when {
            Friends.isFriend(entity.name) -> {
                val a = convertRange(mc.player.getDistance(entity), 0f, range.value.toFloat(), a.value.toFloat(), aFar.value.toFloat()).toInt()
                return arrayOf(-1, -1, -1, a)
            }

            entity is EntityPlayer -> Triple(r.value, g.value, b.value)

            EntityUtils.isPassiveMob(entity) -> Triple(0, 255, 0)

            EntityUtils.isCurrentlyNeutral(entity) -> Triple(255, 255, 0)

            else -> Triple(255, 0, 0)
        }
        return getRangedColour(entity, rgb)
    }

    private fun getRangedColour(entity: Entity, rgb: Triple<Int, Int, Int>): Array<Int> {
        if (!rangedColor.value || playerOnly.value && entity !is EntityPlayer) return arrayOf(rgb.first, rgb.second, rgb.third, a.value)
        val distance = mc.player.getDistance(entity)
        val r = convertRange(distance, 0f, range.value.toFloat(), rgb.first.toFloat(), rFar.value.toFloat()).toInt()
        val g = convertRange(distance, 0f, range.value.toFloat(), rgb.second.toFloat(), gFar.value.toFloat()).toInt()
        val b = convertRange(distance, 0f, range.value.toFloat(), rgb.third.toFloat(), bFar.value.toFloat()).toInt()
        val a = convertRange(distance, 0f, range.value.toFloat(), a.value.toFloat(), aFar.value.toFloat()).toInt()
        return arrayOf(r, g, b, a)
    }
}