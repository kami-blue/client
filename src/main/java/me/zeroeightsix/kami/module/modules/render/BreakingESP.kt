package me.zeroeightsix.kami.module.modules.render

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.BlockBreakEvent
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.font.KamiFontRenderer
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos

@Module.Info(
        name = "BreakingESP",
        description = "Highlights blocks being broken near you",
        category = Module.Category.RENDER
)
object BreakingESP : Module() {
    private val espSelf = register(Settings.b("ESPSelf", true))
    private val warnSelf = register(Settings.b("WarnSelf", false))
    private val obsidianOnly = register(Settings.b("ObsidianOnly", false))
    private val warning = register(Settings.b("Warn", false))
    private val warningProgress = register(Settings.integerBuilder("WarnProgress").withMinimum(0).withValue(4).withMaximum(9).build())
    private val chatWarn = register(Settings.b("ChatWarning", false))
    private val screenWarn = register(Settings.b("HUDWarning", true))
    private val soundWarn = register(Settings.b("SoundWarning", false))
    private val range = register(Settings.floatBuilder("Range").withValue(16.0f).withRange(0.0f, 64.0f).build())
    private val filled = register(Settings.b("Filled", true))
    private val outline = register(Settings.b("Outline", true))
    private val tracer = register(Settings.b("Tracer", false))
    private val r = register(Settings.integerBuilder("Red").withMinimum(0).withValue(255).withMaximum(255).build())
    private val g = register(Settings.integerBuilder("Green").withMinimum(0).withValue(255).withMaximum(255).build())
    private val b = register(Settings.integerBuilder("Blue").withMinimum(0).withValue(255).withMaximum(255).build())
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(31).withRange(0, 255).withVisibility { filled.value }.build())
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(200).withRange(0, 255).withVisibility { outline.value }.build())
    private val aTracer = register(Settings.integerBuilder("TracerAlpha").withValue(255).withRange(0, 255).withVisibility { outline.value }.build())
    private val thickness = register(Settings.floatBuilder("LineThickness").withValue(2.0f).withRange(0.0f, 8.0f).build())

    private val breakingBlockList = HashMap<Int, Triple<BlockPos, Int, Pair<Boolean, Boolean>>>() /* <BreakerID, <Position, Progress, <Warned, Render>> */

    private var warningText = ""

    override fun onWorldRender(event: RenderEvent) {
        val colour = ColorHolder(r.value, g.value, b.value)
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
                renderer.add(resizedBox, colour)
            }
        }
        renderer.render(true)

        if (selfBreaking != null) {
            renderer.aTracer = 0
            renderer.add(selfBreaking, colour)
            renderer.render(true)
        }
    }

    private var warn = false
    private var delay = 0

    override fun onRender() {
        if (screenWarn.value && warn) {
            if (delay++ > 100) warn = false
            val scale = DisplayGuiScreen.getScale().toInt()
            val divider = if (scale == 0) 1 else scale
            val posX = mc.displayWidth / divider / 2f - KamiFontRenderer.getStringWidth(warningText) / 2f
            val posY = mc.displayHeight / divider / 2f - 16f
            val color = ColorHolder(240, 87, 70)
            KamiFontRenderer.drawString(warningText, posX, posY, color = color)
        }
    }

    @EventHandler
    private val blockBreaklistener = Listener(EventHook { event: BlockBreakEvent ->
        if (mc.player == null || mc.player.getDistanceSq(event.position) > range.value * range.value) return@EventHook
        val breaker = mc.world.getEntityByID(event.breakId) ?: return@EventHook
        if (event.progress in 0..9) {
            val render = mc.player != breaker || espSelf.value
            breakingBlockList.putIfAbsent(event.breakId, Triple(event.position, event.progress, Pair(false, render)))
            breakingBlockList.computeIfPresent(event.breakId) { _, triple -> Triple(event.position, event.progress, triple.third) }
            if (warning.value && (mc.player != breaker || warnSelf.value) && event.progress >= warningProgress.value && !breakingBlockList[event.breakId]!!.third.first
                    && ((obsidianOnly.value && mc.world.getBlockState(event.position).block == Blocks.OBSIDIAN) || !obsidianOnly.value)) {
                if (soundWarn.value) mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                warningText = "${breaker.name} is breaking near you!"
                if (chatWarn.value) sendChatMessage(warningText)
                delay = 0
                warn = true
                breakingBlockList[event.breakId] = Triple(event.position, event.progress, Pair(true, render))
            }
        } else {
            breakingBlockList.remove(event.breakId)
        }
    })

    override fun onUpdate() {
        breakingBlockList.values.removeIf { triple ->
            mc.world.isAirBlock(triple.first)
        }
    }
}
