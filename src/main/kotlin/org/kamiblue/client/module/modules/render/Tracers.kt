package org.kamiblue.client.module.modules.render

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.events.RenderWorldEvent
import org.kamiblue.client.manager.managers.FriendManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.EntityUtils.getTargetList
import org.kamiblue.client.util.EntityUtils.isNeutral
import org.kamiblue.client.util.EntityUtils.isPassive
import org.kamiblue.client.util.and
import org.kamiblue.client.util.atTrue
import org.kamiblue.client.util.atValue
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.color.DyeColors
import org.kamiblue.client.util.color.HueCycler
import org.kamiblue.client.util.graphics.ESPRenderer
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.commons.utils.MathUtils.convertRange
import org.kamiblue.event.listener.listener
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

internal object Tracers : Module(
    name = "Tracers",
    description = "Draws lines to other living entities",
    category = Category.RENDER
) {
    private val page = setting("Page", Page.ENTITY_TYPE)

    /* Entity type settings */
    private val players = setting("Players", true, page.atValue(Page.ENTITY_TYPE))
    private val friends = setting("Friends", false, page.atValue(Page.ENTITY_TYPE) and players.atTrue())
    private val sleeping = setting("Sleeping", false, page.atValue(Page.ENTITY_TYPE) and players.atTrue())
    private val mobs = setting("Mobs", true, page.atValue(Page.ENTITY_TYPE))
    private val passive = setting("Passive Mobs", false, page.atValue(Page.ENTITY_TYPE) and mobs.atTrue())
    private val neutral = setting("Neutral Mobs", true, page.atValue(Page.ENTITY_TYPE) and mobs.atTrue())
    private val hostile = setting("Hostile Mobs", true, page.atValue(Page.ENTITY_TYPE) and mobs.atTrue())
    private val invisible = setting("Invisible", true, page.atValue(Page.ENTITY_TYPE))
    private val range = setting("Range", 64, 8..512, 8, page.atValue(Page.ENTITY_TYPE))

    /* Color settings */
    private val colorPlayer = setting("Player Color", DyeColors.KAMI, page.atValue(Page.COLOR))
    private val colorFriend = setting("Friend Color", DyeColors.RAINBOW, page.atValue(Page.COLOR))
    private val colorPassive = setting("Passive Mob Color", DyeColors.GREEN, page.atValue(Page.COLOR))
    private val colorNeutral = setting("Neutral Mob Color", DyeColors.YELLOW, page.atValue(Page.COLOR))
    private val colorHostile = setting("Hostile Mob Color", DyeColors.RED, page.atValue(Page.COLOR))
    private val colorFar = setting("Far Color", DyeColors.WHITE, page.atValue(Page.COLOR))

    /* General rendering settings */
    private val rangedColor = setting("Ranged Color", true, page.atValue(Page.RENDERING))
    private val colorChangeRange = setting("Color Change Range", 16, 8..128, 8, page.atValue(Page.RENDERING) and rangedColor.atTrue())
    private val playerOnly = setting("Player Only", true, page.atValue(Page.RENDERING) and rangedColor.atTrue())
    private val aFar = setting("Far Alpha", 127, 0..255, 1, page.atValue(Page.RENDERING) and rangedColor.atTrue())
    private val a = setting("Tracer Alpha", 255, 0..255, 1, page.atValue(Page.RENDERING))
    private val yOffset = setting("y Offset Percentage", 0, 0..100, 5, page.atValue(Page.RENDERING))
    private val thickness = setting("Line Thickness", 2.0f, 0.25f..5.0f, 0.25f, page.atValue(Page.RENDERING))

    private enum class Page {
        ENTITY_TYPE, COLOR, RENDERING
    }

    private var renderList = ConcurrentHashMap<Entity, Pair<ColorHolder, Float>>() /* <Entity, <RGBAColor, AlphaMultiplier>> */
    private var cycler = HueCycler(600)
    private val renderer = ESPRenderer()

    init {
        listener<RenderWorldEvent> {
            renderer.aTracer = a.value
            renderer.thickness = thickness.value
            renderer.tracerOffset = yOffset.value
            for ((entity, pair) in renderList) {
                val rgba = pair.first.clone()
                rgba.a = (rgba.a * pair.second).toInt()
                renderer.add(entity, rgba)
            }
            renderer.render(true)
        }

        safeListener<TickEvent.ClientTickEvent> {
            cycler++
            alwaysListening = renderList.isNotEmpty()

            val player = arrayOf(players.value, friends.value, sleeping.value)
            val mob = arrayOf(mobs.value, passive.value, neutral.value, hostile.value)
            val entityList = if (isEnabled) {
                getTargetList(player, mob, invisible.value, range.value.toFloat(), ignoreSelf = false)
            } else {
                ArrayList()
            }

            val cacheMap = HashMap<Entity, Pair<ColorHolder, Float>>()
            for (entity in entityList) {
                cacheMap[entity] = Pair(getColor(entity), 0f)
            }

            for ((entity, pair) in renderList) {
                cacheMap.computeIfPresent(entity) { _, cachePair -> Pair(cachePair.first, min(pair.second + 0.075f, 1f)) }
                cacheMap.computeIfAbsent(entity) { Pair(getColor(entity), pair.second - 0.05f) }
                if (pair.second < 0f) cacheMap.remove(entity)
            }
            renderList.clear()
            renderList.putAll(cacheMap)
        }
    }

    private fun getColor(entity: Entity): ColorHolder {
        val color = when {
            FriendManager.isFriend(entity.name) -> colorFriend.value
            entity is EntityPlayer -> colorPlayer.value
            entity.isPassive -> colorPassive.value
            entity.isNeutral -> colorNeutral.value
            else -> colorHostile.value
        }.color

        return if (color == DyeColors.RAINBOW.color) {
            getRangedColor(entity, cycler.currentRgba(a.value))
        } else {
            color.a = a.value
            getRangedColor(entity, color)
        }
    }

    private fun getRangedColor(entity: Entity, rgba: ColorHolder): ColorHolder {
        if (!rangedColor.value || playerOnly.value && entity !is EntityPlayer) return rgba
        val distance = mc.player.getDistance(entity)
        val colorFar = colorFar.value.color
        colorFar.a = aFar.value
        val r = convertRange(distance, 0f, colorChangeRange.value.toFloat(), rgba.r.toFloat(), colorFar.r.toFloat()).toInt()
        val g = convertRange(distance, 0f, colorChangeRange.value.toFloat(), rgba.g.toFloat(), colorFar.g.toFloat()).toInt()
        val b = convertRange(distance, 0f, colorChangeRange.value.toFloat(), rgba.b.toFloat(), colorFar.b.toFloat()).toInt()
        val a = convertRange(distance, 0f, colorChangeRange.value.toFloat(), a.value.toFloat(), colorFar.a.toFloat()).toInt()
        return ColorHolder(r, g, b, a)
    }
}
