package me.zeroeightsix.kami.module.modules.render

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
    name = "BreakingESP",
    description = "Highlights blocks being broken near you",
    category = Category.RENDER
) {
    private val espSelf = setting("ESPSelf", true)
    private val warnSelf = setting("WarnSelf", false)
    private val obsidianOnly = setting("ObsidianOnly", false)
    private val warning = setting("Warn", false)
    private val warningProgress = setting("WarnProgress", 4, 0..10, 1)
    private val chatWarn = setting("ChatWarning", false)
    private val screenWarn = setting("HUDWarning", true)
    private val soundWarn = setting("SoundWarning", false)
    private val range = setting("Range", 16.0f, 2.0f..32.0f, 2.0f)
    private val filled = setting("Filled", true)
    private val outline = setting("Outline", true)
    private val tracer = setting("Tracer", false)
    private val r = setting("Red", 255, 0..255, 1)
    private val g = setting("Green", 255, 0..255, 1)
    private val b = setting("Blue", 255, 0..255, 1)
    private val aFilled = setting("FilledAlpha", 31, 0..255, 1, { filled.value })
    private val aOutline = setting("OutlineAlpha", 200, 0..255, 1, { outline.value })
    private val aTracer = setting("TracerAlpha", 255, 0..255, 1, { outline.value })
    private val thickness = setting("LineThickness", 2.0f, 0.25f..5.0f, 0.25f)

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
                    warningText = "${breaker.name} is breaking near you!"
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
