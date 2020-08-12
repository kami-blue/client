package me.zeroeightsix.kami.module.modules.render

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.BlockBreakEvent
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ColourHolder
import me.zeroeightsix.kami.util.ESPRenderer
import me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos

/**
 * Created by Xiaro on 27/07/20.
 */
@Module.Info(
        name = "BreakingESP",
        description = "Highlights blocks being broken near you",
        category = Module.Category.RENDER
)
class BreakingESP : Module() {
    private val ignoreSelf = register(Settings.b("IgnoreSelf", false))
    private val obsidianOnly = register(Settings.b("ObsidianOnly", false))
    private val warning = register(Settings.b("Warning", false))
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

    private val breakingBlockList = HashMap<Int, Triple<BlockPos, Int, Boolean>>() /* <BreakerID, <Position, Progress, Warned> */

    override fun onWorldRender(event: RenderEvent) {
        val colour = ColourHolder(r.value, g.value, b.value)
        val renderer = ESPRenderer(event.partialTicks)
        renderer.aFilled = if (filled.value) aFilled.value else 0
        renderer.aOutline = if (outline.value) aOutline.value else 0
        renderer.aTracer = if (tracer.value) aTracer.value else 0
        renderer.thickness = thickness.value

        var selfBreaking: AxisAlignedBB? = null
        for ((breakID, pair) in breakingBlockList) {
            val box = mc.world.getBlockState(pair.first).getSelectedBoundingBox(mc.world, pair.first)
            val progress = pair.second / 9f
            val resizedBox = box.shrink((1f - progress) * box.averageEdgeLength * 0.5)
            if (mc.world.getEntityByID(breakID) == mc.player) {
                selfBreaking = resizedBox
                continue
            }
            renderer.add(resizedBox, colour)
        }
        renderer.render()

        if (selfBreaking != null) {
            renderer.aTracer = 0
            renderer.add(selfBreaking, colour)
            renderer.render()
        }
    }

    @EventHandler
    private val blockBreaklistener = Listener(EventHook { event: BlockBreakEvent ->
        if (mc.player == null || mc.player.getDistanceSq(event.position) > range.value * range.value) return@EventHook
        val breaker = mc.world.getEntityByID(event.breakId)?: return@EventHook
        if (ignoreSelf.value && breaker == mc.player) return@EventHook
        if (event.progress in 0..9) {
            breakingBlockList.putIfAbsent(event.breakId, Triple(event.position, event.progress, false))
            breakingBlockList.computeIfPresent(event.breakId) { _, triple -> Triple(event.position, event.progress, triple.third)}
            if (warning.value && breaker != mc.player && event.progress > 4 && !breakingBlockList[event.breakId]!!.third
                    && ((obsidianOnly.value && mc.world.getBlockState(event.position).block == Blocks.OBSIDIAN) || !obsidianOnly.value)) {
                mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                sendChatMessage("${breaker.name} is breaking near you!")
                breakingBlockList[event.breakId] = Triple(event.position, event.progress, true)
            }
        } else {
            breakingBlockList.remove(event.breakId)
        }
    })
}
