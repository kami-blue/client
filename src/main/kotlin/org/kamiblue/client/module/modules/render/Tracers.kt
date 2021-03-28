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
    private val page by setting("Page", Page.ENTITY_TYPE)

    /* Entity type settings */
    private val players by setting("Players", true, { page == Page.ENTITY_TYPE })
    private val friends by setting("Friends", false, { page == Page.ENTITY_TYPE && players })
    private val sleeping by setting("Sleeping", false, { page == Page.ENTITY_TYPE && players })
    private val mobs by setting("Mobs", true, { page == Page.ENTITY_TYPE })
    private val passive by setting("Passive Mobs", false, { page == Page.ENTITY_TYPE && mobs })
    private val neutral by setting("Neutral Mobs", true, { page == Page.ENTITY_TYPE && mobs })
    private val hostile by setting("Hostile Mobs", true, { page == Page.ENTITY_TYPE && mobs })
    private val invisible by setting("Invisible", true, { page == Page.ENTITY_TYPE })
    private val range by setting("Range", 64, 8..512, 8, { page == Page.ENTITY_TYPE })

    /* Color settings */
    private val colorPlayer by setting("Player Color", ColorHolder(155, 144, 255), false, { page == Page.COLOR })
    private val colorFriend by setting("Friend Color", ColorHolder(32, 250, 32), false, { page == Page.COLOR })
    private val colorPassive by setting("Passive Mob Color", ColorHolder(132, 240, 32), false, { page == Page.COLOR })
    private val colorNeutral by setting("Neutral Mob Color", ColorHolder(255, 232, 0), false, { page == Page.COLOR })
    private val colorHostile by setting("Hostile Mob Color", ColorHolder(250, 32, 32), false, { page == Page.COLOR })
    private val colorFar by setting("Far Color", ColorHolder(255, 255, 255), false, { page == Page.COLOR })

    /* General rendering settings */
    private val alpha by setting("Alpha", 255, 0..255, 1, { page == Page.RENDERING })
    private val yOffset by setting("Y Offset Percentage", 0, 0..100, 5, { page == Page.RENDERING })
    private val thickness by setting("Line Thickness", 2.0f, 0.25f..5.0f, 0.25f, { page == Page.RENDERING })

    /* Range color settings */
    private val rangedColor by setting("Ranged Color", true, { page == Page.RANGE_COLOR })
    private val colorChangeRange by setting("Color Change Range", 16, 8..128, 8, { page == Page.RANGE_COLOR && rangedColor })
    private val playerOnly by setting("Player Only", true, { page == Page.RANGE_COLOR && rangedColor })
    private val alphaFar by setting("Far Alpha", 127, 0..255, 1, { page == Page.RANGE_COLOR && rangedColor })

    private enum class Page {
        ENTITY_TYPE, COLOR, RENDERING, RANGE_COLOR
    }

    private var renderList = ConcurrentHashMap<Entity, Pair<ColorHolder, Float>>() /* <Entity, <RGBAColor, AlphaMultiplier>> */
    private var cycler = HueCycler(600)
    private val renderer = ESPRenderer()

    init {
        listener<RenderWorldEvent> {
            renderer.aTracer = alpha
            renderer.thickness = thickness
            renderer.tracerOffset = yOffset

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

            val player = arrayOf(players, friends, sleeping)
            val mob = arrayOf(mobs, passive, neutral, hostile)
            val entityList = if (isEnabled) {
                getTargetList(player, mob, invisible, range.toFloat(), ignoreSelf = false)
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

                if (pair.second < 0f) {
                    cacheMap.remove(entity)
                }
            }

            renderList.clear()
            renderList.putAll(cacheMap)
        }
    }

    private fun getColor(entity: Entity): ColorHolder {
        val color = when {
            FriendManager.isFriend(entity.name) -> colorFriend
            entity is EntityPlayer -> colorPlayer
            entity.isPassive -> colorPassive
            entity.isNeutral -> colorNeutral
            else -> colorHostile
        }

        return getRangedColor(entity, color)
    }

    private fun getRangedColor(entity: Entity, c: ColorHolder): ColorHolder {
        if (!rangedColor || playerOnly && entity !is EntityPlayer) return c
        val distance = mc.player.getDistance(entity)

        val r = convertRange(distance, 0f, colorChangeRange.toFloat(), c.r.toFloat(), colorFar.r.toFloat()).toInt()
        val g = convertRange(distance, 0f, colorChangeRange.toFloat(), c.g.toFloat(), colorFar.g.toFloat()).toInt()
        val b = convertRange(distance, 0f, colorChangeRange.toFloat(), c.b.toFloat(), colorFar.b.toFloat()).toInt()
        val a = convertRange(distance, 0f, colorChangeRange.toFloat(), alpha.toFloat(), alphaFar.toFloat()).toInt()
        return ColorHolder(r, g, b, a)
    }
}
