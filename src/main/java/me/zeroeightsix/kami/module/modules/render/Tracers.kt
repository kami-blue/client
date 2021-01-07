package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.EntityUtils.getTargetList
import me.zeroeightsix.kami.util.EntityUtils.isNeutral
import me.zeroeightsix.kami.util.EntityUtils.isPassive
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.color.DyeColors
import me.zeroeightsix.kami.util.color.HueCycler
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.utils.MathUtils.convertRange
import org.kamiblue.event.listener.listener
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

object Tracers : Module(
    name = "Tracers",
    description = "Draws lines to other living entities",
    category = Category.RENDER
) {
    private val page = setting("Page", Page.ENTITY_TYPE)

    /* Entity type settings */
    private val players = setting("Players", true, { page.value == Page.ENTITY_TYPE })
    private val friends = setting("Friends", false, { page.value == Page.ENTITY_TYPE && players.value })
    private val sleeping = setting("Sleeping", false, { page.value == Page.ENTITY_TYPE && players.value })
    private val mobs = setting("Mobs", true, { page.value == Page.ENTITY_TYPE })
    private val passive = setting("PassiveMobs", false, { page.value == Page.ENTITY_TYPE && mobs.value })
    private val neutral = setting("NeutralMobs", true, { page.value == Page.ENTITY_TYPE && mobs.value })
    private val hostile = setting("HostileMobs", true, { page.value == Page.ENTITY_TYPE && mobs.value })
    private val invisible = setting("Invisible", true, { page.value == Page.ENTITY_TYPE })
    private val range = setting("Range", 64, 8..256, 8, { page.value == Page.ENTITY_TYPE })

    /* Color settings */
    private val colorPlayer = setting("PlayerColor", DyeColors.KAMI, { page.value == Page.COLOR })
    private val colorFriend = setting("FriendColor", DyeColors.RAINBOW, { page.value == Page.COLOR })
    private val colorPassive = setting("PassiveMobColor", DyeColors.GREEN, { page.value == Page.COLOR })
    private val colorNeutral = setting("NeutralMobColor", DyeColors.YELLOW, { page.value == Page.COLOR })
    private val colorHostile = setting("HostileMobColor", DyeColors.RED, { page.value == Page.COLOR })

    /* General rendering settings */
    private val rangedColor = setting("RangedColor", true, { page.value == Page.RENDERING })
    private val colorChangeRange = setting("ColorChangeRange", 16, 8..128, 8, { page.value == Page.RENDERING && rangedColor.value })
    private val playerOnly = setting("PlayerOnly", true, { page.value == Page.RENDERING && rangedColor.value })
    private val colorFar = setting("FarColor", DyeColors.WHITE, { page.value == Page.COLOR })
    private val aFar = setting("FarAlpha", 127, 0..255, 1, { page.value == Page.RENDERING && rangedColor.value })
    private val a = setting("TracerAlpha", 255, 0..255, 1, { page.value == Page.RENDERING })
    private val yOffset = setting("yOffsetPercentage", 0, 0..100, 5, { page.value == Page.RENDERING })
    private val thickness = setting("LineThickness", 2.0f, 0.25f..5.0f, 0.25f, { page.value == Page.RENDERING })

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