package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtils.getInterpolatedAmount
import me.zeroeightsix.kami.util.EntityUtils.getTargetList
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.RayTraceResult
import org.lwjgl.opengl.GL11.GL_LINES
import kotlin.math.min

@Module.Info(
        name = "EyeFinder",
        description = "Draw lines from entity's heads to where they are looking",
        category = Module.Category.RENDER
)
object EyeFinder : Module() {
    private val page = register(Settings.e<Page>("Page", Page.ENTITY_TYPE))

    /* Entity type settings */
    private val players = register(Settings.booleanBuilder("Players").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE })
    private val friends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && players.value })
    private val sleeping = register(Settings.booleanBuilder("Sleeping").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && players.value })
    private val mobs = register(Settings.booleanBuilder("Mobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE })
    private val passive = register(Settings.booleanBuilder("PassiveMobs").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && mobs.value })
    private val neutral = register(Settings.booleanBuilder("NeutralMobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && mobs.value })
    private val hostile = register(Settings.booleanBuilder("HostileMobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && mobs.value })
    private val invisible = register(Settings.booleanBuilder("Invisible").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE })
    private val range = register(Settings.integerBuilder("Range").withValue(64).withRange(1, 128).withVisibility { page.value == Page.ENTITY_TYPE })

    /* Rendering settings */
    private val r = register(Settings.integerBuilder("Red").withValue(155).withRange(0, 255).withStep(1).withVisibility { page.value == Page.RENDERING })
    private val g = register(Settings.integerBuilder("Green").withValue(144).withRange(0, 255).withStep(1).withVisibility { page.value == Page.RENDERING })
    private val b = register(Settings.integerBuilder("Blue").withValue(255).withRange(0, 255).withStep(1).withVisibility { page.value == Page.RENDERING })
    private val a = register(Settings.integerBuilder("Alpha").withValue(200).withRange(0, 255).withStep(1).withVisibility { page.value == Page.RENDERING })
    private val thickness = register(Settings.floatBuilder("Thickness").withValue(2.0f).withRange(0.25f, 5.0f).withStep(0.25f).withVisibility { page.value == Page.RENDERING })


    private enum class Page {
        ENTITY_TYPE, RENDERING
    }

    private val resultMap = HashMap<Entity, Pair<RayTraceResult, Float>>()

    init {
        listener<RenderWorldEvent> {
            if (resultMap.isEmpty()) return@listener
            for ((entity, pair) in resultMap) {
                drawLine(entity, pair)
            }
        }

        listener<SafeTickEvent> {
            alwaysListening = resultMap.isNotEmpty()

            val player = arrayOf(players.value, friends.value, sleeping.value)
            val mob = arrayOf(mobs.value, passive.value, neutral.value, hostile.value)
            val entityList = if (isEnabled) {
                getTargetList(player, mob, invisible.value, range.value.toFloat(), ignoreSelf = false)
            } else {
                ArrayList()
            }
            val cacheMap = HashMap<Entity, Pair<RayTraceResult, Float>>()
            for (entity in entityList) {
                val result = getRaytraceResult(entity) ?: continue
                cacheMap[entity] = Pair(result, 0.0f)
            }
            for ((entity, pair) in resultMap) {
                val result = getRaytraceResult(entity) ?: continue
                cacheMap.computeIfPresent(entity) { _, cachePair -> Pair(cachePair.first, min(pair.second + 0.07f, 1f)) }
                cacheMap.computeIfAbsent(entity) { Pair(result, pair.second - 0.05f) }
                if (pair.second < 0f) cacheMap.remove(entity)
            }
            resultMap.clear()
            resultMap.putAll(cacheMap)
        }
    }

    private fun getRaytraceResult(entity: Entity): RayTraceResult? {
        var result = entity.rayTrace(5.0, Minecraft.getMinecraft().renderPartialTicks)
                ?: return null /* Raytrace for block */
        if (result.typeOfHit == RayTraceResult.Type.MISS) { /* Raytrace for entity */
            val eyePos = entity.getPositionEyes(mc.renderPartialTicks)
            val entityLookVec = entity.getLook(mc.renderPartialTicks).scale(5.0)
            val entityLookEnd = eyePos.add(entityLookVec)
            for (otherEntity in mc.world.loadedEntityList) {
                if (otherEntity.getDistance(entity) > 10.0) continue /* Some entity has bigger bounding box */
                if (otherEntity == entity || otherEntity == mc.player) continue
                val box = otherEntity.boundingBox
                result = box.calculateIntercept(eyePos, entityLookEnd) ?: continue
                result.typeOfHit = RayTraceResult.Type.ENTITY
                result.entityHit = otherEntity
            }
        }
        return result
    }

    private fun drawLine(entity: Entity, pair: Pair<RayTraceResult, Float>) {
        val eyePos = entity.getPositionEyes(mc.renderPartialTicks)
        val result = pair.first
        val alpha = (a.value * pair.second).toInt()

        /* Render line */
        val buffer = KamiTessellator.buffer
        GlStateManager.glLineWidth(thickness.value)
        KamiTessellator.begin(GL_LINES)
        buffer.pos(eyePos.x, eyePos.y, eyePos.z).color(r.value, g.value, b.value, alpha).endVertex()
        buffer.pos(result.hitVec.x, result.hitVec.y, result.hitVec.z).color(r.value, g.value, b.value, alpha).endVertex()
        KamiTessellator.render()

        /* Render hit position */
        if (result.typeOfHit != RayTraceResult.Type.MISS) {
            val box = if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
                AxisAlignedBB(result.blockPos).grow(0.002)
            } else {
                val offset = getInterpolatedAmount(result.entityHit, KamiTessellator.pTicks())
                result.entityHit.renderBoundingBox.offset(offset)
            }
            val colour = ColorHolder(r.value, g.value, b.value)
            val renderer = ESPRenderer()
            renderer.aFilled = (alpha / 3)
            renderer.aOutline = alpha
            renderer.thickness = (thickness.value)
            renderer.through = false
            renderer.add(box, colour)
            renderer.render(true)
        }
    }
}