package org.kamiblue.client.module.modules.render

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.events.BlockBreakEvent
import org.kamiblue.client.event.events.RenderOverlayEvent
import org.kamiblue.client.event.events.RenderWorldEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.ESPRenderer
import org.kamiblue.client.util.graphics.font.FontRenderAdapter
import org.kamiblue.client.util.math.VectorUtils.distanceTo
import org.kamiblue.client.util.text.MessageSendHelper.sendChatMessage
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener

internal object BreakingESP : Module(
    name = "BreakingESP",
    description = "Highlights blocks being broken near you",
    category = Category.RENDER
) {
    private val espSelf = setting("ESP Self", true)
    private val warnSelf = setting("Warn Self", false)
    private val obsidianOnly = setting("Obsidian Only", false)
    private val warning = setting("Warn", false)
    private val warningProgress = setting("Warn Progress", 4, 0..10, 1)
    private val chatWarn = setting("Chat Warning", false)
    private val screenWarn = setting("HUD Warning", true)
    private val soundWarn = setting("Sound Warning", false)
    private val range = setting("Range", 16.0f, 2.0f..32.0f, 2.0f)
    private val filled = setting("Filled", true)
    private val outline = setting("Outline", true)
    private val tracer = setting("Tracer", false)
    private val r = setting("Red", 255, 0..255, 1)
    private val g = setting("Green", 255, 0..255, 1)
    private val b = setting("Blue", 255, 0..255, 1)
    private val aFilled = setting("Filled Alpha", 31, 0..255, 1, { filled.value })
    private val aOutline = setting("Outline Alpha", 200, 0..255, 1, { outline.value })
    private val aTracer = setting("Tracer Alpha", 255, 0..255, 1, { outline.value })
    private val thickness = setting("Line Thickness", 2.0f, 0.25f..5.0f, 0.25f)

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
            val breaker = mc.world.getEntityByID(it.breakerID) ?: return@listener
            if (it.progress in 0..9) {
                val render = mc.player != breaker || espSelf.value
                breakingBlockList.putIfAbsent(it.breakerID, Triple(it.position, it.progress, Pair(false, render)))
                breakingBlockList.computeIfPresent(it.breakerID) { _, triple -> Triple(it.position, it.progress, triple.third) }
                if (warning.value && (mc.player != breaker || warnSelf.value) && it.progress >= warningProgress.value && !breakingBlockList[it.breakerID]!!.third.first
                    && ((obsidianOnly.value && mc.world.getBlockState(it.position).block == Blocks.OBSIDIAN) || !obsidianOnly.value)) {
                    if (soundWarn.value) mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                    warningText = "${breaker.name} is breaking near you!"
                    if (chatWarn.value) sendChatMessage(warningText)
                    delay = 0
                    warn = true
                    breakingBlockList[it.breakerID] = Triple(it.position, it.progress, Pair(true, render))
                }
            } else {
                breakingBlockList.remove(it.breakerID)
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            breakingBlockList.values.removeIf { triple ->
                world.isAirBlock(triple.first)
            }
        }
    }
}
