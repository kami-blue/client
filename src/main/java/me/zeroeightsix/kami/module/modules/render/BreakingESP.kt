package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.util.KamiLang 
import me.zeroeightsix.kami.event.events.BlockBreakEvent
import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.math.VectorUtils.distanceTo
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener

object BreakingESP : Module(
    name = KamiLang.get("module.modules.render.BreakingESP.Breakingesp"),
    description = KamiLang.get("module.modules.render.BreakingESP.HighlightsBlocksBeingBroken"),
    category = Category.RENDER
) {
    private val espSelf = setting(KamiLang.get("module.modules.render.BreakingESP.Espself"), true)
    private val warnSelf = setting(KamiLang.get("module.modules.render.BreakingESP.Warnself"), false)
    private val obsidianOnly = setting(KamiLang.get("module.modules.render.BreakingESP.Obsidianonly"), false)
    private val warning = setting(KamiLang.get("module.modules.render.BreakingESP.Warn"), false)
    private val warningProgress = setting(KamiLang.get("module.modules.render.BreakingESP.Warnprogress"), 4, 0..10, 1)
    private val chatWarn = setting(KamiLang.get("module.modules.render.BreakingESP.Chatwarning"), false)
    private val screenWarn = setting(KamiLang.get("module.modules.render.BreakingESP.Hudwarning"), true)
    private val soundWarn = setting(KamiLang.get("module.modules.render.BreakingESP.Soundwarning"), false)
    private val range = setting(KamiLang.get("module.modules.render.BreakingESP.Range"), 16.0f, 2.0f..32.0f, 2.0f)
    private val filled = setting(KamiLang.get("module.modules.render.BreakingESP.Filled"), true)
    private val outline = setting(KamiLang.get("module.modules.render.BreakingESP.Outline"), true)
    private val tracer = setting(KamiLang.get("module.modules.render.BreakingESP.Tracer"), false)
    private val r = setting(KamiLang.get("module.modules.render.BreakingESP.Red"), 255, 0..255, 1)
    private val g = setting(KamiLang.get("module.modules.render.BreakingESP.Green"), 255, 0..255, 1)
    private val b = setting(KamiLang.get("module.modules.render.BreakingESP.Blue"), 255, 0..255, 1)
    private val aFilled = setting(KamiLang.get("module.modules.render.BreakingESP.Filledalpha"), 31, 0..255, 1, { filled.value })
    private val aOutline = setting(KamiLang.get("module.modules.render.BreakingESP.Outlinealpha"), 200, 0..255, 1, { outline.value })
    private val aTracer = setting(KamiLang.get("module.modules.render.BreakingESP.Traceralpha"), 255, 0..255, 1, { outline.value })
    private val thickness = setting(KamiLang.get("module.modules.render.BreakingESP.Linethickness"), 2.0f, 0.25f..5.0f, 0.25f)

    private val breakingBlockList = LinkedHashMap<Int, Triple<BlockPos, Int, Pair<Boolean, Boolean>>>() /* <BreakerID, <Position, Progress, <Warned, Render>> */
    private var warn = false
    private var delay = 0
    private var warningText = ""

    init {
        listener<RenderWorldEvent> {
            val color = ColorHolder(r.value, g.value, b.value)
            val renderer = ESPRenderer()
            renderer.aFilled = if (filled.value) aFilled.value else 0
            renderer.aOutline = if (outline.value) aOutline.value else 0
            renderer.aTracer = if (tracer.value) aTracer.value else 0
            renderer.thickness = thickness.value

            var selfBreaking: AxisAlignedBB? = null
            for ((breakID, triple) in breakingBlockList) {
                if (triple.third.second) {
                    val box = mc.world.getBlockState(triple.first).getSelectedBoundingBox(mc.world, triple.first)
                    val progress = triple.second / 9f
                    val resizedBox = box.shrink((1f - progress) * box.averageEdgeLength * 0.5)
                    if (mc.world.getEntityByID(breakID) == mc.player) {
                        selfBreaking = resizedBox
                        continue
                    }
                    renderer.add(resizedBox, color)
                }
            }
            renderer.render(true)

            if (selfBreaking != null) {
                renderer.aTracer = 0
                renderer.add(selfBreaking, color)
                renderer.render(true)
            }
        }

        listener<RenderOverlayEvent> {
            if (screenWarn.value && warn) {
                if (delay++ > 100) warn = false
                val scaledResolution = ScaledResolution(mc)
                val posX = scaledResolution.scaledWidth / 2f - FontRenderAdapter.getStringWidth(warningText) / 2f
                val posY = scaledResolution.scaledHeight / 2f - 16f
                val color = ColorHolder(240, 87, 70)
                FontRenderAdapter.drawString(warningText, posX, posY, color = color)
            }
        }

        listener<BlockBreakEvent> {
            if (mc.player == null || mc.player.distanceTo(it.position) > range.value) return@listener
            val breaker = mc.world.getEntityByID(it.breakId) ?: return@listener
            if (it.progress in 0..9) {
                val render = mc.player != breaker || espSelf.value
                breakingBlockList.putIfAbsent(it.breakId, Triple(it.position, it.progress, Pair(false, render)))
                breakingBlockList.computeIfPresent(it.breakId) { _, triple -> Triple(it.position, it.progress, triple.third) }
                if (warning.value && (mc.player != breaker || warnSelf.value) && it.progress >= warningProgress.value && !breakingBlockList[it.breakId]!!.third.first
                        && ((obsidianOnly.value && mc.world.getBlockState(it.position).block == Blocks.OBSIDIAN) || !obsidianOnly.value)) {
                    if (soundWarn.value) mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                    warningText = KamiLang.get("module.modules.render.BreakingESP.{breaker.name}IsBreakingNear", breaker.name)
                    if (chatWarn.value) sendChatMessage(warningText)
                    delay = 0
                    warn = true
                    breakingBlockList[it.breakId] = Triple(it.position, it.progress, Pair(true, render))
                }
            } else {
                breakingBlockList.remove(it.breakId)
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            breakingBlockList.values.removeIf { triple ->
                world.isAirBlock(triple.first)
            }
        }
    }
}
